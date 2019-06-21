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

package com.doctordark.hcf.command.server;

import com.doctordark.hcf.HCF;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import technology.brk.util.BukkitUtils;
import technology.brk.util.InventorySerialisation;
import technology.brk.util.file.Config;

import java.io.IOException;

public class LoadoutCommand implements CommandExecutor{

    private final Config config;
    private final HCF plugin;
    private ItemStack[] inventory;
    private ItemStack[] armor;

    public LoadoutCommand(HCF plugin){
        config = new Config(plugin, "loadout.yml");
        this.plugin = plugin;

        if(config.contains("inventory")){
            try{
                inventory = InventorySerialisation.itemStackArrayFromBase64(config.getString("inventory"));
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        if(config.contains("armor")){
            try{
                armor = InventorySerialisation.itemStackArrayFromBase64(config.getString("armor"));
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("loadout")){
            if(armor == null || inventory == null){
                sender.sendMessage(plugin.getMessages().getString("Commands.Loadout.Error.NotSet"));
                return true;
            }

            if(args.length < 1){
                sender.sendMessage(plugin.getMessages().getString("Commands.Loadout.Error.Usage"));
                return true;
            }

            Player player = plugin.getServer().getPlayer(args[0]);

            if(player == null){
                sender.sendMessage(plugin.getMessages().getString("Error-Messages.InvalidPlayer").replace("{player}", args[0]));
                return true;
            }

            player.getInventory().setArmorContents(BukkitUtils.deepClone(armor));
            player.getInventory().setContents(BukkitUtils.deepClone(inventory));

            sender.sendMessage(plugin.getMessages().getString("Commands.Loadout.Given").replace("{player}", player.getName()));
            Command.broadcastCommandMessage(sender, String.format("Given %s the loadout.", player.getName()), false);
            return true;
        }

        if(cmd.getName().equalsIgnoreCase("setloadout")){
            if(!(sender instanceof Player)){
                sender.sendMessage(plugin.getMessages().getString("Error-Messages.PlayerOnly"));
                return true;
            }

            Player player = (Player) sender;

            armor = BukkitUtils.deepClone(player.getInventory().getArmorContents());
            inventory = BukkitUtils.deepClone(player.getInventory().getContents());

            config.set("armor", InventorySerialisation.itemStackArrayToBase64(armor));
            config.set("inventory", InventorySerialisation.itemStackArrayToBase64(inventory));

            sender.sendMessage(plugin.getMessages().getString("Commands.Loadout.Set"));
            Command.broadcastCommandMessage(sender, "Updated loadout kit", false);
        }

        return true;
    }

    public void save(){
        config.save();
    }
}
