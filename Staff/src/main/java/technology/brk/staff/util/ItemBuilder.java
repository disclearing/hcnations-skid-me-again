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

package technology.brk.staff.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder{

    public static ItemStack buildItem(Plugin plugin, String path){
        ItemStack item;
        Material material;

        try{
            material = Material.valueOf(plugin.getConfig().getString(path + ".item"));
        }catch(Exception exception){
            throw new RuntimeException("Invalid Material: " + plugin.getConfig().getString(path + ".item"));
        }

        item = new ItemStack(material);

        if(plugin.getConfig().contains(path + ".amount")){
            item.setAmount(plugin.getConfig().getInt(path + ".amount"));
        }

        if(plugin.getConfig().contains(path + ".data")){
            item.setDurability((short) plugin.getConfig().getInt(path + ".data"));
        }

        ItemMeta meta = item.getItemMeta();

        if(plugin.getConfig().contains(path + ".name")){
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(path + ".name")));
        }

        if(plugin.getConfig().contains(path + ".lore")){
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.addAll(plugin.getConfig().getStringList(path + ".lore").stream().map(i -> ChatColor.translateAlternateColorCodes('&', i)).collect(Collectors.toList()));
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

}
