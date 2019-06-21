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

package technology.brk.util.itemdb;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import technology.brk.base.BasePlugin;

import java.util.LinkedHashMap;
import java.util.Map;

public class ItemData
implements ConfigurationSerializable {
    private final Material material;
    private final short itemData;

    public ItemData(MaterialData data) {
        this(data.getItemType(), data.getData());
    }

    public ItemData(ItemStack stack) {
        this(stack.getType(), stack.getData().getData());
    }

    @Deprecated
    public ItemData(Material material, short itemData) {
        this.material = material;
        this.itemData = itemData;
    }

    public ItemData(Map<String, Object> map) {
        Object object = map.get("itemType");
        if (!(object instanceof String)) {
            throw new AssertionError("Incorrectly configurised");
        }
        this.material = Material.getMaterial((String)object);
        object = map.get("itemData");
        if (!(object instanceof Short)) {
            throw new AssertionError("Incorrectly configurised");
        }
        this.itemData = (Short)object;
    }

    public Map<String, Object> serialize() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("itemType", this.material.name());
        map.put("itemData", this.itemData);
        return map;
    }

    public Material getMaterial() {
        return this.material;
    }

    @Deprecated
    public short getItemData() {
        return this.itemData;
    }

    public String getItemName() {
        return BasePlugin.getPlugin().getItemDb().getName(new ItemStack(this.material, 1, this.itemData));
    }

    public static ItemData fromItemName(String string) {
        ItemStack stack = BasePlugin.getPlugin().getItemDb().getItem(string);
        return new ItemData(stack.getType(), stack.getData().getData());
    }

    public static ItemData fromStringValue(String value) {
        int firstBracketIndex = value.indexOf(40);
        if (firstBracketIndex == -1) {
            return null;
        }
        int otherBracketIndex = value.indexOf(41);
        if (otherBracketIndex == -1) {
            return null;
        }
        String itemName = value.substring(0, firstBracketIndex);
        String itemData = value.substring(firstBracketIndex + 1, otherBracketIndex);
        Material material = Material.getMaterial(itemName);
        return new ItemData(material, Short.parseShort(itemData));
    }

    public String toString() {
        return String.valueOf(this.material.name()) + "(" + String.valueOf(this.itemData) + ")";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ItemData itemData1 = (ItemData)o;
        if (this.itemData != itemData1.itemData) {
            return false;
        }
        return this.material == itemData1.material;
    }

    public int hashCode() {
        int result = this.material != null ? this.material.hashCode() : 0;
        result = 31 * result + this.itemData;
        return result;
    }
}

