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
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import technology.brk.staff.Staff;
import technology.brk.staff.methods.ReportManager;
import technology.brk.staff.util.HiddenStringUtils;

@RequiredArgsConstructor @Deprecated
public class InventoryClickListener implements Listener{

    private final Staff plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event){
        if((!(event.getWhoClicked() instanceof Player))){
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if(plugin.getStaffMode().inStaffMode(player.getUniqueId())){
            event.setResult(Event.Result.DENY);
        }

        if(plugin.getReportManager().hasPageOpen(player.getUniqueId())){
            event.setResult(Event.Result.DENY);
            if(event.getCurrentItem() != null && !event.getCurrentItem().getType().equals(Material.AIR)){
                if(event.getCurrentItem().equals(plugin.getReportManager().buttonItemData.getClearItem())){
                    if(plugin.getReportManager().isEmpty()){
                        ((Player) event.getWhoClicked()).sendMessage(ChatColor.RED + "No reports to clear.");
                    }else{
                        plugin.getReportManager().clear();
                    }
                }

                if(event.getCurrentItem().equals(plugin.getReportManager().buttonItemData.getActiveButton())){
                    int currentPage = plugin.getReportManager().getPage(player.getUniqueId()) + 1;

                    if(currentPage > plugin.getReportManager().getPagesCount()){
                        return;
                    }

                    Inventory nextPage = plugin.getReportManager().getGUI(currentPage);

                    if(nextPage != null){
                        player.closeInventory();

                        Bukkit.getServer().getScheduler().runTask(plugin, () -> Bukkit.getServer().dispatchCommand(player, "viewreports " + String.valueOf(currentPage)));
                    }
                }

                if(event.getCurrentItem().getType().equals(plugin.getReportManager().buttonItemData.getReportType())){
                    if(!event.getCurrentItem().hasItemMeta()){
                        return;
                    }

                    ItemMeta itemMeta = event.getCurrentItem().getItemMeta();

                    if(!itemMeta.hasLore()){
                        return;
                    }

                    String lastLine = itemMeta.getLore().get(itemMeta.getLore().size() - 1);

                    if(!HiddenStringUtils.hasHiddenString(lastLine)){
                        return;
                    }

                    ReportManager.Report report = plugin.getReportManager().getReport(Integer.valueOf(HiddenStringUtils.extractHiddenString(lastLine).replace("ReportID: ", "")));

                    if(report == null){
                        event.getInventory().setItem(event.getSlot(), null);
                        return;
                    }

                    if(event.isRightClick()){
                        event.getInventory().setItem(event.getSlot(), null);
                        plugin.getReportManager().removeReport(report.getId());
                        return;
                    }

                    if(event.isLeftClick()){
                        Player target = Bukkit.getServer().getPlayer(report.getPlayer());

                        if(target == null || !target.isOnline()){
                            event.getInventory().setItem(event.getSlot(), null);
                            plugin.getReportManager().removeReport(report.getId());
                            return;
                        }

                        player.sendMessage(plugin.getMessages().getString("Interaction-ViewReports-Teleport")
                                .replace("{player}", target.getName()));
                        player.teleport(target);
                    }
                }
            }
        }
    }

}
