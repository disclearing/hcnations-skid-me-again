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

package org.hcgames.hcfactions;

import com.google.common.base.Joiner;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.claim.ClaimHandler;
import org.hcgames.hcfactions.command.FactionExecutor;
import org.hcgames.hcfactions.command.LocationCommand;
import org.hcgames.hcfactions.command.RegenCommand;
import org.hcgames.hcfactions.faction.ClaimableFaction;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.faction.system.EndPortalFaction;
import org.hcgames.hcfactions.faction.system.RoadFaction;
import org.hcgames.hcfactions.faction.system.SpawnFaction;
import org.hcgames.hcfactions.faction.system.WarzoneFaction;
import org.hcgames.hcfactions.faction.system.WildernessFaction;
import org.hcgames.hcfactions.listener.ClaimWandListener;
import org.hcgames.hcfactions.listener.FactionChatListener;
import org.hcgames.hcfactions.listener.NameCacheListener;
import org.hcgames.hcfactions.listener.ProtectionListener;
import org.hcgames.hcfactions.listener.SignSubclaimListener;
//import org.hcgames.hcfactions.listener.StaffHookListener;
import org.hcgames.hcfactions.manager.FactionManager;
import org.hcgames.hcfactions.manager.FlatFileFactionManager;
import org.hcgames.hcfactions.manager.MongoFactionManager;
import org.hcgames.hcfactions.structure.FactionMember;
import org.hcgames.stats.Stats;
import technology.brk.util.file.Messages;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;


@Getter
public class HCFactions extends JavaPlugin{

    public static final Joiner SPACE_JOINER = Joiner.on(' ');
    public static final Joiner COMMA_JOINER = Joiner.on(", ");

    private MongoManager mongoManager;
    private WorldEditPlugin worldEdit;//TODO

    private Configuration configuration;
    private boolean configLoaded;

    private FactionManager factionManager;
    private ClaimHandler claimHandler;

    private Messages messages;
    private Stats stats;

    @Override
    public void onLoad(){
        ConfigurationSerialization.registerClass(Claim.class);
        ConfigurationSerialization.registerClass(ClaimableFaction.class);
        ConfigurationSerialization.registerClass(EndPortalFaction.class);
        ConfigurationSerialization.registerClass(Faction.class);
        ConfigurationSerialization.registerClass(FactionMember.class);
        ConfigurationSerialization.registerClass(PlayerFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.class);
        ConfigurationSerialization.registerClass(SpawnFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.NorthRoadFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.EastRoadFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.SouthRoadFaction.class);
        ConfigurationSerialization.registerClass(RoadFaction.WestRoadFaction.class);

        FactionManager.registerSystemFaction(EndPortalFaction.class);
        FactionManager.registerSystemFaction(RoadFaction.EastRoadFaction.class);
        FactionManager.registerSystemFaction(RoadFaction.NorthRoadFaction.class);
        FactionManager.registerSystemFaction(RoadFaction.SouthRoadFaction.class);
        FactionManager.registerSystemFaction(RoadFaction.WestRoadFaction.class);
        FactionManager.registerSystemFaction(SpawnFaction.class);
        FactionManager.registerSystemFaction(WarzoneFaction.class);
        FactionManager.registerSystemFaction(WildernessFaction.class);
    }

    @Override
    public void onEnable(){
        if(!registerConfiguration()){
            getLogger().severe("Failed to load configuration.");
            setEnabled(false);
            return;
        }

        registerManagers();
        registerListeners();
        registerCommands();

        getServer().getScheduler().runTaskTimerAsynchronously(this, this::saveData, (60 * 20) * 5, (60 * 20) * 5);
    }

    @Override
    public void onDisable(){
        if (!configLoaded) {
            // Ignore everything.
            return;
        }

        saveData();
        if(mongoManager != null){
            mongoManager.close();
        }

        try{
            String configFileName = "config.cdl";
            configuration.save(new File(getDataFolder(), configFileName), HCFactions.class.getResource("/" + configFileName));
        } catch (IOException | InvalidConfigurationException ex) {
            getLogger().warning("Unable to save config.");
            ex.printStackTrace();
        }
    }

    private boolean registerConfiguration(){
        configuration = new Configuration();
        try {
            String configFileName = "config.cdl";
            File file = new File(getDataFolder(), configFileName);
            if (!file.exists()) {
                saveResource(configFileName, false);
            }

            configuration.load(file, HCFactions.class.getResource("/" + configFileName));
            configuration.load(this);

            messages = new Messages(this, "messages.yml", getConfiguration().isMessageDebug());
        } catch (IOException | InvalidConfigurationException ex) {
            getLogger().log(Level.SEVERE, "Failed to load configuration", ex);
            configLoaded = false;
            return false;
        }

        configLoaded = true;
        return true;
    }

    private void saveData(){
        factionManager.saveFactionData();
    }

    private void registerListeners(){
        PluginManager manager = getServer().getPluginManager();

        manager.registerEvents(new ClaimWandListener(this), this);
        manager.registerEvents(new NameCacheListener(this), this);
        manager.registerEvents(new SignSubclaimListener(this), this);
        manager.registerEvents(new ProtectionListener(this), this);
        manager.registerEvents(new FactionChatListener(this), this);

//        Plugin staff = manager.getPlugin("Staff");
//        if(staff != null && staff.getDescription().getMain().equals("technology.brk.staff.Staff")){
//            new StaffHookListener(this);
//        }
    }

    private void registerCommands(){
        getCommand("factions").setExecutor(new FactionExecutor(this));
        getCommand("location").setExecutor(new LocationCommand(this));
        getCommand("regen").setExecutor(new RegenCommand(this));
    }

    private void registerManagers(){
        if(configuration.isUseMongo()){
            mongoManager = new MongoManager(this);
            factionManager = new MongoFactionManager(this);
        }else{
            factionManager = new FlatFileFactionManager(this);
        }

        claimHandler = new ClaimHandler(this);

        worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        Plugin statsPlugin = getServer().getPluginManager().getPlugin("Stats");
        if(statsPlugin instanceof Stats){
            stats = (Stats) statsPlugin;
        }
    }
}
