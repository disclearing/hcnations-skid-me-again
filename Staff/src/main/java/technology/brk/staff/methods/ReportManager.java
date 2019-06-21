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

package technology.brk.staff.methods;

import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import technology.brk.staff.Staff;
import technology.brk.staff.util.HiddenStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Deprecated
public class ReportManager{//TODO Completely rewrite because ew

    private int id;

    private final Map<Integer, Inventory> pages = new HashMap<>();
    private final Map<Integer, Report> reports = new HashMap<>();

    private final Map<UUID, Integer> openPages = new HashMap<>();
    private final Map<UUID, Long> cooldown = new HashMap<>();

    private final Set<String> reportedSelf = new HashSet<>();

    public final ButtonItemData buttonItemData;
    private final Staff plugin;

    public ReportManager(Staff plugin){
        this.plugin = plugin;
        buttonItemData = new ButtonItemData();
    }

    public void handleLogout(Player player){
        cooldown.remove(player.getUniqueId());
        reportedSelf.remove(player.getName().toLowerCase());
        openPages.remove(player.getUniqueId());
    }

    public void startCooldown(Player player){
        cooldown.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean inCooldown(Player player){
        return cooldown.containsKey(player.getUniqueId());
    }

    public long getCooldown(Player player){
        return cooldown.get(player.getUniqueId());
    }
    
    public Inventory getGUI(int page){
        return pages.get(page);
    }

    public void addReportedSelf(String name){
        reportedSelf.add(name.toLowerCase());
    }

    public boolean hasReportedSelf(String name){
        return reportedSelf.contains(name.toLowerCase());
    }

    public void openPage(UUID uuid, int page){
        openPages.put(uuid, page);
    }

    public boolean hasPageOpen(UUID uuid){
        return openPages.containsKey(uuid);
    }

    public int getPage(UUID uuid){
        return openPages.get(uuid);
    }

    public void closePage(UUID uuid){
        openPages.remove(uuid);
    }

    public Report buildReport(String player, String reporter, String reason){
        Report report = new Report();

        report.setId(id++);

        report.setPlayer(player);
        report.setReporter(reporter);
        report.setReason(reason);

        report.buildItem();

        return report;
    }

    public void addReport(Report report){
        reports.put(report.getId(), report);
        buildInventories();
    }

    public void removeReport(int reportId){
        Report report = reports.get(reportId);
        reportedSelf.remove(report.getReporter().toLowerCase());
        reports.remove(reportId);
        buildInventories();
    }

    public void buildInventories(){
        Inventory inventory = Bukkit.getServer().createInventory(null, 9 * 3,
                ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("Reports.Inventory.Name")
                                .replace("{pageNumber}", String.valueOf(1))));

        int items = 0;
        int page = 1;

        for(int i : reports.keySet()){
            Report report = reports.get(i);

            if(items >= 25){
                if(reports.containsKey(i + 1)){
                    inventory.setItem(26, buttonItemData.getActiveButton());
                }else{
                    inventory.setItem(26, buttonItemData.getInactiveButton());
                }
                inventory.setItem(25, buttonItemData.getClearItem());

                pages.put(page, inventory);
                page++;
                items = 0;

                inventory = Bukkit.getServer().createInventory(null, 9 * 3,
                        ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString("Reports.Inventory.Name")
                                        .replace("{pageNumber}", String.valueOf(page))));
            }

            report.setPage(page);
            reports.put(report.getId(), report);

            inventory.setItem(items, report.getItem());
            items++;
        }
        System.out.println(Arrays.toString(inventory.getContents()));

        if(inventory.getContents().length > 0){
            inventory.setItem(26, buttonItemData.getInactiveButton());
            inventory.setItem(25, buttonItemData.getClearItem());
            pages.put(page, inventory);
        }
    }

    public int getPagesCount() {
        return pages.size();
    }

    public Report getReport(int integer){
        return reports.get(integer);
    }

    public boolean isEmpty(){
        return reports.isEmpty();
    }

    public void clear(){
        reportedSelf.clear();
        reports.clear();
        pages.clear();

        for(Map.Entry<UUID, Integer> uuid : openPages.entrySet()){
            Player player = plugin.getServer().getPlayer(uuid.getKey());
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Reports cleared.");
        }
        openPages.clear();
    }

    @Data
    public class ButtonItemData{
        private ItemStack activeButton;
        private ItemStack inactiveButton;
        private ItemStack clearItem;

        private Material reportType;

        public ButtonItemData(){
            setActiveButton(buildItem("Reports.Active-Button"));
            setInactiveButton(buildItem("Reports.Inactive-Button"));
            setClearItem(buildItem("Reports.Clear-Button"));

            try{
                reportType = Material.valueOf(plugin.getConfig().getString("Reports.ReportItem.Item"));
            }catch(Exception exception){
                throw new RuntimeException("Invalid Material: " + plugin.getConfig().getString("Reports.ReportItem.Item"));
            }
        }

        public ItemStack buildItem(String path){
            ItemStack itemStack;
            Material material;

            int amount;
            int data;

            try{
                material = Material.valueOf(plugin.getConfig().getString(path + ".Item"));
            }catch(Exception exception){
                throw new RuntimeException("Invalid Material: " + plugin.getConfig().getString(path + ".Item"));
            }

            itemStack = new ItemStack(material);

            amount = plugin.getConfig().getInt(path + ".Amount");
            data = plugin.getConfig().getInt(path + ".Data");

            if(amount > 0){
                itemStack.setAmount(amount);
            }

            if(data > 0){
                itemStack.setDurability((short) data);
            }

            ItemMeta itemMeta = itemStack.getItemMeta();

            String itemName = plugin.getConfig().getString(path + ".Name");

            if(itemName != null){
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName));
            }

            List<String> lore = plugin.getConfig().getStringList(path + ".Lore");
            List<String> colouredLore = new ArrayList<>();

            if(lore != null){
                colouredLore.addAll(plugin.getConfig().getStringList(path + ".Lore").stream().map(i -> ChatColor.translateAlternateColorCodes('&', i)).collect(Collectors.toList()));
            }

            itemMeta.setLore(colouredLore);

            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }
    }

    @Data
    public class Report{
        private int id;

        private int page = 0;
        private int slot;

        private String reporter;
        private String player;

        private String reason;

        private ItemStack item;

        public void buildItem(){
            Material material;

            try{
                material = Material.valueOf(plugin.getConfig().getString("Reports.ReportItem.Item"));
            }catch(Exception exception){
                throw new RuntimeException("Error while building report time - Invalid item");
            }

            ItemStack itm = new ItemStack(material,
                    plugin.getConfig().getInt("Reports.ReportItem.Amount"));

            int data = plugin.getConfig().getInt("Reports.ReportItem.Data");

            if(data > 0){
                itm.setDurability((short) data);
            }

            String name = plugin.getConfig().getString("Reports.ReportItem.Name")
                    .replace("{report-id}", String.valueOf(id))
                    .replace("{player}", player)
                    .replace("{reporter}", reporter)
                    .replace("{reason}", reason);

            ItemMeta itemMeta = itm.getItemMeta();

            if(StringUtils.isNotEmpty(name)){
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            }

            List<String> lore = plugin.getConfig().getStringList("Reports.ReportItem.Lore")
                    .stream()
                    .map(i -> ChatColor.translateAlternateColorCodes('&', i
                            .replace("{report-id}", String.valueOf(id))
                            .replace("{player}", player)
                            .replace("{reporter}", reporter)
                            .replace("{reason}", reason)))
                    .collect(Collectors.toList());

            lore.add(HiddenStringUtils.encodeString("ReportID: " + String.valueOf(id)));
            itemMeta.setLore(lore);
            itm.setItemMeta(itemMeta);

            item = itm;
        }

        public ItemStack getItem(){
            return item;
        }
    }
}
