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
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import technology.brk.staff.Staff;
import technology.brk.staff.util.ItemBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InvseeManager{

    //TODO: Map<UUIDViewer, UUIDOpenedInventory>
    final Map<UUID, OpenedInventory> inventories = new ConcurrentHashMap<>(); //TODO: Do those really have to be concurrent? :/
    final Set<UUID> viewers = new HashSet<>();

    @Getter
    private final Data data;

    public InvseeManager(Staff plugin){
        data = new Data(plugin);
    }

    public OpenedInventory newOfflineInventory(Staff plugin, UUID player, String playerName){
        OpenedInventory inventory = OpenedInventory.newOfflineInventory(plugin, player, playerName);
        inventories.put(player, inventory);
        return inventory;
    }

    public OpenedInventory newOnlineInventory(Staff plugin, Player player){
        OpenedInventory inventory = OpenedInventory.newOnlineInventory(plugin, player);
        inventories.put(player.getUniqueId(), inventory);
        return inventory;
    }

    public OpenedInventory getInventory(UUID user){
        return inventories.get(user);
    }

    public boolean beingWatched(UUID user){
        return inventories.containsKey(user);
    }

    public void remove(UUID user){
        if(inventories.containsKey(user)){
            inventories.get(user).remove();
        }
    }

    public OpenedInventory getWatching(Player player){
        for(OpenedInventory inventory : inventories.values()){
            if(inventory.getViewers().contains(player)){
                return inventory;
            }
        }

        return null;
    }

    public boolean isViewer(Player player){
        return viewers.contains(player.getUniqueId());
    }

    @Getter
    public class Data{

        private final String defaultInventoryTitle;

        private final ItemStack emptyArmorItem;
        private final ItemStack clearInventoryItem;
        private final ItemStack healthItem;
        private final ItemStack potionEffectItem;

        public Data(Plugin plugin){
            defaultInventoryTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("invsee.title"));

            emptyArmorItem = ItemBuilder.buildItem(plugin, "invsee.emptyarmoritem");
            clearInventoryItem = ItemBuilder.buildItem(plugin, "invsee.clearinventoryitem");
            healthItem = ItemBuilder.buildItem(plugin, "invsee.healthitem");
            potionEffectItem = ItemBuilder.buildItem(plugin, "invsee.potioneffectitem");
        }

    }
}
