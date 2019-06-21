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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import technology.brk.staff.Staff;
import technology.brk.staff.event.PlayerDisableVanishEvent;
import technology.brk.staff.event.PlayerEnableVanishEvent;
import technology.brk.staff.manager.invsee.OpenedInventory;
import technology.brk.staff.util.EnchantGlow;

@RequiredArgsConstructor
public class StaffModeListener implements Listener{

    private final Staff plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerEnableVanish(PlayerEnableVanishEvent event){
        Player player = event.getPlayer();

        if(plugin.getStaffMode().inStaffMode(player)){
            EnchantGlow.addGlow(player.getInventory().getItem(2));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDisableVanish(PlayerDisableVanishEvent event){
        Player player = event.getPlayer();

        if(plugin.getStaffMode().inStaffMode(player)){
            EnchantGlow.removeGlow(player.getInventory().getItem(2));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
        Player player = event.getPlayer();

        if(plugin.getStaffMode().inStaffMode(player)){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
        if(!(event.getRightClicked() instanceof Player) || event.getPlayer().getItemInHand() == null){
            return;
        }

        Player player = event.getPlayer();

        if(plugin.getStaffMode().inStaffMode(player)){
            Player clickedPlayer = (Player) event.getRightClicked();

            if(plugin.getStaffMode().getStaffItemData().getBook().equals(player.getItemInHand())){
                OpenedInventory inventory;

                if(plugin.getInvseeManager().beingWatched(clickedPlayer.getUniqueId())){
                    inventory = plugin.getInvseeManager().getInventory(clickedPlayer.getUniqueId());
                }else{
                    inventory = plugin.getInvseeManager().newOnlineInventory(plugin, clickedPlayer);
                }

                inventory.addViewer(player);
            }
        }
    }

    /*
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if(plugin.getStaffMode().inStaffMode(event.getPlayer())){
                if(event.getPlayer().getItemInHand() != null && !event.getPlayer().getItemInHand().getType().equals(Material.AIR)){
                    Material itemInHand = event.getPlayer().getItemInHand().getType();

                    if(itemInHand.equals(plugin.getStaffMode().getStaffItemData().getBook().getType())){
                        if(!(event.getRightClicked() instanceof Player)){
                            return;
                        }

                        Player player = (Player) event.getRightClicked();

                        event.getPlayer().sendMessage(plugin.getMessages().getString("Interaction-StaffMode-InvOpen")
                                .replace("{player}", player.getName()));

                        plugin.getInvseeManager().openInventory(event.getPlayer(), player);
                    }
                }
            }
        });
    }
     */

}
