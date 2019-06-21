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
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class SEMListener implements Listener{

    private final static long TPS_CLEAR_DELAY = TimeUnit.SECONDS.toMillis(5L);
    private final HCF plugin;

    private long lastTPSRun = 0L;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCreatureSpawn(CreatureSpawnEvent event){
        if(shouldClear()){
            for(Entity entity : plugin.getServer().getWorlds().get(0).getEntities()){
                if(entity instanceof Player || entity instanceof Projectile || entity instanceof ItemFrame ||
                        entity instanceof Cow || entity instanceof Pig || entity instanceof Minecart || entity instanceof Chicken){
                    continue;
                }

                if(entity instanceof Item){
                    Item item = (Item) entity;

                    if(item.getItemStack() != null && item.getItemStack().getType().name().contains("DIAMOND") && !item.getItemStack().getEnchantments().isEmpty()){
                        continue;
                    }
                }

                entity.remove();
            }
        }
    }

    private boolean shouldClear(){
        if(!plugin.getSOTWManager().isActive() && plugin.getServer().spigot().getTPS()[0] <= 18 &&
                lastTPSRun + TPS_CLEAR_DELAY < System.currentTimeMillis()){
            lastTPSRun = System.currentTimeMillis();
            return true;
        }

        int onlinePlayers = plugin.getServer().getOnlinePlayers().size();
        int entities = plugin.getServer().getWorlds().get(0).getEntities().size() - onlinePlayers;

        return onlinePlayers >= 200 && entities >= 3500 || onlinePlayers >= 100 && entities >= 4000;
    }

}
