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

package technology.brk.staff.manager.invsee;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_7_R4.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.PlayerInventory;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftItem;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import technology.brk.staff.Staff;
import technology.brk.staff.util.EnchantGlow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//TODO: Disable editing of online inventory, introduced it later
public class OpenedInventory{

    @Getter
    private final Inventory inventory;
    private final Staff plugin;

    private Set<Player> viewers = new HashSet<>(); //TODO: Player set?!!!

    @Getter
    private final UUID owner;

    @Getter @Setter
    private boolean offline;

    //Only used when viewing offline inventory.
    private CraftInventoryPlayer offlineInventory;
    private NBTTagCompound compound;
    private File userFile;

    @Getter @Setter
    private boolean clearAuthorized;
    private int pendingClearTaskId;

    private OpenedInventory(Staff plugin, UUID uuid, String name){
        this.plugin = plugin;
        this.owner = uuid;

        String title = plugin.getInvseeManager().getData().getDefaultInventoryTitle().replace("{player}", name);
        if(title.length() > 32){
            title = title.substring(32);
        }

        inventory = plugin.getServer().createInventory(null, 5 * 9, title);
    }

    static OpenedInventory newOfflineInventory(Staff plugin, UUID player, String playerName){
        OpenedInventory inventory = new OpenedInventory(plugin, player, playerName);
        inventory.offline = true;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            inventory.userFile = new File(plugin.getServer().getWorlds().get(0).getWorldFolder() + "/playerdata/" + player.toString() + ".dat");

            if(inventory.userFile.exists()){
                try{
                    inventory.compound = NBTCompressedStreamTools.a(new FileInputStream(inventory.userFile));
                    PlayerInventory playerInventory = new PlayerInventory(null);
                    NBTTagList nbttaglist = inventory.compound.getList("Inventory", 10);

                    //Taken from PlayerInventory
                    for (int i = 0; i < nbttaglist.size(); ++i) {
                        net.minecraft.server.v1_7_R4.NBTTagCompound nbttagcompound = nbttaglist.get(i);
                        int j = nbttagcompound.getByte("Slot") & 255;
                        net.minecraft.server.v1_7_R4.ItemStack itemstack = net.minecraft.server.v1_7_R4.ItemStack.createStack(nbttagcompound);

                        if (itemstack != null) {
                            if (j >= 0 && j < playerInventory.getContents().length) {
                                playerInventory.getContents()[j] = itemstack;
                            }

                            if (j >= 100 && j < playerInventory.getContents().length + 100) {
                                playerInventory.getArmorContents()[j - 100] = itemstack;
                            }
                        }
                    }

                    inventory.offlineInventory = new CraftInventoryPlayer(playerInventory);
                }catch(FileNotFoundException e){
                    e.printStackTrace();
                }

                IntStream.range(0, inventory.offlineInventory.getContents().length).forEach(slot -> inventory.inventory.setItem(slot, inventory.offlineInventory.getContents()[slot]));
                IntStream.range(0, 4).forEach(slot -> {
                    ItemStack item = inventory.offlineInventory.getArmorContents()[slot];

                    if(item == null || item.getType().equals(Material.AIR)){
                        inventory.inventory.setItem(39 - slot, plugin.getInvseeManager().getData().getEmptyArmorItem().clone());
                    }else{
                        inventory.inventory.setItem(39 - slot, item);
                    }
                });

                inventory.inventory.setItem(44, plugin.getInvseeManager().getData().getClearInventoryItem());
            }
        });

        return inventory;
    }

    static OpenedInventory newOnlineInventory(Staff plugin, Player player){
        OpenedInventory inventory = new OpenedInventory(plugin, player.getUniqueId(), player.getName());
        inventory.inventory.setItem(44, plugin.getInvseeManager().getData().getClearInventoryItem());
        inventory.updateDisplayedContents(0);
        inventory.updateDisplayedContents(1);
        inventory.updateDisplayedContents(2);
        inventory.updateDisplayedContents(3);
        return inventory;
    }

    public void becomeOnline(Player player){
        if(offline){
            offline = false;
            player.getInventory().setContents(Arrays.copyOfRange(inventory.getContents(), 0, 36));
            player.getInventory().setArmorContents(Arrays.copyOfRange(inventory.getContents(), 36, 40));
        }
    }

    public void updateDisplayedContents(int i){
        if(!offline){
            Player player = plugin.getServer().getPlayer(owner);

            if(player != null){
                switch(i){
                    case 0:
                        IntStream.range(0, player.getInventory().getContents().length).forEach(slot -> inventory.setItem(slot, player.getInventory().getContents()[slot]));
                        break;
                    case 1:
                        IntStream.range(0, 4).forEach(slot -> {
                            ItemStack item = player.getInventory().getArmorContents()[slot];

                            if(item == null || item.getType().equals(Material.AIR)){
                                inventory.setItem(39 - slot, plugin.getInvseeManager().getData().getEmptyArmorItem().clone());
                            }else{
                                inventory.setItem(39 - slot, item);
                            }
                        });
                        break;
                    case 2:
                        ItemStack potionItem = plugin.getInvseeManager().getData().getPotionEffectItem().clone();

                        if(!player.getActivePotionEffects().isEmpty()){
                            ItemMeta meta = potionItem.getItemMeta();
                            List<String> newLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                            newLore.addAll(player.getActivePotionEffects().stream().map(effect -> plugin.getMessages().getString("Commands-Invsee-PotionLoreItem")//TODO: Replace
                                    .replace("{potionEffect}", WordUtils.capitalize(effect.getType().getName().replace("_", " ").toLowerCase()))
                                    .replace("{potionLevel}", String.valueOf(effect.getAmplifier() + 1))).collect(Collectors.toList()));
                            meta.setLore(newLore);
                            potionItem.setItemMeta(meta);
                        }

                        inventory.setItem(43, potionItem);
                        break;
                    case 3:
                        ItemStack healthItem = plugin.getInvseeManager().getData().getHealthItem();
                        int health = ((Double)player.getHealth()).intValue();

                        if(health <= 0){
                            health = 1;
                        }

                        healthItem.setAmount(health);
                        inventory.setItem(42, healthItem);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void addViewer(Player viewer){
        viewer.closeInventory();
        viewer.openInventory(inventory);
        viewers.add(viewer);
        plugin.getInvseeManager().viewers.add(viewer.getUniqueId());
    }

    public void removeViewer(Player viewer){
        viewers.remove(viewer);
        plugin.getInvseeManager().viewers.remove(viewer.getUniqueId());

        if(viewers.isEmpty()){
            remove();
        }
    }

    Set<Player> getViewers(){
        return viewers;
    }

    public void remove(){
        if(offline){
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                net.minecraft.server.v1_7_R4.ItemStack[] nmsItemArray = new net.minecraft.server.v1_7_R4.ItemStack[35];
                ItemStack[] itemArray = Arrays.copyOfRange(inventory.getContents(), 0, 35);
                IntStream.range(0, 35).forEach(i -> nmsItemArray[i] = itemArray[i] == null ? null : CraftItemStack.asNMSCopy(itemArray[i]));
                offlineInventory.getInventory().items = nmsItemArray;

                net.minecraft.server.v1_7_R4.ItemStack[] nmsArmorItemArray = new net.minecraft.server.v1_7_R4.ItemStack[35];
                ItemStack[] itemArmorArray = Arrays.copyOfRange(inventory.getContents(), 36, 39);
                IntStream.range(0, 3).forEach(i -> {
                    ItemStack found = itemArmorArray[i];

                    if(found != null && found.equals(plugin.getInvseeManager().getData().getEmptyArmorItem())){
                        found = null;
                    }

                    nmsArmorItemArray[i] = CraftItemStack.asNMSCopy(found);
                });
                offlineInventory.getInventory().armor = nmsArmorItemArray;

                compound.set("Inventory", offlineInventory.getInventory().a(new NBTTagList()));
                if(userFile.exists()){
                    try{
                        NBTCompressedStreamTools.a(this.compound, new FileOutputStream(userFile));
                    }catch(FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
            });
        }

        if(!viewers.isEmpty()){
            viewers.forEach((player) -> {
                player.closeInventory();
                plugin.getInvseeManager().viewers.remove(player.getUniqueId());
            });
            viewers.clear();
        }

        if(pendingClearTaskId > 0){
            plugin.getServer().getScheduler().cancelTask(pendingClearTaskId);
        }
        plugin.getInvseeManager().inventories.remove(owner);
    }

    public void triggerClear(Player cause){
        if(clearAuthorized){
            plugin.getLogger().info("Triggered clear on " + owner.toString() + " by " + cause.getName());
            if(isOffline()){
                offlineInventory.clear();
                offlineInventory.getInventory().armor = new net.minecraft.server.v1_7_R4.ItemStack[4];

                IntStream.range(0, offlineInventory.getContents().length).forEach(slot -> inventory.setItem(slot, null));
                IntStream.range(0, 4).forEach(slot -> inventory.setItem(39 - slot, plugin.getInvseeManager().getData().getEmptyArmorItem().clone()));
            }else{
                Player player = plugin.getServer().getPlayer(owner);

                if(player != null){
                    player.getInventory().clear();
                    player.getInventory().setArmorContents(null);
                }

                IntStream.range(0, inventory.getContents().length).forEach(slot -> inventory.setItem(slot, null));
                IntStream.range(0, 4).forEach(slot -> inventory.setItem(39 - slot, plugin.getInvseeManager().getData().getEmptyArmorItem().clone()));
            }

            clearAuthorized = false;
            EnchantGlow.removeGlow(inventory.getItem(44));
            if(pendingClearTaskId > 0){
                plugin.getServer().getScheduler().cancelTask(pendingClearTaskId);
                pendingClearTaskId = 0;
            }
        }else{
            clearAuthorized = true;
            EnchantGlow.addGlow(inventory.getItem(44));

            pendingClearTaskId = plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                clearAuthorized = false;
                EnchantGlow.removeGlow(inventory.getItem(44));
            }, (60L * 20)).getTaskId();
        }
    }

}
