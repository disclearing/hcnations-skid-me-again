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

package org.hcgames.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Stats extends JavaPlugin implements Listener{

    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Integer> deaths = new HashMap<>();

    @Override
    public void onEnable(){
        saveConfig();

        if(getConfig().contains("Kills")){
            for(String key : getConfig().getConfigurationSection("Kills").getKeys(false)){
                kills.put(UUID.fromString(key), getConfig().getInt("Kills." + key));
            }
        }

        if(getConfig().contains("Deaths")){
            for(String key : getConfig().getConfigurationSection("Deaths").getKeys(false)){
                deaths.put(UUID.fromString(key), getConfig().getInt("Deaths." + key));
            }
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable(){
        for(Map.Entry<UUID, Integer> entry : kills.entrySet()){
            getConfig().set("Kills." + entry.getKey().toString(), entry.getValue());
        }

        for(Map.Entry<UUID, Integer> entry : deaths.entrySet()){
            getConfig().set("Deaths." + entry.getKey().toString(), entry.getValue());
        }

        saveConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event){
        int deaths = this.deaths.getOrDefault(event.getEntity().getUniqueId(), 0);
        this.deaths.put(event.getEntity().getUniqueId(), deaths + 1);

        if(event.getEntity().getKiller() != null && !event.getEntity().getKiller().equals(event.getEntity())){
            Player killer = event.getEntity().getKiller();
            int kills = this.kills.getOrDefault(killer.getUniqueId(), 0);
            this.kills.put(killer.getUniqueId(), kills + 1);
        }
    }

    public int getKills(UUID uuid){
        return kills.getOrDefault(uuid, 0);
    }

    public int getDeaths(UUID uuid){
        return deaths.getOrDefault(uuid, 0);
    }
}
