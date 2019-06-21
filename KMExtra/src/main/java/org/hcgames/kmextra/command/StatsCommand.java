/*
 *   COPYRIGHT NOTICE
 *
 *   Copyright (C) 2016, SystemUpdate, <admin@systemupdate.io>.
 *
 *   All rights reserved.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS. IN
 *   NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *   DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 *   OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 *   OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *   Except as contained in this notice, the name of a copyright holder shall not
 *   be used in advertising or otherwise to promote the sale, use or other dealings
 *   in this Software without prior written authorization of the copyright holder.
 */

package org.hcgames.kmextra.command;

import com.evilmidget38.UUIDFetcher;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.kmextra.KMExtra;
import org.hcgames.kmextra.profile.Profile;
import org.hcgames.kmextra.util.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class StatsCommand implements CommandExecutor{

    private Cache<String, Optional<UUID>> userCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000).build(new CacheLoader<String, Optional<UUID>>() {
        @Override
        public Optional<UUID> load(String username) throws Exception{
            return Optional.ofNullable(UUIDFetcher.getUUIDOf(username));
        }
    });

    private final KMExtra plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
        if(args.length == 1){
            Player player = plugin.getServer().getPlayer(args[0]);
            if(player != null) {
                printStats(sender, player.getName(), player.getUniqueId(), false);
            }else if(userCache.asMap().containsKey(args[0].toLowerCase())){
                Optional<UUID> uuid = userCache.getUnchecked(args[0]);

                if(uuid.isPresent()){
                    printStats(sender, args[0], uuid.get(), false);
                }else{
                    sender.sendMessage(Messages.get("Player-Not-Found"));
                }
            }else{
                sender.sendMessage(ChatColor.GRAY + "(Loading...)");
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    Optional<UUID> uuid = userCache.getUnchecked(args[0]);

                    if(uuid.isPresent()){
                        printStats(sender, args[0], uuid.get(), true);
                    }else{
                        sender.sendMessage(Messages.get("Player-Not-Found").replace("{player}", args[0]));
                    }
                });
            }
        }else if(sender instanceof Player){
            printStats(sender, sender.getName(), ((Player) sender).getUniqueId(), false);
        }else{
            sender.sendMessage(Messages.get("Stats-Console"));
        }

        return true;
    }

    private void printStats(CommandSender sender, String username, UUID player, boolean async){
        Profile profile = async ? plugin.getProfileManager().getProfileAsync(player) : plugin.getProfileManager().getProfile(player);

        sender.sendMessage(Messages.get("Stats").replace("{player}", username)
                .replace("{kills}", String.valueOf(plugin.getStats().getKills(player)))
                .replace("{deaths}", String.valueOf(plugin.getStats().getDeaths(player)))
                .replace("{killstreak}", String.valueOf(profile.getKillstreak()))
                .replace("{highestKillstreak}", String.valueOf(profile.getHighestKillStreak())));
    }
}
