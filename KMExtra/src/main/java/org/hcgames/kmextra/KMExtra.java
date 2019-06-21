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

import com.doctordark.hcf.HCF;
import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.kmextra.command.ChestCommand;
import org.hcgames.kmextra.command.StatsCommand;
import org.hcgames.kmextra.listener.FastPotListener;
import org.hcgames.kmextra.listener.JoinItemListener;
import org.hcgames.kmextra.listener.KitSelectorListener;
import org.hcgames.kmextra.listener.StatsListener;
import org.hcgames.kmextra.profile.Profile;
import org.hcgames.kmextra.profile.ProfileManager;
import org.hcgames.kmextra.util.CommandWrapper;
import org.hcgames.kmextra.util.ItemBuilder;
import org.hcgames.kmextra.util.Messages;
import org.hcgames.stats.Stats;

import java.util.HashMap;
import java.util.Map;

@Getter
public class KMExtra extends JavaPlugin{

    private Map<String, Kit> kits = new HashMap<>();

    private ProfileManager profileManager;
    private CommandWrapper commandWrapper;

    private HCFactions factions;
    private Stats stats;
    private HCF core;

    @Override
    public void onLoad(){
        ConfigurationSerialization.registerClass(Profile.class);
    }

    @Override
    public void onEnable(){
        saveDefaultConfig();
        Messages.load(this);

        profileManager = new ProfileManager(this);
        commandWrapper = new CommandWrapper(this);

        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("chest").setExecutor(new ChestCommand(this));

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new StatsListener(this), this);
        manager.registerEvents(new JoinItemListener(this), this);
        manager.registerEvents(new KitSelectorListener(this), this);
        manager.registerEvents(new FastPotListener(), this);

        loadKits();
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> profileManager.saveProfileData(), (20 * 60) * 5, (20 * 60) * 5);

        Plugin factionsPlugin = getServer().getPluginManager().getPlugin("HCFactions");
        if(factionsPlugin instanceof HCFactions){
            factions = (HCFactions) factionsPlugin;
        }else{
            throw new RuntimeException("HCFactions not found");
        }

        Plugin statsPlugin = getServer().getPluginManager().getPlugin("Stats");
        if(statsPlugin instanceof Stats){
            stats = (Stats) statsPlugin;
        }else{
            throw new RuntimeException("Stats not found");
        }

        Plugin corePlugin = getServer().getPluginManager().getPlugin("Core");
        if(corePlugin instanceof HCF){
            core = (HCF) corePlugin;
        }else{
            throw new RuntimeException("Core not found");
        }
    }

    @Override
    public void onDisable(){
        profileManager.saveProfileData();
    }

    private void loadKits(){
        for(String kitName : getConfig().getConfigurationSection("kits").getKeys(false)){
            ItemStack helmet = null, chest = null, leggings = null, boots = null, fillItem = null;
            Map<Integer, ItemStack> inv = new HashMap<>();

            if(getConfig().contains("kits." + kitName + ".armor.helmet")){
                helmet = ItemBuilder.buildItem(this, "kits." + kitName + ".armor.helmet");
            }

            if(getConfig().contains("kits." + kitName + ".armor.chestplate")){
                chest = ItemBuilder.buildItem(this, "kits." + kitName + ".armor.chestplate");
            }

            if(getConfig().contains("kits." + kitName + ".armor.leggings")){
                leggings = ItemBuilder.buildItem(this, "kits." + kitName + ".armor.leggings");
            }

            if(getConfig().contains("kits." + kitName + ".armor.boots")){
                boots = ItemBuilder.buildItem(this, "kits." + kitName + ".armor.boots");
            }

            if(getConfig().contains("kits." + kitName + ".inventory.fill")){
                fillItem = ItemBuilder.buildItem(this, "kits." + kitName + ".inventory.fill");
            }

            if(getConfig().contains("kits." + kitName + ".inventory.items")){
                for(String slotAsString : getConfig().getConfigurationSection("kits." + kitName + ".inventory.items").getKeys(false)){
                    inv.put(Integer.valueOf(slotAsString), ItemBuilder.buildItem(this, "kits." + kitName + ".inventory.items." + slotAsString));
                }
            }

            kits.put(kitName, new Kit(inv, helmet, chest, leggings, boots, fillItem));
        }
    }
}
