package com.doctordark.hcf;

import com.doctordark.hcf.combatlog.CombatLogListener;
import com.doctordark.hcf.command.player.FilterCommand;
import com.doctordark.hcf.command.server.FreezeServerCommand;
import com.doctordark.hcf.command.server.ListCommand;
import com.doctordark.hcf.command.server.LoadoutCommand;
import com.doctordark.hcf.command.player.LogoutCommand;
import com.doctordark.hcf.command.server.MapKitCommand;
import com.doctordark.hcf.command.player.NightVisionCommand;
import com.doctordark.hcf.command.player.OresCommand;
import com.doctordark.hcf.command.player.PlayerTimeCommand;
import com.doctordark.hcf.command.player.PvpTimerCommand;
import com.doctordark.hcf.command.server.SaveDataCommand;
import com.doctordark.hcf.command.server.ToggleEndCommand;
import com.doctordark.hcf.command.server.TogglePMCommand;
import com.doctordark.hcf.deathban.Deathban;
import com.doctordark.hcf.deathban.DeathbanListener;
import com.doctordark.hcf.deathban.DeathbanManager;
import com.doctordark.hcf.deathban.FlatFileDeathbanManager;
import com.doctordark.hcf.deathban.MongoDeathbanManager;
import com.doctordark.hcf.deathban.ReviveCommand;
import com.doctordark.hcf.deathban.lives.LivesExecutor;
import com.doctordark.hcf.economy.EconomyCommand;
import com.doctordark.hcf.economy.EconomyManager;
import com.doctordark.hcf.economy.FlatFileEconomyManager;
import com.doctordark.hcf.economy.MongoEconomyManager;
import com.doctordark.hcf.economy.PayCommand;
import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventExecutor;
import com.doctordark.hcf.eventgame.EventScheduler;
import com.doctordark.hcf.eventgame.conquest.ConquestExecutor;
import com.doctordark.hcf.eventgame.crate.KeyListener;
import com.doctordark.hcf.eventgame.crate.KeyManager;
import com.doctordark.hcf.eventgame.eotw.EotwCommand;
import com.doctordark.hcf.eventgame.eotw.EotwHandler;
import com.doctordark.hcf.eventgame.eotw.EotwListener;
import com.doctordark.hcf.eventgame.faction.CapturableFaction;
import com.doctordark.hcf.eventgame.faction.ConquestFaction;
import com.doctordark.hcf.eventgame.faction.FuryFaction;
import com.doctordark.hcf.eventgame.faction.KothFaction;
import com.doctordark.hcf.eventgame.faction.PalaceFaction;
import com.doctordark.hcf.eventgame.fury.FuryExecutor;
import com.doctordark.hcf.eventgame.koth.KothExecutor;
import com.doctordark.hcf.eventgame.glmoutain.GlowstoneMountain;
import com.doctordark.hcf.hook.ProtocolLibHook;
import com.doctordark.hcf.hook.VaultHook;
import com.doctordark.hcf.invrestore.InvCommand;
import com.doctordark.hcf.invrestore.InvListener;
import com.doctordark.hcf.invrestore.InvManager;
import com.doctordark.hcf.listener.BlockFilterListener;
import com.doctordark.hcf.listener.BookDeenchantListener;
import com.doctordark.hcf.listener.BottledExpListener;
import com.doctordark.hcf.listener.ChatListener;
import com.doctordark.hcf.listener.CoreListener;
import com.doctordark.hcf.listener.CrowbarListener;
import com.doctordark.hcf.listener.DeathListener;
import com.doctordark.hcf.listener.DeathMessageListener;
import com.doctordark.hcf.listener.DeathSignListener;
import com.doctordark.hcf.listener.ElevatorListener;
import com.doctordark.hcf.listener.EnchantLimitListener;
import com.doctordark.hcf.listener.EntityLimitListener;
import com.doctordark.hcf.listener.EventSignListener;
import com.doctordark.hcf.listener.ExpMultiplierListener;
import com.doctordark.hcf.listener.FactionListener;
import com.doctordark.hcf.listener.FastBrewListener;
import com.doctordark.hcf.listener.FoundDiamondsListener;
import com.doctordark.hcf.listener.FreezeServerListener;
import com.doctordark.hcf.listener.FurnaceSmeltSpeederListener;
import com.doctordark.hcf.listener.HelpListener;
import com.doctordark.hcf.listener.KitMapListener;
import com.doctordark.hcf.listener.NightVisionListener;
import com.doctordark.hcf.listener.OreCountListener;
import com.doctordark.hcf.listener.OreTrackerListener;
import com.doctordark.hcf.listener.PortalListener;
import com.doctordark.hcf.listener.PotionLimitListener;
import com.doctordark.hcf.listener.SEMListener;
import com.doctordark.hcf.listener.ShopSignListener;
import com.doctordark.hcf.listener.SkullListener;
import com.doctordark.hcf.listener.SyntaxBlocker;
import com.doctordark.hcf.listener.TogglePMListener;
import com.doctordark.hcf.listener.WorldListener;
import com.doctordark.hcf.listener.fixes.BeaconStrengthFixListener;
import com.doctordark.hcf.listener.fixes.BlockHitFixListener;
import com.doctordark.hcf.listener.fixes.BlockJumpGlitchFixListener;
import com.doctordark.hcf.listener.fixes.BoatGlitchFixListener;
import com.doctordark.hcf.listener.fixes.EnderChestRemovalListener;
import com.doctordark.hcf.listener.fixes.InfinityArrowFixListener;
import com.doctordark.hcf.listener.fixes.PVPTimerListener;
import com.doctordark.hcf.listener.fixes.PearlDeathGlitchListener;
import com.doctordark.hcf.listener.fixes.VoidGlitchFixListener;
import com.doctordark.hcf.mongo.MongoManager;
import com.doctordark.hcf.mongo.StorageType;
import com.doctordark.hcf.pvpclass.PvpClassManager;
import com.doctordark.hcf.pvpclass.bard.EffectRestorer;
import com.doctordark.hcf.scoreboard.ScoreboardHandler;
import com.doctordark.hcf.eventgame.sotw.SOTWCommand;
import com.doctordark.hcf.eventgame.sotw.SOTWListener;
import com.doctordark.hcf.eventgame.sotw.SOTWManager;
import com.doctordark.hcf.timer.TimerExecutor;
import com.doctordark.hcf.timer.TimerManager;
import com.doctordark.hcf.user.MongoUserManager;
import com.doctordark.hcf.user.UserListener;
import com.doctordark.hcf.user.UserManager;
import com.doctordark.hcf.visualise.VisualiseHandler;
import com.doctordark.hcf.visualise.WallBorderListener;
import com.google.common.base.Joiner;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcgames.hcfactions.HCFactions;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import technology.brk.staff.Staff;
import technology.brk.util.file.Messages;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

@Getter
public class HCF extends JavaPlugin {

    public static final Joiner SPACE_JOINER = Joiner.on(' ');
    public static final Joiner COMMA_JOINER = Joiner.on(", ");

    @Getter @Deprecated
    private static HCF plugin;

    private Configuration configuration;
    private CombatLogListener combatLogListener;
    private DeathbanManager deathbanManager;
    private EconomyManager economyManager;
    private EffectRestorer effectRestorer;
    private EotwHandler eotwHandler;
    private KeyManager keyManager;
    private PvpClassManager pvpClassManager;
    private ScoreboardHandler scoreboardHandler;
    private TimerManager timerManager;
    private UserManager userManager;
    private VisualiseHandler visualiseHandler;
    private WorldEditPlugin worldEdit;
    private boolean configurationLoaded = true;
    @Deprecated private Messages messagesOld;
    private Messages messages;
    private EventScheduler eventScheduler;
    private SOTWManager SOTWManager;
    private MongoManager mongoManager;
    private VaultHook vaultHook;
    private InvManager invManager;
    @Setter private boolean serverFrozen;
    private HCFactions factions;
    @Getter(AccessLevel.NONE) private LoadoutCommand loadoutCommand;
    private Staff staff;

    @Override
    public void onLoad(){
        registerSerialization();
    }

    @Override
    public void onEnable(){
        registerConfiguration();
        if (!configurationLoaded) {
            getLogger().severe("Disabling plugin..");
            setEnabled(false);
            return;
        }

        HCF.plugin = this;
        Plugin wep = getServer().getPluginManager().getPlugin("WorldEdit");  // Initialise WorldEdit hook.
        worldEdit = wep instanceof WorldEditPlugin && wep.isEnabled() ? (WorldEditPlugin) wep : null;

        Plugin HCFactions = getServer().getPluginManager().getPlugin("HCFactions");
        if(HCFactions instanceof HCFactions){
            this.factions = (org.hcgames.hcfactions.HCFactions) HCFactions;
        }

        //TODO: Commands before managers?!
        staff = JavaPlugin.getPlugin(Staff.class);
        registerCommands();
        registerManagers();
        registerListeners();

        getServer().getScheduler().runTaskTimerAsynchronously(this, this::saveData, (60 * 20) * 5, (60 * 20) * 5);
        ProtocolLibHook.hook(this); // Initialise ProtocolLib hook.

    }

    public void saveData() {
        deathbanManager.saveDeathbanData();
        economyManager.saveEconomyData();
        keyManager.saveKeyData();
        timerManager.saveTimerData();
        userManager.saveUserData();
        invManager.save();
        loadoutCommand.save();
    }


    @Override
    public void onDisable() {
        if (!configurationLoaded) {
            // Ignore everything.
            return;
        }

        combatLogListener.removeCombatLoggers();
        pvpClassManager.onDisable();
        timerManager.getEventTimer().handleDisable();

        saveData();
        if(mongoManager != null){
            mongoManager.close();
        }

        try{
            String configFileName = "config.cdl";
            configuration.save(new File(getDataFolder(), configFileName), HCF.class.getResource("/" + configFileName));
        }catch(IOException | InvalidConfigurationException ex){
            getLogger().warning("Unable to save config.");
            ex.printStackTrace();
        }

        HCF.plugin = null; // Always uninitialise last.
    }
    private void registerConfiguration() {
        configuration = new Configuration();
        try {
            String configFileName = "config.cdl";
            File file = new File(getDataFolder(), configFileName);
            if (!file.exists()) {
                saveResource(configFileName, false);
            }

            configuration.load(file, HCF.class.getResource("/" + configFileName));
            configuration.updateFields(this);
        } catch (IOException | InvalidConfigurationException ex) {
            getLogger().log(Level.SEVERE, "Failed to load configuration", ex);
            configurationLoaded = false;
            return;
        }

        messagesOld = new Messages(this, "messages.yml", getConfiguration().isMessageDebug());
        messages = new Messages(this, "newMessages.yml", getConfiguration().isMessageDebug());

        String eventSchedulesFileName = "eventSchedules.txt";
        File file = new File(getDataFolder(), eventSchedulesFileName);
        if (!file.exists()) {
            saveResource(eventSchedulesFileName, false);
        }
    }

    //TODO: More reliable, SQL based.
    public boolean serialized;
    private void registerSerialization() {
        ConfigurationSerialization.registerClass(CaptureZone.class);
        ConfigurationSerialization.registerClass(Deathban.class);
        ConfigurationSerialization.registerClass(ConquestFaction.class);
        ConfigurationSerialization.registerClass(CapturableFaction.class);
        ConfigurationSerialization.registerClass(KothFaction.class);
        ConfigurationSerialization.registerClass(FuryFaction.class);
        ConfigurationSerialization.registerClass(PalaceFaction.class);
    }

    private void registerListeners() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new BlockJumpGlitchFixListener(), this);
        manager.registerEvents(new BoatGlitchFixListener(this), this);
        manager.registerEvents(new BookDeenchantListener(this), this);
        manager.registerEvents(new BottledExpListener(this), this);
        manager.registerEvents(new ChatListener(this), this);
        manager.registerEvents(combatLogListener = new CombatLogListener(this), this);
        manager.registerEvents(new CoreListener(this), this);
        manager.registerEvents(new CrowbarListener(this), this);
        manager.registerEvents(new DeathListener(this), this);
        manager.registerEvents(new DeathMessageListener(this), this);
        manager.registerEvents(new DeathbanListener(this), this);
        manager.registerEvents(new EnchantLimitListener(this), this);
        manager.registerEvents(new EnderChestRemovalListener(this), this);
        manager.registerEvents(new EntityLimitListener(this), this);
        manager.registerEvents(new EotwListener(this), this);
        manager.registerEvents(new EventSignListener(), this);
        manager.registerEvents(new ExpMultiplierListener(this), this);
        manager.registerEvents(new FactionListener(this), this);
        manager.registerEvents(new FurnaceSmeltSpeederListener(this), this);
        manager.registerEvents(new InfinityArrowFixListener(this), this);
        manager.registerEvents(new KeyListener(this), this);
        manager.registerEvents(new KitMapListener(this), this);
        manager.registerEvents(new PortalListener(this), this);
        manager.registerEvents(new PotionLimitListener(this), this);
        manager.registerEvents(new ShopSignListener(this), this);
        manager.registerEvents(new SkullListener(this), this);
        manager.registerEvents(new SOTWListener(this), this);
        manager.registerEvents(new FreezeServerListener(this), this);
        manager.registerEvents(new BeaconStrengthFixListener(this), this);
        manager.registerEvents(new VoidGlitchFixListener(), this);
        manager.registerEvents(new TogglePMListener(this), this);
        manager.registerEvents(new WallBorderListener(this), this);
        manager.registerEvents(new WorldListener(), this);
        manager.registerEvents(new SyntaxBlocker(), this);
        manager.registerEvents(new NightVisionListener(this), this);
        manager.registerEvents(new FastBrewListener(this), this);
        manager.registerEvents(new OreTrackerListener(), this);
        manager.registerEvents(new HelpListener(this), this);
        manager.registerEvents(new ElevatorListener(), this);
        manager.registerEvents(new OreCountListener(this), this);
        manager.registerEvents(new BlockFilterListener(this), this);
        manager.registerEvents(new FoundDiamondsListener(this), this);
        manager.registerEvents(new InvListener(this), this);
        manager.registerEvents(new SEMListener(this), this);
        manager.registerEvents(new PVPTimerListener(this), this);
        manager.registerEvents(new UserListener(this), this);
        manager.registerEvents(new PearlDeathGlitchListener(), this);
        manager.registerEvents(new BlockHitFixListener(), this);

        if(getConfiguration().isDeathSigns()){
            getServer().getPluginManager().registerEvents(new DeathSignListener(this), this);
        }
    }

    private void registerCommands() {
        getCommand("conquest").setExecutor(new ConquestExecutor(this));
        getCommand("economy").setExecutor(new EconomyCommand(this));
        getCommand("eotw").setExecutor(new EotwCommand(this));
        getCommand("event").setExecutor(new EventExecutor(this));
        getCommand("koth").setExecutor(new KothExecutor(this));
        getCommand("lives").setExecutor(new LivesExecutor(this));
        getCommand("logout").setExecutor(new LogoutCommand(this));
        getCommand("mapkit").setExecutor(new MapKitCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("pvptimer").setExecutor(new PvpTimerCommand(this));
        getCommand("sotw").setExecutor(new SOTWCommand(this));
        getCommand("revive").setExecutor(new ReviveCommand(this));
        getCommand("timer").setExecutor(new TimerExecutor(this));
        getCommand("togglepm").setExecutor(new TogglePMCommand(this));
        getCommand("savedata").setExecutor(new SaveDataCommand(this));
        getCommand("nightvision").setExecutor(new NightVisionCommand(this));
        getCommand("playertime").setExecutor(new PlayerTimeCommand(this));
        getCommand("freezeserver").setExecutor(new FreezeServerCommand(this));
        getCommand("fury").setExecutor(new FuryExecutor(this));
        getCommand("toggleend").setExecutor(new ToggleEndCommand(this));
        getCommand("list").setExecutor(new ListCommand(this));
        getCommand("ores").setExecutor(new OresCommand(this));
        getCommand("blockfilter").setExecutor(new FilterCommand(this));
        getCommand("inv").setExecutor(new InvCommand(this));
        getCommand("loadout").setExecutor(loadoutCommand = new LoadoutCommand(this));

        if(!getConfiguration().isKitMap()){
            getCommand("mountain").setExecutor(new GlowstoneMountain(this));
        }

        Map<String, Map<String, Object>> map = getDescription().getCommands();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            PluginCommand command = getCommand(entry.getKey());
            command.setPermission("hcf.command." + entry.getKey());
            command.setPermissionMessage(ChatColor.RED + "You do not have permission for this command.");
        }
    }

    private void registerManagers(){
        mongoManager = new MongoManager(this);
        userManager = new MongoUserManager(this);
        deathbanManager = configuration.getLivesStorageType().equals(StorageType.MONGO) ? new MongoDeathbanManager(this) : new FlatFileDeathbanManager(this);
        economyManager = configuration.getEcomonyStorageType().equals(StorageType.MONGO) ? new MongoEconomyManager(this) : new FlatFileEconomyManager(this);

        effectRestorer = new EffectRestorer(this);
        eotwHandler = new EotwHandler(this);
        keyManager = new KeyManager(this);
        pvpClassManager = new PvpClassManager(this);
        SOTWManager = new SOTWManager(this);
        eventScheduler = new EventScheduler(this);
        timerManager = new TimerManager(this); // Needs to be registered before ScoreboardHandler.
        scoreboardHandler = new ScoreboardHandler(this);
        visualiseHandler = new VisualiseHandler();
        vaultHook = new VaultHook(this);
        invManager = new InvManager(this);

        if(!getServer().getOnlinePlayers().isEmpty()){
            Set<PermissionUser> members = PermissionsEx.getPermissionManager().getGroup("nation+").getUsers();
            String line;

            if(members.isEmpty()){
                line = "None";
            }else{
                StringBuilder builder = new StringBuilder();
                members.forEach(user -> builder.append(user.getName()).append(ChatColor.WHITE).append(',').append(' '));
                builder.setLength(Math.max(0, builder.length() - 2));
                line = builder.toString();
            }

            getServer().broadcastMessage(getMessages().getString("Broadcasts.Elite").replace("{eliteUsers}", line));
        }
    }

}
