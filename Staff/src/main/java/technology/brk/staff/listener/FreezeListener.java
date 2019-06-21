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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import technology.brk.staff.Staff;
import technology.brk.staff.manager.FreezeManager;

@RequiredArgsConstructor
public class FreezeListener implements Listener{

    private final Staff plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        if(plugin.getFreezeManager().isFrozen(player)){
            plugin.getServer().broadcast(plugin.getMessages().getString("freeze.alert.login_frozen", player.getName()), "staff.staff");
            player.setWalkSpeed(0.0F);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128), true);

            if(plugin.getFreezeManager().getState(player) == FreezeManager.FreezeState.INVENTORY_OFFLINE){
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    player.closeInventory();
                    player.openInventory(plugin.getFreezeManager().getInventory());
                    plugin.getFreezeManager().setState(player, FreezeManager.FreezeState.INVENTORY);
                }, 20L);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        if(plugin.getFreezeManager().isFrozen(player)){
            plugin.getServer().broadcast(plugin.getMessages().getString("freeze.alert.logout_frozen", player.getName()), "staff.staff");
            player.setWalkSpeed(0.2F);
            player.removePotionEffect(PotionEffectType.JUMP);

            if(plugin.getFreezeManager().getState(player) == FreezeManager.FreezeState.INVENTORY){
                plugin.getFreezeManager().setState(player, FreezeManager.FreezeState.INVENTORY_OFFLINE);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event){
        Player player = event.getPlayer();

        if(plugin.getFreezeManager().getState(player) == FreezeManager.FreezeState.INVENTORY){
            plugin.getFreezeManager().setState(player, FreezeManager.FreezeState.INVENTORY_OFFLINE);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();

        if(plugin.getFreezeManager().isFrozen(player)){
            if(!plugin.getFreezeManager().isCommandAllowed(event.getMessage().substring(1))){
                player.sendMessage(plugin.getMessages().getString("freeze.cannot_interact"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        if(event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()){
            return;
        }

        Player player = event.getPlayer();

        if(plugin.getFreezeManager().isFrozen(player)){
            event.setTo(event.getFrom().getBlock().getLocation());
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event){
        Player player = event.getPlayer();

        if(plugin.getFreezeManager().isFrozen(player)){
            switch(event.getCause()){
                case UNKNOWN:
                    break;
                case COMMAND:
                case END_PORTAL:
                case NETHER_PORTAL:
                case PLUGIN:
                case ENDER_PEARL:
                    event.setCancelled(true);
                    player.sendMessage(plugin.getMessages().getString("freeze.cannot_teleport"));
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player)){
            return; //Better safe then sorry
        }

        Player player = (Player) event.getWhoClicked();

        if(plugin.getFreezeManager().isFrozen(player)){
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
        if(player.isOnline() && plugin.getFreezeManager().getState(player) == FreezeManager.FreezeState.INVENTORY){
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if(player.isOnline() && plugin.getFreezeManager().getState(player) == FreezeManager.FreezeState.INVENTORY){
                    player.openInventory(plugin.getFreezeManager().getInventory());
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        if(plugin.getFreezeManager().isFrozen(player)){
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getString("freeze.cannot_interact"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        if(plugin.getFreezeManager().isFrozen(player)){
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getString("freeze.cannot_interact"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player){
            Player damager = (Player) event.getDamager();
            if(plugin.getFreezeManager().isFrozen(damager)){
                event.setCancelled(true);
                damager.sendMessage(plugin.getMessages().getString("freeze.cannot_interact"));
                return;
            }
        }

        if(event.getEntity() instanceof Player){
            Player entity = (Player) event.getEntity();

            if(plugin.getFreezeManager().isFrozen(entity)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        if(plugin.getFreezeManager().isFrozen(player)){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
        Player player = event.getPlayer();
        if(plugin.getFreezeManager().isFrozen(player)){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }

        Player player = (Player) event.getEntity();
        if(plugin.getFreezeManager().isFrozen(player)){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event){
        if(!(event.getAttacker() instanceof Player)){
            return;
        }

        Player player = (Player) event.getAttacker();
        if(plugin.getFreezeManager().isFrozen(player)){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestory(VehicleDestroyEvent event){
        if(!(event.getAttacker() instanceof Player)){
            return;
        }

        Player player = (Player) event.getAttacker();
        if(plugin.getFreezeManager().isFrozen(player)){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event){
        if(!(event.getTarget() instanceof Player)){
            return;
        }

        Player player = (Player) event.getTarget();
        if(plugin.getFreezeManager().isFrozen(player)){
            event.setCancelled(true);
        }
    }
}
