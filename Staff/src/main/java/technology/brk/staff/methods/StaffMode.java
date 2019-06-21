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
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import technology.brk.staff.Staff;
import technology.brk.staff.util.EnchantGlow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Deprecated //TODO: Rewrite cus buggy, broken & ew
public class StaffMode {

    @Getter private StaffItemData staffItemData;

    @Getter private int onlineStaffMembers;

    private HashMap<UUID, Boolean> staffMode = new HashMap<>();

    private final Staff plugin;

    public StaffMode(Staff plugin){
        this.plugin = plugin;
        staffItemData = new StaffItemData();
    }

    public void enableStaffMode(Player player){
        if(staffMode.containsKey(player.getUniqueId())){
            return;
        }

        staffMode.put(player.getUniqueId(), true);
        player.setGameMode(GameMode.CREATIVE);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.getInventory().setItem(0, staffItemData.getBook());
        player.getInventory().setItem(1, staffItemData.getDye());

        ItemStack feather = staffItemData.getFeather().clone();
        if(plugin.getVanishManager().isVanished(player)){
            EnchantGlow.addGlow(feather);
        }
        player.getInventory().setItem(2, feather);

        player.setMetadata("staffmode", new FixedMetadataValue(plugin, true));
        player.getInventory().setItem(6, staffItemData.getCompass());
        player.getInventory().setItem(7, staffItemData.getHead());
        player.getInventory().setItem(8, staffItemData.getIronBars());

        for(Player other : plugin.getServer().getOnlinePlayers()){
            player.showPlayer(other);
        }
    }

    public void disableStaffMode(Player player){
        if(staffMode.remove(player.getUniqueId()) != null){
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();

            staffMode.remove(player.getUniqueId());
            player.removeMetadata("staffmode", plugin);

            plugin.getIcefyreHook().setup(player);
        }
    }

    public void handleLogin(Player player){
        if(player.hasPermission("staff.autotoggle.staffmode")){
            plugin.getLogger().info("[StaffMode] Automatically enabling staff mode for " + player.getName());
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> enableStaffMode(player), 20L);
        }

        if(player.hasPermission("staff.staff")){
            onlineStaffMembers++;
            updateAll();
        }

    }

    public void handleLogout(Player player){
        disableStaffMode(player);

        if(player.hasPermission("staff.staff")){
            onlineStaffMembers--;
            updateAll();
        }
    }

    private void updateAll(){
        for(UUID uuid : staffMode.keySet()){
            Player player = Bukkit.getServer().getPlayer(uuid);

            if(player == null){
                return;
            }

            player.getInventory().setItem(7, staffItemData.getHead());
        }
    }

    public boolean inStaffMode(Player player){
        return inStaffMode(player.getUniqueId());
    }

    public boolean inStaffMode(UUID uuid){
        return staffMode.containsKey(uuid);
    }

    @Data
    public class StaffItemData{

        //This is used in the BlockPlaceListener
        private final List<Material> materialTypes = new ArrayList<>();

        private ItemStack book;
        private ItemStack dye;
        private ItemStack compass;
        private ItemStack head;
        private ItemStack ironBars;
        private ItemStack feather;
        private ItemStack pickaxe;

        public StaffItemData(){
            setBook(buildItem("StaffMode.InvSee"));
            setDye(buildItem("StaffMode.RandomPlayer"));
            setCompass(buildItem("StaffMode.Compass"));
            setHead(buildItem("StaffMode.StaffOnline"));
            setIronBars(buildItem("StaffMode.Reports"));
            setFeather(buildItem("StaffMode.Vanish"));
            setPickaxe(buildItem("StaffMode.Pickaxe"));
        }


        public ItemStack getHead(){
            ItemStack head = this.head.clone();

            head.setAmount(plugin.getStaffMode().getOnlineStaffMembers());

            return head;
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

            materialTypes.add(material);
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

            if(plugin.getConfig().contains(path + ".Lore")){
                itemMeta.setLore(plugin.getConfig().getStringList(path + ".Lore").stream().map(i -> ChatColor.translateAlternateColorCodes('&', i)).collect(Collectors.toList()));
            }
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }
    }

    @Data
    private class PlayerInv{//TODO
        private UUID UUID;
        private ItemStack[] invContents;
        private ItemStack[] armorContents;
        private float exp;
        private int level;
        private Collection<PotionEffect> effects;
        private GameMode gameMode;
        private Double health;
        private float saturation;
        private boolean flying;
    }
}
