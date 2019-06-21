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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.hcgames.hcfactions.event.claim.PlayerClaimEnterEvent;
import org.hcgames.kmextra.KMExtra;
import org.hcgames.kmextra.util.ItemBuilder;
import technology.brk.staff.event.PlayerDisableVanishEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinItemListener implements Listener{

    private final Map<Integer, ItemStack> joinItems = new HashMap<>();
    private final KMExtra plugin;

    public JoinItemListener(KMExtra plugin){
        for(String slotAsString : plugin.getConfig().getConfigurationSection("join-items").getKeys(false)){
            joinItems.put(Integer.valueOf(slotAsString), ItemBuilder.buildItem(plugin, "join-items." + slotAsString));
        }

        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        if(plugin.getFactions().getFactionManager().getFactionAt(player.getLocation()).isSafezone()){
            PlayerInventory inventory = event.getPlayer().getInventory();

            inventory.setArmorContents(null);
            List<ItemStack> keys = clearInventoryReturnKeys(player);

            for(Map.Entry<Integer, ItemStack> entry : joinItems.entrySet()){
                inventory.setItem(entry.getKey(), entry.getValue());
            }

            if(!keys.isEmpty()){
                player.getInventory().addItem(keys.toArray(new ItemStack[keys.size()]));
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        for(Map.Entry<Integer, ItemStack> entry : joinItems.entrySet()){
            event.getPlayer().getInventory().setItem(entry.getKey(), entry.getValue());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerEnterClaim(PlayerClaimEnterEvent event){
        if(!event.getPlayer().hasMetadata("staffmode") && event.getToFaction().getName().equals("Spawn")){
            Player player = event.getPlayer();
            PlayerInventory inventory = player.getInventory();

            inventory.setArmorContents(null);
            List<ItemStack> keys = clearInventoryReturnKeys(player);

            for(PotionEffect effect : player.getActivePotionEffects()){
                player.removePotionEffect(effect.getType());
            }

            for(Map.Entry<Integer, ItemStack> entry : joinItems.entrySet()){
                inventory.setItem(entry.getKey(), entry.getValue());
            }

            if(!keys.isEmpty()){
                player.getInventory().addItem(keys.toArray(new ItemStack[keys.size()]));
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
        if(plugin.getFactions().getFactionManager().getFactionAt(event.getPlayer().getLocation()).getName().equals("Spawn")){
            event.setCancelled(true);
        }
    }

    private List<ItemStack> clearInventoryReturnKeys(Player player){
        List<ItemStack> keys = new ArrayList<>();
        int current = 0;

        for(ItemStack item : player.getInventory().getContents()){
            //TODO: hard coded
            if(item != null && item.getType() == Material.TRIPWIRE_HOOK && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Event Key")){
                keys.add(item);
            }

            player.getInventory().setItem(current, null);
            current++;
        }

        return keys;
    }

//    @EventHandler
//    public void onInventoryClick(InventoryClickEvent event){
//        if(!(event.getWhoClicked() instanceof Player)) return;
//        Player player = (Player) event.getWhoClicked();
//
//        if(event.getClickedInventory().equals(player.getInventory())){
//            if(joinItems.containsKey(event.getSlot()) && joinItems.get(event.getSlot()).equals(event.getCurrentItem())){
//                event.setCancelled(true);
//            }
//        }
//    }
//
//    @EventHandler
//    public void onInventoryDrag(InventoryDragEvent event){
//        if(!(event.getWhoClicked() instanceof Player)) return;
//        Player player = (Player) event.getWhoClicked();
//
//        if(event.getView().getBottomInventory().equals(player.getInventory())){
//            for(int slot : event.getRawSlots()){
//                if(joinItems.containsKey(slot)){
//                    event.setCancelled(true);
//                    break;
//                }
//            }
//        }
//    }

}
