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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import technology.brk.staff.Staff;
import technology.brk.staff.manager.FreezeManager;

@RequiredArgsConstructor
public class FreezeCommand implements CommandExecutor{

    private final Staff plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]){
        if(!(args.length >= 1)){
            sender.sendMessage(plugin.getMessages().getString("commands.freeze.usage"));
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);

        if(target == null){
            sender.sendMessage(plugin.getMessages().getString("error.player_not_found", args[0]));
            return true;
        }

        if(target.hasPermission("staff.bypass.freeze")){
            sender.sendMessage(plugin.getMessages().getString("commands.freeze.cannot_freeze", target.getName()));
            return true;
        }

        FreezeManager.FreezeState currentState = plugin.getFreezeManager().getState(target);

        switch(currentState){
            case NONE:
                plugin.getFreezeManager().setState(target, FreezeManager.FreezeState.INVENTORY);
                sender.sendMessage(plugin.getMessages().getString("commands.freeze.frozen_user", target.getName()));
                target.sendMessage(plugin.getMessages().getString("commands.freeze.frozen"));
                break;
            case INVENTORY:
                plugin.getFreezeManager().setState(target, FreezeManager.FreezeState.NORMAL);
                target.closeInventory();
                sender.sendMessage(plugin.getMessages().getString("commands.freeze.frozen_user_normal", target.getName()));
                break;
            case NORMAL:
                plugin.getFreezeManager().setState(target, FreezeManager.FreezeState.NONE);
                sender.sendMessage(plugin.getMessages().getString("commands.freeze.unfrozen_user", target.getName()));
                target.sendMessage(plugin.getMessages().getString("commands.freeze.unfrozen"));
                break;
            default:
                throw new RuntimeException("Unknown state " + currentState);
        }

        return true;
    }
}
