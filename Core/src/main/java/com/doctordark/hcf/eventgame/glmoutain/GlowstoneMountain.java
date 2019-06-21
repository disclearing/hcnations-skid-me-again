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

package com.doctordark.hcf.eventgame.glmoutain;

import com.doctordark.hcf.HCF;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class GlowstoneMountain implements CommandExecutor{

    private final HCF plugin;
    private final Material mountainMaterial;
    private MountainHandler mountainHandler;

    public GlowstoneMountain(HCF plugin){
        this.plugin = plugin;

        plugin.saveDefaultConfig();
        mountainMaterial = Material.valueOf(plugin.getConfig().getString("material").toUpperCase());

        Map<Integer, String> depleteBroadcasts = new HashMap<>();

        for(String key : plugin.getConfig().getConfigurationSection("deplete-broadcast").getKeys(false)){
            depleteBroadcasts.put(Integer.parseInt(key), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("deplete-broadcast." + key)));
        }

        mountainHandler = new MountainHandler(plugin, depleteBroadcasts);
        plugin.getServer().getPluginManager().registerEvents(mountainHandler, plugin);

        Map<Integer, String> resetBroadcasts = new HashMap<>();

        for(String key : plugin.getConfig().getConfigurationSection("reset-broadcast").getKeys(false)){
            resetBroadcasts.put(Integer.parseInt(key), ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("reset-broadcast." + key)));
        }

        new CountdownManager(mountainHandler, plugin.getConfig().getInt("interval"), resetBroadcasts).runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length != 1){
            return false;
        }

        if(args[0].equalsIgnoreCase("set")){
            if(sender instanceof Player){
                Player player = (Player) sender;
                Selection selection = plugin.getWorldEdit().getSelection(player);

                if(selection != null){
                    if(selection instanceof CuboidSelection){
                        mountainHandler.createNewMountain(selection.getMinimumPoint(), selection.getMaximumPoint(), mountainMaterial);
                        player.sendMessage(ChatColor.GREEN + "You have successfully created a mountain!");
                    }else{
                        player.sendMessage(ChatColor.RED + "The selected region must be a cuboid!");
                    }
                }else{
                    player.sendMessage(ChatColor.RED + "You must select a region with the WorldEdit wand!");
                }
            }else{
                sender.sendMessage("You must be a player to use this command!");
            }
        }

        return true;
    }
}
