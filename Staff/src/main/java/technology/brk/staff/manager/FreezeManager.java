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

package technology.brk.staff.manager;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import technology.brk.staff.Staff;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FreezeManager{

    private final Map<UUID, FreezeState> frozenUsers = new HashMap<>();
    private final ImmutableList<String> commands;

    @Getter
    private final Inventory inventory;

    public FreezeManager(Staff plugin){
        inventory = plugin.getServer().createInventory(null, plugin.getConfig().getInt("freeze.inventory.rows") * 9,
                ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("freeze.inventory.name")));
        commands = ImmutableList.copyOf(plugin.getConfig().getStringList("freeze.allowed_commands"));

        for(String slotAsString : plugin.getConfig().getConfigurationSection("freeze.inventory.items").getKeys(false)){
            Integer slot = Integer.valueOf(slotAsString);
            ItemStack item = new ItemStack(Material.valueOf(plugin.getConfig().getString("freeze.inventory.items." + slotAsString + ".item")),
                    plugin.getConfig().getInt("freeze.inventory.items." + slotAsString + ".amount"));

            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("freeze.inventory.items." + slotAsString + ".name")));
            meta.setLore(plugin.getConfig().getStringList("freeze.inventory.items." + slotAsString + ".lore")
                    .stream().map(i -> ChatColor.translateAlternateColorCodes('&', i)).collect(Collectors.toList()));
            item.setItemMeta(meta);

            inventory.setItem(slot, item);
        }

        ItemStack fill = new ItemStack(Material.valueOf(plugin.getConfig().getString("freeze.inventory.fill.item")), plugin.getConfig().getInt("freeze.inventory.fill.amount"));
        fill.setDurability((short) plugin.getConfig().getInt("freeze.inventory.fill.data", 0));

        ItemMeta meta = fill.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("freeze.inventory.fill.name")));
        meta.setLore(plugin.getConfig().getStringList("freeze.inventory.fill.lore")
                .stream().map(i -> ChatColor.translateAlternateColorCodes('&', i)).collect(Collectors.toList()));
        fill.setItemMeta(meta);

        IntStream.rangeClosed(0, inventory.getSize() - 1).forEach(i -> {
            if(inventory.getItem(i) == null){
                inventory.setItem(i, fill);
            }
        });
    }

    public void setState(Player player, FreezeState state){
        if(state == FreezeState.NONE && frozenUsers.containsKey(player.getUniqueId())){
            frozenUsers.remove(player.getUniqueId());
            player.setWalkSpeed(0.2F);
            player.removePotionEffect(PotionEffectType.JUMP);
            return;
        }

        if(state == FreezeState.INVENTORY){
            player.closeInventory();
            player.openInventory(inventory);
        }

        player.setWalkSpeed(0.0F);
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128), true);

        frozenUsers.put(player.getUniqueId(), state);
    }

    public FreezeState getState(Player player){
        return frozenUsers.getOrDefault(player.getUniqueId(), FreezeState.NONE);
    }

    public boolean isFrozen(Player player){
        return getState(player) != FreezeState.NONE;
    }

    public boolean isCommandAllowed(String command){
        return commands.contains(command);
    }

    public enum FreezeState{
        INVENTORY,
        INVENTORY_OFFLINE,
        NORMAL,
        NONE
    }

}
