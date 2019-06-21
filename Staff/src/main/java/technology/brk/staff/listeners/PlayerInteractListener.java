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

package technology.brk.staff.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import technology.brk.staff.Staff;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RequiredArgsConstructor @Deprecated
public class PlayerInteractListener implements Listener{

    private final Staff plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract2(PlayerInteractEvent event) {
        if(plugin.getStaffMode().inStaffMode(event.getPlayer())){
            if (!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR))){
                if(event.getPlayer().getItemInHand() != null && !event.getPlayer().getItemInHand().getType().equals(Material.AIR)){
                    Material itemInHand = event.getPlayer().getItemInHand().getType();

                    if(event.getPlayer().isSneaking()){
                        if(itemInHand.equals(plugin.getStaffMode().getStaffItemData().getDye().getType())){
                            event.getPlayer().getInventory().setItem(1, plugin.getStaffMode().getStaffItemData().getPickaxe());
                            return;
                        }else if(itemInHand.equals(plugin.getStaffMode().getStaffItemData().getPickaxe().getType())){
                            event.getPlayer().getInventory().setItem(1, plugin.getStaffMode().getStaffItemData().getDye());
                            return;
                        }
                    }
                }
                return;
            }

            if(event.getPlayer().getItemInHand() != null && !event.getPlayer().getItemInHand().getType().equals(Material.AIR)){
                Material itemInHand = event.getPlayer().getItemInHand().getType();

                if(itemInHand.equals(plugin.getStaffMode().getStaffItemData().getDye().getType())){
                    Player player = getRandomEntry(Staff.getOnlinePlayers());
                    if(player == null) return;
                    event.getPlayer().sendMessage(plugin.getMessages().getString("Interaction-StaffMode-RandomTeleport")
                            .replace("{player}", player.getName()));

                    Bukkit.getServer().getScheduler().runTask(plugin, () -> event.getPlayer().teleport(player.getLocation()));
                    return;
                }

                if(itemInHand.equals(plugin.getStaffMode().getStaffItemData().getPickaxe().getType())){
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        List<Player> below = Staff.getOnlinePlayers().stream().filter(player -> player.getLocation().getY() < 30).collect(Collectors.toList());

                        if(below.isEmpty()){
                            event.getPlayer().sendMessage(ChatColor.RED + "No players below y 30");
                            return;
                        }

                        Player player = getRandomEntry(below);
                        if(player == null) return;
                        event.getPlayer().sendMessage(plugin.getMessages().getString("Interaction-StaffMode-RandomTeleport")
                                .replace("{player}", player.getName()));
                        plugin.getServer().getScheduler().runTask(plugin, () -> event.getPlayer().teleport(player.getLocation()));
                    });
                    return;
                }

                if(itemInHand.equals(plugin.getStaffMode().getStaffItemData().getIronBars().getType())){
                    Bukkit.getServer().dispatchCommand(event.getPlayer(), "viewreports");
                    return;
                }

                if(itemInHand.equals(plugin.getStaffMode().getStaffItemData().getFeather().getType())){
                    boolean newState = !plugin.getVanishManager().isVanished(event.getPlayer());
                    plugin.getVanishManager().setVanished(event.getPlayer(), newState);
                    event.getPlayer().sendMessage(plugin.getMessages().getString("commands.vanish." + (newState ? "enabled" : "disabled")));
                    return;
                }

                if(itemInHand.equals(plugin.getStaffMode().getStaffItemData().getFeather().getType())){
                    boolean newState = !plugin.getVanishManager().isVanished(event.getPlayer());
                    plugin.getVanishManager().setVanished(event.getPlayer(), newState);
                    event.getPlayer().sendMessage(plugin.getMessages().getString("commands.vanish." + (newState ? "enabled" : "disabled")));
                }
            }
        }

    }

    public static <T> T getRandomEntry(Collection<T> from){
        int random = ThreadLocalRandom.current().nextInt(from.size());
        int current = 0;

        for(T t : from){
            if(current == random){
                return t;
            }
            current++;
        }

        return null;
    }
}
