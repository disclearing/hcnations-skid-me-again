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

package org.hcgames.hcfactions.command;

import com.doctordark.hcf.HCF;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.faction.Faction;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class LocationCommand implements CommandExecutor, TabCompleter{

    private final HCFactions plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        if (args.length >= 1 && sender.hasPermission(command.getPermission() + ".others")) {
            target = plugin.getServer().getPlayer(args[0]);
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Location.Usage")
                    .replace("{commandLabel}", label));
            return true;
        }

        if (target == null || (sender instanceof Player && !((Player) sender).canSee(target))) {
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Location.Output")
                    .replace("{player}", args[0]));
            return true;
        }

        Location location = target.getLocation();
        Faction factionAt = plugin.getFactionManager().getFactionAt(location);

        sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Location.Output")
                .replace("{player}", target.getName())
                .replace("{factionName}", factionAt.getFormattedName(sender))
                .replace("{isDeathBanLocation}", factionAt.isSafezone() ?
                        HCF.getPlugin().getMessages().getString("Commands.Location.NonDeathban") :
                        HCF.getPlugin().getMessages().getString("Commands.Location.Deathban")));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 1 && sender.hasPermission(command.getPermission() + ".others") ? null : Collections.emptyList();
    }

}
