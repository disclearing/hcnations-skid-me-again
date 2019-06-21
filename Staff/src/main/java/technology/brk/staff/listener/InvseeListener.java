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

package technology.brk.staff.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import technology.brk.staff.Staff;
import technology.brk.staff.manager.invsee.OpenedInventory;

import java.util.Arrays;
import java.util.UUID;

@RequiredArgsConstructor
public class InvseeListener implements Listener{

    private final Staff plugin;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        if(plugin.getInvseeManager().beingWatched(player.getUniqueId())){
            OpenedInventory inventory = plugin.getInvseeManager().getInventory(player.getUniqueId());

            if(inventory.isOffline()){
                inventory.becomeOnline(player);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        if(plugin.getInvseeManager().beingWatched(player.getUniqueId())){
            OpenedInventory inventory = plugin.getInvseeManager().getInventory(player.getUniqueId());
//            Set<Player> viewers = inventory.getViewers();
            inventory.remove();

//            plugin.getServer().getScheduler().runTask(plugin, () -> {
//                for(Player viewer : viewers){
//                    plugin.getServer().dispatchCommand(viewer, "invsee " + player.getName());
//                }
//            });
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if(!(event.getPlayer() instanceof Player)){
            return;
        }

        Player player = (Player) event.getPlayer();
        OpenedInventory watching = plugin.getInvseeManager().getWatching(player);

        if(watching != null){
            watching.removeViewer(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();
            update(player.getUniqueId(), 1, 3);
        }

        if(event instanceof EntityDamageByEntityEvent){
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;

            if(entityDamageByEntityEvent.getDamager() instanceof Player){
                Player damager = (Player) entityDamageByEntityEvent.getDamager();
                update(damager.getUniqueId(), 0);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityRegainHealthEvent(EntityRegainHealthEvent event){
        if(event.getEntityType() != EntityType.PLAYER){
            return;
        }

        Player player = (Player) event.getEntity();
        update(player.getUniqueId(), 3);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event){
        Player player = event.getPlayer();

        if(plugin.getInvseeManager().beingWatched(player.getUniqueId())){
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> plugin.getInvseeManager().getInventory(player.getUniqueId()).updateDisplayedContents(0));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
        Player player = event.getPlayer();

        if(plugin.getInvseeManager().beingWatched(player.getUniqueId())){
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> plugin.getInvseeManager().getInventory(player.getUniqueId()).updateDisplayedContents(0));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        update(player.getUniqueId(), 0, 1, 2, 3);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        update(player.getUniqueId(), 0, 1, 2, 3);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event){
        Player player = event.getPlayer();

        if(plugin.getInvseeManager().beingWatched(player.getUniqueId())){
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                OpenedInventory inventory = plugin.getInvseeManager().getInventory(player.getUniqueId());

                inventory.updateDisplayedContents(0);
                inventory.updateDisplayedContents(1);
            });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClickMonitor(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player)){
            return;
        }

        Player player = (Player) event.getWhoClicked();
        update(player.getUniqueId(), 0, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player) || event.getClickedInventory() == null){
            return;
        }

        if(event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR){
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if(plugin.getInvseeManager().isViewer(player)){
            if(!player.hasPermission("staff.invsee.edit") | event.getSlot() >= 35){
                event.setResult(Event.Result.DENY);
            }

            OpenedInventory inventory = plugin.getInvseeManager().getWatching(player);

            if(!inventory.isOffline()){
                event.setResult(Event.Result.DENY);
            }

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                if(event.getSlot() == 44 && clickedItem.getType().equals(plugin.getInvseeManager().getData().getClearInventoryItem().getType())){
                    inventory.triggerClear(player);
                    return;
                }

                if(player.hasPermission("staff.invsee.edit")){
                    if(!inventory.isOffline()){
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            Player viewee = plugin.getServer().getPlayer(inventory.getOwner());

                            if(viewee != null){
                                viewee.getInventory().setContents(Arrays.copyOfRange(inventory.getInventory().getContents(), 0, 35));
                            }
                        }, 1L);
                    }
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event){
        if(!(event.getWhoClicked() instanceof Player)){
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if(plugin.getInvseeManager().isViewer(player)){
            event.setResult(Event.Result.DENY);
        }
    }


    private void update(UUID whom, int... ints){
        if(plugin.getInvseeManager().beingWatched(whom)){
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                OpenedInventory inventory = plugin.getInvseeManager().getInventory(whom);

                for(int i : ints){
                    inventory.updateDisplayedContents(i);
                }
            });
        }
    }
}
