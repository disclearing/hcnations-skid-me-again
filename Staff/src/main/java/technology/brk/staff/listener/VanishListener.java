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
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.inventory.InventoryHolder;
import technology.brk.staff.Staff;

import java.util.Iterator;
import java.util.UUID;

@RequiredArgsConstructor
public class VanishListener implements Listener{

    private final Staff plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        if(player.hasPermission("staff.auto.vanish") ){
            plugin.getVanishManager().setVanished(player, true);
        }

        if(!player.hasPermission("staff.staff")){
            for(UUID vanishedUser : plugin.getVanishManager().getVanishedUsers()){
                Player vanishedPlayer = plugin.getServer().getPlayer(vanishedUser);

                if(player.equals(vanishedPlayer)){
                    continue;
                }

                player.hidePlayer(vanishedPlayer);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        plugin.getVanishManager().setVanished(player, false);
        plugin.getVanishManager().handleSilentClose(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR))){
            return;
        }

        Player player = event.getPlayer();

        if(plugin.getVanishManager().isVanished(player)){
            BlockState blockState = event.getClickedBlock().getState();
            event.setCancelled(true);

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                if(blockState instanceof InventoryHolder){
                    plugin.getVanishManager().openSilently(player, ((InventoryHolder) blockState).getInventory());
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player)){
            return; //Better safe then sorry
        }

        Player player = (Player) event.getWhoClicked();

        if(plugin.getVanishManager().hasSilentChestOpen(player)){
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if(!(event.getPlayer() instanceof Player)){
            return; //Better safe then sorry
        }

        Player player = (Player) event.getPlayer();
        plugin.getVanishManager().handleSilentClose(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        if(plugin.getVanishManager().isVanished(player) && !player.hasPermission("staff.vanish.interact.build")){
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getString("vanish.cannot_interact"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        if(plugin.getVanishManager().isVanished(player) && !player.hasPermission("staff.vanish.interact.build")){
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getString("vanish.cannot_interact"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player){
            Player damager = (Player) event.getDamager();
            if(plugin.getVanishManager().isVanished(damager) && !damager.hasPermission("staff.vanish.interact." +
                    (event.getEntity() instanceof Player ? "pvp" : "pve"))){
                event.setCancelled(true);
                damager.sendMessage(plugin.getMessages().getString("vanish.cannot_interact"));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        if(plugin.getVanishManager().isVanished(player)){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
        Player player = event.getPlayer();
        if(plugin.getVanishManager().isVanished(player)){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }

        Player player = (Player) event.getEntity();
        if(plugin.getVanishManager().isVanished(player)){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event){
        if(!(event.getAttacker() instanceof Player)){
            return;
        }

        Player player = (Player) event.getAttacker();
        if(plugin.getVanishManager().isVanished(player)){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestory(VehicleDestroyEvent event){
        if(!(event.getAttacker() instanceof Player)){
            return;
        }

        Player player = (Player) event.getAttacker();
        if(plugin.getVanishManager().isVanished(player)){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event){
        if(!(event.getTarget() instanceof Player)){
            return;
        }

        Player player = (Player) event.getTarget();
        if(plugin.getVanishManager().isVanished(player)){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onServerListPing(ServerListPingEvent event){
        Iterator<Player> players = event.iterator();

        while(players.hasNext()){
            Player player = players.next();

            if(plugin.getVanishManager().isVanished(player)){
                players.remove();
            }
        }
    }
}
