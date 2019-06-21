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

package technology.brk.staff.command;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import technology.brk.staff.Staff;

@RequiredArgsConstructor @Deprecated
public class StaffCommand implements CommandExecutor{

    private final Staff plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]){
        boolean isConsole = false;

        if(!(sender instanceof Player)){
            isConsole = true;
        }

        if(!sender.hasPermission("staff.command.staff") && !isConsole){
            sender.sendMessage(plugin.getMessages().getString("Messages-NoPermission"));
            return true;
        }

        boolean otherPlayer = true;
        Player targetPlayer = null;

        if(args.length >= 1){
            if(!sender.hasPermission("staff.command.staff.other") && !isConsole){
                sender.sendMessage(plugin.getMessages().getString("Commands-Staff-CannotToggleOthers"));
                return true;
            }

            targetPlayer = Bukkit.getServer().getPlayer(args[0]);

            if(targetPlayer == null){
                sender.sendMessage(plugin.getMessages().getString("Commands-Staff-InvalidPlayer")
                        .replace("{player}", args[0]));
                return true;
            }
        }

        if(targetPlayer == null && isConsole){
            sender.sendMessage(plugin.getMessages().getString("Commands-Staff-PlayerRequiredConsole"));
            return true;
        }

        if(targetPlayer == null){
            targetPlayer = (Player) sender;
            otherPlayer = false;
        }

        boolean state = plugin.getStaffMode().inStaffMode(targetPlayer);

        if(state){
            plugin.getStaffMode().disableStaffMode(targetPlayer);
        }else{
            if(plugin.getIcefyreHook().inFight(targetPlayer)){
                sender.sendMessage(ChatColor.RED + "You cannot enter staff mode while in a fight.");
                return true;
            }

            plugin.getStaffMode().enableStaffMode(targetPlayer);
        }

        state = !state;

        targetPlayer.sendMessage(plugin.getMessages().getString("Commands-Staff-Toggled")
                .replace("{state}", (state ?
                        plugin.getMessages().getString("Commands-Staff-StateEnabled") :
                        plugin.getMessages().getString("Commands-Staff-StateDisabled"))));

        if(otherPlayer){
            sender.sendMessage(plugin.getMessages().getString("Commands-Staff-ToggledOther")
                    .replace("{player}", targetPlayer.getName())
                    .replace("{state}", (state ?
                            plugin.getMessages().getString("Commands-Staff-StateEnabled") :
                            plugin.getMessages().getString("Commands-Staff-StateDisabled"))));
        }
        return true;
    }
}
