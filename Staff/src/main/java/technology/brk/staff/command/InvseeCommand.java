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
import technology.brk.staff.manager.invsee.OpenedInventory;
import technology.brk.staff.util.UUIDFetcher;

import java.util.UUID;

@RequiredArgsConstructor
public class InvseeCommand implements CommandExecutor{

    private final Staff plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]){
        if(!(sender instanceof Player)){
            sender.sendMessage(plugin.getMessages().getString("error.player_only"));
            return true;
        }

        if(args.length != 1){
            sender.sendMessage(plugin.getMessages().getString("commands.invsee.usage"));
            return true;
        }

        String input = args[0];

        Player target = plugin.getServer().getPlayer(input);
        Player player = (Player) sender;

        if(target == null){
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                sender.sendMessage(ChatColor.GRAY + "(Loading...)");
                UUID uuid = null;

                try{
                    uuid = UUIDFetcher.getUUIDOf(input);
                }catch(Exception ignored){}

                if(uuid == null){
                    sender.sendMessage(plugin.getMessages().getString("error.player_not_found", input));
                    return;
                }

                OpenedInventory inventory;

                if(plugin.getInvseeManager().beingWatched(uuid)){
                    inventory = plugin.getInvseeManager().getInventory(uuid);
                }else{
                    inventory = plugin.getInvseeManager().newOfflineInventory(plugin, uuid, input);
                }

                inventory.addViewer(player);
                player.sendMessage(plugin.getMessages().getString("commands.invsee.opened", input));
            });
        }else{
            OpenedInventory inventory;

            if(plugin.getInvseeManager().beingWatched(target.getUniqueId())){
                inventory = plugin.getInvseeManager().getInventory(target.getUniqueId());
            }else{
                inventory = plugin.getInvseeManager().newOnlineInventory(plugin, target);
            }

            inventory.addViewer(player);
            player.sendMessage(plugin.getMessages().getString("commands.invsee.opened", input));
        }
        return true;
    }
}
