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

package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.google.common.collect.ImmutableMultimap;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@RequiredArgsConstructor
public class OreCountListener implements Listener{

    public static final ImmutableMultimap<Material, ChatColor> ORES = ImmutableMultimap.<Material, ChatColor>builder().
            put(Material.EMERALD_ORE, ChatColor.GREEN).
            put(Material.DIAMOND_ORE, ChatColor.AQUA).
            put(Material.REDSTONE_ORE, ChatColor.RED).
            put(Material.GLOWING_REDSTONE_ORE, ChatColor.RED).
            put(Material.GOLD_ORE, ChatColor.GOLD).
            put(Material.IRON_ORE, ChatColor.GRAY).
            put(Material.LAPIS_ORE, ChatColor.BLUE).
            put(Material.COAL_ORE, ChatColor.DARK_GRAY).
            build();

    private final HCF plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();

        if(ORES.containsKey(event.getBlock().getType()) && player.getItemInHand() != null && !(player.getItemInHand().getEnchantments().containsKey(Enchantment.SILK_TOUCH))){
            Material type = event.getBlock().getType();
            if(type == Material.GLOWING_REDSTONE_ORE) type = Material.REDSTONE_ORE;

            plugin.getUserManager().getUser(player.getUniqueId()).incrementOre(type);
        }
    }
}
