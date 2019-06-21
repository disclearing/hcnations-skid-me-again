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

package org.hcgames.kmextra;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Kit{

    private final Map<Integer, ItemStack> inventory;

    private final ItemStack helmet;
    private final ItemStack chest;
    private final ItemStack leggings;
    private final ItemStack boots;

    private final ItemStack fillItem;

    public void apply(Player player, boolean clear){
        PlayerInventory inventory = player.getInventory();

        if(clear){
            inventory.clear();
        }

        inventory.setHelmet(helmet);
        inventory.setChestplate(chest);
        inventory.setLeggings(leggings);
        inventory.setBoots(boots);

        for(Map.Entry<Integer, ItemStack> entry : this.inventory.entrySet()){
            inventory.setItem(entry.getKey(), entry.getValue());
        }

        if(fillItem != null){
            int current = 0;
            for(ItemStack item : player.getInventory().getContents()){
                if(item == null) inventory.setItem(current, fillItem);
                current++;
            }
        }
    }

}
