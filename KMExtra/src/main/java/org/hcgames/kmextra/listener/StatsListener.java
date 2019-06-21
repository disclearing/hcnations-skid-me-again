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

package org.hcgames.kmextra.listener;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.hcgames.kmextra.KMExtra;
import org.hcgames.kmextra.profile.Profile;

import java.util.HashMap;
import java.util.List;

public class StatsListener implements Listener{

    private final HashMap<Integer, KSReward> ksRewards = new HashMap<>();
    private final KMExtra plugin;

    public StatsListener(KMExtra plugin){
        this.plugin = plugin;

        for(String entry : plugin.getConfig().getConfigurationSection("KS-Rewards").getKeys(false)){
            ksRewards.put(Integer.valueOf(entry), new KSReward(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("KS-Rewards." + entry + ".Broadcast")),
                    plugin.getConfig().getStringList("KS-Rewards." + entry + ".Commands")));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event){
        Profile dead = plugin.getProfileManager().getProfile(event.getEntity().getUniqueId());
        dead.setKillStreak(0);

        if(event.getEntity().getKiller() == null){
            return;
        }

        Profile profile = plugin.getProfileManager().getProfile(event.getEntity().getKiller().getUniqueId());
        profile.setKillStreak(profile.getKillstreak() + 1);

        if(ksRewards.containsKey(profile.getKillstreak())){
            ksRewards.get(profile.getKillstreak()).run(event.getEntity().getKiller());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event){
        plugin.getProfileManager().getProfile(event.getPlayer().getUniqueId()).setKillStreak(0);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKick(PlayerKickEvent event){
        plugin.getProfileManager().getProfile(event.getPlayer().getUniqueId()).setKillStreak(0);
    }

    @RequiredArgsConstructor
    private class KSReward{
        private final String broadcast;
        private final List<String> commands;

        void run(Player player){
            plugin.getServer().broadcastMessage(broadcast.replace("{player}", player.getName()));

            for(String command : commands){
                plugin.getServer().dispatchCommand(plugin.getCommandWrapper(), command.replace("{player}", player.getName()));
            }
        }
    }
}
