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

package org.hcgames.kmextra.listener;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.hcgames.kmextra.KMExtra;
import org.hcgames.kmextra.Kit;
import org.hcgames.kmextra.util.IconMenu;
import org.hcgames.kmextra.util.ItemBuilder;

import java.util.HashMap;
import java.util.Map;

public class KitSelectorListener implements Listener{

    private final Map<Integer, String> slotToKit = new HashMap<>();
    private final ItemStack matchItem;
    private final IconMenu menu;

    private final KMExtra plugin;

    public KitSelectorListener(KMExtra plugin){
        matchItem = ItemBuilder.buildItem(plugin, "selector.match_item");
        this.plugin = plugin;

        menu = new IconMenu(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("selector.title")), plugin.getConfig().getInt("selector.rows") * 9, new IconMenu.OptionClickEventHandler(){
            @Override
            public void onOptionClick(IconMenu.OptionClickEvent event){
                if(slotToKit.containsKey(event.getPosition())){
                    Kit kit = plugin.getKits().get(slotToKit.get(event.getPosition()));
                    kit.apply(event.getPlayer(), true);
                }
            }
        }, plugin);

        for(String kitName : plugin.getConfig().getConfigurationSection("kits").getKeys(false)){
            if(plugin.getConfig().contains("kits." + kitName + ".kitselector")){
                menu.setOption(plugin.getConfig().getInt("kits." + kitName + ".kitselector.slot"), ItemBuilder.buildItem(plugin, "kits." + kitName + ".kitselector.icon"));
                slotToKit.put(plugin.getConfig().getInt("kits." + kitName + ".kitselector.slot"), kitName);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
            Player player = event.getPlayer();

            if(player.getItemInHand() != null && player.getItemInHand().equals(matchItem)){
                if(plugin.getFactions().getFactionManager().getFactionAt(player.getLocation()).isSafezone()){
                    menu.open(player);
                }else{
                    player.setItemInHand(null);
                }
            }
        }
    }
}
