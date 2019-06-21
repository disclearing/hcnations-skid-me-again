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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import technology.brk.staff.Staff;
import technology.brk.staff.player.StaffMember;
import technology.brk.staff.util.Permissions;

@Deprecated
public class GoCommand implements CommandExecutor{

    private Staff plugin;
    private int maxTime;

    public GoCommand(Staff plugin){
        this.plugin = plugin;
        maxTime = plugin.getConfig().getInt("Go.Maximum-Time", 100);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]){
        if(!(sender instanceof Player)){
            sender.sendMessage(plugin.getMessages().getString("Messages-PlayerOnly"));
            return true;
        }

        if(!sender.hasPermission(Permissions.COMMAND_GO_MODE)){
            sender.sendMessage(plugin.getMessages().getString("Messages-NoPermission"));
            return true;
        }


        Player player = (Player) sender;
        StaffMember primePlayer = plugin.getPlayerManager().getPlayer(player);

        if(!(args.length >= 1)){
            player.sendMessage(plugin.getMessages().getString("Commands-Go-Usage"));
            return true;
        }

        if(args[0].equalsIgnoreCase("toggle")){

            if(plugin.getGoManager().hasGoPaused(player.getUniqueId())){
                plugin.getGoManager().setPaused(player.getUniqueId(), false);

                player.sendMessage(plugin.getMessages().getString("Commands-Go-Toggled")
                        .replace("{toggleState}", plugin.getMessages().getString("Commands-Go-ToggleEnable")));
            }else if(plugin.getGoManager().hasGoEnabled(player.getUniqueId())){
                plugin.getGoManager().setPaused(player.getUniqueId(), true);

                player.sendMessage(plugin.getMessages().getString("Commands-Go-Toggled")
                        .replace("{toggleState}", plugin.getMessages().getString("Commands-Go-ToggleDisable")));
            }else{
                player.sendMessage(plugin.getMessages().getString("Commands-Go-ToggleNotInGo"));
            }
            return true;
        }

        if(plugin.getGoManager().hasGoEnabled(player.getUniqueId())){
            player.sendMessage(plugin.getMessages().getString("Commands-Go-AlreadyEnabled"));
            return true;
        }

        if(!(args.length >= 1)){
            player.sendMessage(plugin.getMessages().getString("Commands-Go-Usage"));
            return true;
        }

        int maxTime = plugin.getConfig().getInt("Go.Maximum-Time", 100);
        int time;

        try{
            time = Integer.valueOf(args[0]);
        }catch(NumberFormatException exception){
            player.sendMessage(plugin.getMessages().getString("Error-Messages.Invalid-Number")
                    .replace("{number}", args[0]));
            return true;
        }

        if(time >= maxTime && !(maxTime == -1)){
            player.sendMessage(plugin.getMessages().getString("Messages-TimeTooLong")
                    .replace("{maxTime}", String.valueOf(maxTime))
                    .replace("{number}", args[0]));
            return true;
        }

        if(time <= 0){
            player.sendMessage(plugin.getMessages().getString("Commands-Go-TimeMustBeAboveZerod"));
            return true;
        }

        plugin.getGoManager().add(player, time * 20);
        player.sendMessage(plugin.getMessages().getString("Commands-Go-Enabled")
                .replace("{time}", String.valueOf(time))
                .replace("{s}", time == 1 ? "" : "s"));
        return false;
    }
}
