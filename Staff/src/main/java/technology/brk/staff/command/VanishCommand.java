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
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import technology.brk.staff.Staff;
import technology.brk.staff.util.SystemUtil;

@RequiredArgsConstructor
public class VanishCommand implements CommandExecutor{

    private final Staff plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        Player targetPlayer;
        Boolean newState = null;

        if(args.length > 0){
            targetPlayer = plugin.getServer().getPlayer(args[0]);

            if(targetPlayer == null){
                if(sender instanceof Player){
                    targetPlayer = (Player) sender;
                }else{
                    sender.sendMessage(plugin.getMessages().getString("error.player_not_found", args[0]));
                    return true;
                }
            }

            newState = SystemUtil.parseState(args[0]);

            if(newState == null && args.length > 1){
                newState = SystemUtil.parseState(args[1]);

                if(newState == null){
                    sender.sendMessage(plugin.getMessages().getString("error.invalid_state", args[1]));
                    return true;
                }
            }
        }else if(sender instanceof Player){
            targetPlayer = (Player) sender;
        }else{
            sender.sendMessage(plugin.getMessages().getString("commands.vanish.invalid_usage"));
            return true;
        }

        boolean differentPerson = false;
        if(!targetPlayer.getName().equals(sender.getName())){
            differentPerson = true;
            if(!sender.hasPermission("staff.command.vaish.other")){
                sender.sendMessage(plugin.getMessages().getString("commands.vanish.no_permission_other", targetPlayer.getName()));
                return true;
            }
        }

        if(newState == null){
            newState = !plugin.getVanishManager().isVanished(targetPlayer);
        }

        plugin.getVanishManager().setVanished(targetPlayer, newState);

        targetPlayer.sendMessage(plugin.getMessages().getString("commands.vanish." + (newState ? "enabled" : "disabled")));
        if(differentPerson){
            sender.sendMessage(plugin.getMessages().getString("commands.vanish.other", targetPlayer.getName(), (newState ? "enabled" : "disabled")));
        }

        return true;
    }
}
