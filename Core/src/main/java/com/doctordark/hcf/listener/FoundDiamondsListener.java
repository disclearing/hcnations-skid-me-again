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
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

@RequiredArgsConstructor
public class FoundDiamondsListener implements Listener{

    private final HCF plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event){
        if(event.getBlock().getType() == Material.DIAMOND_ORE){
            event.getBlock().setMetadata("fd", new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event){
        if(event.getBlock().getType() == Material.DIAMOND_ORE && !event.getBlock().hasMetadata("fd")){
            int count = 0;
            for(int x = -5; x < 5; x++){
                for(int y = -5; y < 5; y++){
                    for(int z = -5; z < 5; z++){
                        Block block = event.getBlock().getLocation().add(x, y, z).getBlock();
                        if(block.getType() == Material.DIAMOND_ORE && !block.hasMetadata("fd")){
                            block.setMetadata("fd", new FixedMetadataValue(plugin, null));
                            count++;
                        }
                    }
                }
            }

            plugin.getServer().broadcast("[FD] " + ChatColor.AQUA + event.getPlayer().getName() + " found " + count + " diamond" + (count == 1 ? "" : "s") + ".", "fd.alerts");
        }
    }
}
