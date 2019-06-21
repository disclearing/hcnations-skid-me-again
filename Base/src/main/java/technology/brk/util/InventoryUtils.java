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

package technology.brk.util;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;
import java.util.Set;

public final class InventoryUtils {
    public static final int DEFAULT_INVENTORY_WIDTH = 9;
    public static final int MINIMUM_INVENTORY_HEIGHT = 1;
    public static final int MINIMUM_INVENTORY_SIZE = 9;
    public static final int MAXIMUM_INVENTORY_HEIGHT = 6;
    public static final int MAXIMUM_INVENTORY_SIZE = 54;
    public static final int MAXIMUM_SINGLE_CHEST_SIZE = 27;
    public static final int MAXIMUM_DOUBLE_CHEST_SIZE = 54;

    private InventoryUtils() {
    }

    public static ItemStack[] deepClone(ItemStack[] origin){
        Preconditions.checkNotNull(origin, "Origin cannot be null");
        ItemStack[] cloned = new ItemStack[origin.length];
        for (int i = 0; i < origin.length; ++i) {
            ItemStack next = origin[i];
            cloned[i] = next == null ? null : next.clone();
        }
        return cloned;
    }

    public static int getSafestInventorySize(int initialSize) {
        return (initialSize + 8) / 9 * 9;
    }

    public static void removeItem(Inventory inventory, Material type, short data, int quantity) {
        ItemStack[] contents = inventory.getContents();
        boolean compareDamage = type.getMaxDurability() == 0;
        block0 : for (int i = quantity; i > 0; --i) {
            for (ItemStack content : contents) {
                if (content == null || content.getType() != type || compareDamage && content.getData().getData() != data) continue;
                if (content.getAmount() <= 1) {
                    inventory.removeItem(content);
                    continue block0;
                }
                content.setAmount(content.getAmount() - 1);
                continue block0;
            }
        }
    }

    public static int countAmount(Inventory inventory, Material type, short data) {
        ItemStack[] contents = inventory.getContents();
        boolean compareDamage = type.getMaxDurability() == 0;
        int counter = 0;
        for (ItemStack item : contents) {
            if (item == null || item.getType() != type || compareDamage && item.getData().getData() != data) continue;
            counter += item.getAmount();
        }
        return counter;
    }

    public static boolean isEmpty(Inventory inventory) {
        return InventoryUtils.isEmpty(inventory, true);
    }

    public static boolean isEmpty(Inventory inventory, boolean checkArmour) {
        ItemStack[] contents;
        boolean result = true;
        for (ItemStack content2 : contents = inventory.getContents()) {
            if (content2 == null || content2.getType() == Material.AIR) continue;
            result = false;
            break;
        }
        if (!result) {
            return false;
        }
        if (checkArmour && inventory instanceof PlayerInventory) {
            for (ItemStack content2 : contents = ((PlayerInventory)inventory).getArmorContents()) {
                if (content2 == null || content2.getType() == Material.AIR) continue;
                result = false;
                break;
            }
        }
        return result;
    }

    public static boolean clickedTopInventory(InventoryDragEvent event) {
        InventoryView view = event.getView();
        Inventory topInventory = view.getTopInventory();
        if (topInventory == null) {
            return false;
        }
        boolean result = false;
        Set<Map.Entry<Integer, ItemStack>> entrySet = event.getNewItems().entrySet();
        int size = topInventory.getSize();
        for (Map.Entry<Integer, ItemStack> entry : entrySet) {
            if (entry.getKey() >= size) continue;
            result = true;
            break;
        }
        return result;
    }
}

