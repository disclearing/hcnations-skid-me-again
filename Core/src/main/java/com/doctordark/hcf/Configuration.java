package com.doctordark.hcf;

import com.doctordark.hcf.mongo.StorageType;
import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.techcable.techutils.config.AnnotationConfig;
import net.techcable.techutils.config.Setting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionType;
import technology.brk.util.PersistableLocation;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Getter
public class Configuration extends AnnotationConfig{

    @SuppressWarnings("ALL")
    @Setting("potionLimits")
    private final List<String> potionLimitsUnstored = new ArrayList<>();

    @Setting("potionsExtendedDisallowed")
    private final List<String> potionsExtendedDisallowed = new ArrayList<>();

    @SuppressWarnings("ALL")
    @Setting("enchantmentLimits")
    private final List<String> enchantmentLimitsUnstored = new ArrayList<>();
    private final TObjectIntMap<Enchantment> enchantmentLimits = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
    private final TObjectIntMap<PotionType> potionLimits = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
    private final List<PotionType> disallowedExtendedPotions = new ArrayList<>();

    @Setting("handleEntityLimiting")
    private boolean handleEntityLimiting = true;

    @Setting("removeInfinityArrowsOnLand")
    private boolean removeInfinityArrowsOnLand = true;

    @Setting("beaconStrengthLevelLimit")
    private int beaconStrengthLevelLimit = 1;

    @Setting("disableBoatPlacementOnLand")
    private boolean disableBoatPlacementOnLand = true;

    @Setting("disableEnderchests")
    private boolean disableEnderchests = true;

    @Setting("preventPlacingBedsNether")
    private boolean preventPlacingBedsNether = false;

    @Getter(AccessLevel.NONE)
    @Setting("serverTimeZone")
    private String serverTimeZoneName = "EST";
    private TimeZone serverTimeZone;
    private ZoneId serverTimeZoneID;

    @Setting("furnaceCookSpeedMultiplier")
    private float furnaceCookSpeedMultiplier = 6.0F;

    @Setting("potionBrewSpeedMultiplier")
    private int potionBrewSpeedMultiplier = 6;

    @Setting("bottledExp")
    private boolean bottledExp = true;

    @Setting("bookDeenchanting")
    private boolean bookDeenchanting = true;

    @Setting("deathSigns")
    private boolean deathSigns = true;

    @Setting("deathLightning")
    private boolean deathLightning = true;

    @Setting("mapNumber")
    private int mapNumber = 1;

    @Setting("kitMap")
    private boolean kitMap = false;

    @Setting("economy.startingBalance")
    private int economyStartingBalance = 250;

    @Setting("spawners.preventBreakingNether")
    private boolean spawnersPreventBreakingNether = true;

    @Setting("spawners.preventPlacingNether")
    private boolean spawnersPreventPlacingNether = true;

    @Setting("expMultiplier.global")
    private float expMultiplierGlobal = 1.0F;

    @Setting("expMultiplier.fishing")
    private float expMultiplierFishing = 1.0F;

    @Setting("expMultiplier.smelting")
    private float expMultiplierSmelting = 1.0F;

    @Setting("expMultiplier.lootingPerLevel")
    private float expMultiplierLootingPerLevel = 1.0F;

    @Setting("expMultiplier.luckPerLevel")
    private float expMultiplierLuckPerLevel = 1.0F;

    @Setting("expMultiplier.fortunePerLevel")
    private float expMultiplierFortunePerLevel = 1.0F;

    @Setting("scoreboard.sidebar.title")
    private String scoreboardSidebarTitle = "&a&lHCF &c[Map {MAP_NUMBER}]";

    @Setting("scoreboard.sidebar.enabled")
    private boolean scoreboardSidebarEnabled = true;

    @Setting("scoreboard.nametags.enabled")
    private boolean scoreboardNametagsEnabled = true;

    @Setting("combatlog.enabled")
    private boolean handleCombatLogging = true;

    @Setting("combatlog.despawnDelayTicks")
    private int combatlogDespawnDelayTicks = 600;

    @Setting("conquest.pointLossPerDeath")
    private int conquestPointLossPerDeath = 20;

    @Setting("conquest.requiredVictoryPoints")
    private int conquestRequiredVictoryPoints = 300;

    @Setting("conquest.allowNegativePoints")
    private boolean conquestAllowNegativePoints = true;

    @Setting("fury.pointLossPerDeath")
    private int furyPointLossPerDeath = 20;

    @Setting("fury.requiredVictoryPoints")
    private int furyRequiredVictoryPoints = 300;

    @Setting("fury.allowNegativePoints")
    private boolean furyAllowNegativePoints = true;

    @Setter
    @Setting("deathban.baseDurationMinutes")
    private int deathbanBaseDurationMinutes = 60;

    @Setter
    @Setting("deathban.respawnScreenSecondsBeforeKick")
    private int deathbanRespawnScreenSecondsBeforeKick = 15;
    private long deathbanRespawnScreenTicksBeforeKick;

    @Setter
    @Setting("end.open")
    private boolean endOpen = true;

    @Setting("end.exitLocation")
    private String endExitLocationRaw = "world,0.5,75,0.5,0,0";
    private PersistableLocation endExitLocation = new PersistableLocation(Bukkit.getWorld("world"), 0.5, 75, 0.5);

    @Setting("end.entranceLocation")
    private String endEntranceLocationRaw = "world_the_end,0.5,75,0.5,0,0";
    private Location endEntranceLocation = new Location(Bukkit.getWorld("world_the_end"), 0.5, 75, 0.5);

    @Setting("end.extinguishFireOnExit")
    private boolean endExtinguishFireOnExit = true;

    @Setting("end.removeStrengthOnEntrance")
    private boolean endRemoveStrengthOnEntrance = true;

    @Setting("messageDebug")
    private boolean messageDebug;

    @Setting("data.mongo.auth")
    private boolean mongoDataAuth;

    @Setting("data.mongo.address")
    private String mongoAddress = "127.0.0.1";

    @Setting("data.mongo.port")
    private int mongoPort = 27017;

    @Setting("data.mongo.username")
    private String mongoUsername = "mongo";

    @Setting("data.mongo.password")
    private String mongoPassword = "secure";

    @Setting("data.mongo.database")
    private String mongoDatabase = "hardcore";

    @Setting("data.lives")
    @Getter(AccessLevel.NONE)
    private String livesStorage = "FLATFILE";
    private StorageType livesStorageType;

    @Setting("data.users")
    @Getter(AccessLevel.NONE)
    private String usersStorage = "FLATFILE";
    private StorageType usersStorageType;

    @Setting("data.economy")
    @Getter(AccessLevel.NONE)
    private String economyStorage = "FLATFILE";
    private StorageType ecomonyStorageType;

    @Setting("cheapGlisteringMelon")
    private boolean cheapGlisteringMelon = true;


    public int getEnchantmentLimit(Enchantment enchantment){
        int maxLevel = enchantmentLimits.get(enchantment);
        return maxLevel == enchantmentLimits.getNoEntryValue() ? enchantment.getMaxLevel() : maxLevel;
    }

    public int getPotionLimit(PotionType potionEffectType){
        int maxLevel = potionLimits.get(potionEffectType);
        return maxLevel == potionLimits.getNoEntryValue() ? potionEffectType.getMaxLevel() : maxLevel;
    }

    public boolean isExtendedDurationDisallowed(PotionType type){
        return disallowedExtendedPotions.contains(type);
    }

    protected void updateFields(HCF plugin){
        serverTimeZone = TimeZone.getTimeZone(serverTimeZoneName);
        serverTimeZoneID = serverTimeZone.toZoneId();
        scoreboardSidebarTitle = ChatColor.translateAlternateColorCodes('&', scoreboardSidebarTitle.replace("{MAP_NUMBER}", Integer.toString(mapNumber)));
        deathbanRespawnScreenTicksBeforeKick = TimeUnit.SECONDS.toMillis(deathbanRespawnScreenSecondsBeforeKick) / 50L;
        livesStorageType = StorageType.valueOf(livesStorage);
        usersStorageType = StorageType.valueOf(usersStorage);
        ecomonyStorageType = StorageType.valueOf(economyStorage);

        String[] split = endExitLocationRaw.split(",");
        if(split.length == 6){
            try{
                String worldName = split[0];
                World world = Bukkit.getWorld(worldName);

                if(world != null){
                    Integer x = Integer.parseInt(split[1]);
                    Integer y = Integer.parseInt(split[2]);
                    Integer z = Integer.parseInt(split[3]);
                    Float yaw = Float.parseFloat(split[4]);
                    Float pitch = Float.parseFloat(split[5]);

                    endExitLocation = new PersistableLocation(worldName, x, y, z);
                    endExitLocation.setWorld(world);
                    endExitLocation.setYaw(yaw);
                    endExitLocation.setPitch(pitch);
                }
            }catch(NumberFormatException ignored){
            }
        }

        split = endEntranceLocationRaw.split(",");
        if(split.length == 6){
            try{
                String worldName = split[0];
                World world = Bukkit.getWorld(worldName);

                if(world != null){
                    Integer x = Integer.parseInt(split[1]);
                    Integer y = Integer.parseInt(split[2]);
                    Integer z = Integer.parseInt(split[3]);
                    Float yaw = Float.parseFloat(split[4]);
                    Float pitch = Float.parseFloat(split[5]);

                    endEntranceLocation = new Location(world, x, y, z, yaw, pitch);
                }
            }catch(NumberFormatException ignored){
            }
        }


        String splitter = " = ";
        for(String entry : potionLimitsUnstored){
            if(entry.contains(splitter)){
                split = entry.split(splitter);
                String key = split[0];
                Integer value = Integer.parseInt(split[1]);

                PotionType effect = PotionType.valueOf(key);
                if(effect != null){
                    Bukkit.getLogger().log(Level.INFO, "Potion effect limit of " + effect.name() + " set as " + value);
                    potionLimits.put(effect, value);
                }else{
                    Bukkit.getLogger().log(Level.WARNING, "Unknown potion effect '" + key + "'.");
                }
            }
        }

        for(String entry : potionsExtendedDisallowed){
            PotionType type = PotionType.valueOf(entry);

            if(type != null){
                Bukkit.getLogger().log(Level.INFO, "Extended duration limit for " + type.name() + " set.");
                disallowedExtendedPotions.add(type);
            }
        }

        for(String entry : enchantmentLimitsUnstored){
            if(entry.contains(splitter)){
                split = entry.split(splitter);
                String key = split[0];
                Integer value = Integer.parseInt(split[1]);

                Enchantment enchantment = Enchantment.getByName(key);
                if(enchantment != null){
                    Bukkit.getLogger().log(Level.INFO, "Enchantment limit of " + enchantment.getName() + " set as " + value);
                    enchantmentLimits.put(enchantment, value);
                }else{
                    Bukkit.getLogger().log(Level.WARNING, "Unknown enchantment effect '" + key + "'.");
                }
            }
        }

        if(cheapGlisteringMelon){
            ShapelessRecipe cheapMelonRecipe = new ShapelessRecipe(new ItemStack(Material.SPECKLED_MELON));
            cheapMelonRecipe.addIngredient(1, Material.MELON);
            cheapMelonRecipe.addIngredient(1, Material.GOLD_NUGGET);
            plugin.getServer().addRecipe(cheapMelonRecipe);
        }
    }
}
