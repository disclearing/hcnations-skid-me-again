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

package com.doctordark.hcf.command.player;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.user.FactionUser;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import technology.brk.base.BasePlugin;

public class FilterCommand implements CommandExecutor{

    private final String spacer;
    private final HCF plugin;

    public FilterCommand(HCF plugin){
        this.plugin = plugin;
        spacer = plugin.getMessages().getString("Commands.Filter.List.Spacer");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(!(sender instanceof Player)){
            sender.sendMessage(plugin.getMessages().getString("Error-Messages.PlayerOnly"));
            return true;
        }

        if(args.length < 1){
            sender.sendMessage(plugin.getMessages().getString("Commands.Filter.Invalid-Usage"));
            return true;
        }

        Player player = (Player) sender;
        FactionUser user = plugin.getUserManager().getUser(player.getUniqueId());

        if(args[0].equalsIgnoreCase("add")){
            if(args.length < 2){
                sender.sendMessage(plugin.getMessages().getString("Commands.Filter.Add.Usage"));
                return true;
            }

            ItemStack item;
            if((item = BasePlugin.getPlugin().getItemDb().getItem(args[1])) == null || item.getType().equals(Material.AIR)){
                sender.sendMessage(plugin.getMessages().getString("Commands.Filter.Invalid-Item").replace("{item}", args[1]));
                return true;
            }

            if(user.isFiltered(item.getType())){
                sender.sendMessage(plugin.getMessages().getString("Commands.Filter.Add.Already-Added").replace("{item}", args[1]));
                return true;
            }

            sender.sendMessage(plugin.getMessages().getString("Commands.Filter.Add.Added").replace("{item}", args[1]));
            user.setFiltered(item.getType(), true);
            return true;
        }

        if(args[0].equalsIgnoreCase("remove")){
            if(args.length < 2){
                sender.sendMessage(plugin.getMessages().getString("Commands.Filter.Remove.Add.Usage"));
                //invalid usage
                return true;
            }

            ItemStack item;
            if((item = BasePlugin.getPlugin().getItemDb().getItem(args[1])) == null || item.getType().equals(Material.AIR)){
                sender.sendMessage(plugin.getMessages().getString("Commands.Filter.Invalid-Item").replace("{item}", args[1]));
                return true;
            }

            if(!user.isFiltered(item.getType())){
                sender.sendMessage(plugin.getMessages().getString("Commands.Filter.Does-Not-Exist").replace("{item}", args[1]));
                return true;
            }

            sender.sendMessage(plugin.getMessages().getString("Commands.Filter.Remove.Removed").replace("{item}", args[1]));
            user.setFiltered(item.getType(), false);
            return true;
        }

        if(args[0].equalsIgnoreCase("list")){
            if(user.getFilteredBlocks().isEmpty()){
                sender.sendMessage(plugin.getMessages().getString("Commands.Filter.List.No-Blocks"));
                return true;
            }

            StringBuilder items = new StringBuilder();
            user.getFilteredBlocks().forEach(material -> items.append(BasePlugin.getPlugin().getItemDb().getName(new ItemStack(material))).append(spacer));
            items.setLength(Math.max(0, items.length() - 2));
            sender.sendMessage(plugin.getMessages().getString("Commands.Filter.List.Items").replace("{items}", items.toString()));
            return true;
        }

        return false;
    }
}
