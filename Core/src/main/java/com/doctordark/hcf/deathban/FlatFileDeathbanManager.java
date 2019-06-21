package com.doctordark.hcf.deathban;

import com.doctordark.hcf.HCF;
import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.faction.Faction;
import technology.brk.util.PersistableLocation;
import technology.brk.util.file.Config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FlatFileDeathbanManager implements DeathbanManager{

    private static final int MAX_DEATHBAN_MULTIPLIER = 300;

    private final HCF plugin;

    private TObjectIntMap<UUID> lives;
    private Config livesConfig;

    public FlatFileDeathbanManager(HCF plugin){
        this.plugin = plugin;
        reloadDeathbanData();
    }

    @Override
    public TObjectIntMap<UUID> getLivesMap(){
        return lives;
    }

    @Override
    public int getLives(UUID uuid){
        return lives.get(uuid);
    }

    @Override
    public int setLives(UUID uuid, int lives){
        this.lives.put(uuid, lives);
        plugin.getServer().getPluginManager().callEvent(new LivesUpdateEvent(uuid, lives));
        return lives;
    }

    @Override
    public int addLives(UUID uuid, int amount){
        int newValue = lives.adjustOrPutValue(uuid, amount, amount);
        plugin.getServer().getPluginManager().callEvent(new LivesUpdateEvent(uuid, newValue));
        return newValue;
    }

    @Override
    public int takeLives(UUID uuid, int amount){
        return setLives(uuid, getLives(uuid) - amount);
    }

    @Override
    public double getDeathBanMultiplier(Player player){
        for(int i = 5; i < MAX_DEATHBAN_MULTIPLIER; i++){
            if(player.hasPermission("hcf.deathban.multiplier." + i)){
                return ((double) i) / 100.0;
            }
        }
        return 1.0D;
    }

    @Override
    public Deathban applyDeathBan(Player player, String reason){
        Location location = player.getLocation();
        Faction factionAt = plugin.getFactions().getFactionManager().getFactionAt(location);

        long duration;
        if(plugin.getEotwHandler().isEndOfTheWorld()){
            duration = MAX_DEATHBAN_TIME;
        }else{
            duration = TimeUnit.MINUTES.toMillis(plugin.getConfiguration().getDeathbanBaseDurationMinutes());
            if(!factionAt.isDeathban()){
                duration /= 2L; // non-deathban factions should be 50% quicker
            }

            duration *= getDeathBanMultiplier(player);
            duration *= factionAt.getDeathbanMultiplier();
        }

        return applyDeathBan(player.getUniqueId(), new Deathban(reason, Math.min(MAX_DEATHBAN_TIME, duration),
                new PersistableLocation(location), plugin.getEotwHandler().isEndOfTheWorld()));
    }

    @Override
    public Deathban applyDeathBan(UUID uuid, Deathban deathban){
        plugin.getUserManager().getUser(uuid).setDeathban(deathban);
        return deathban;
    }

    @Override
    public void reloadDeathbanData(){
        livesConfig = new Config(plugin, "lives");
        Object object = livesConfig.get("lives");
        if(object instanceof MemorySection){
            MemorySection section = (MemorySection) object;
            Set<String> keys = section.getKeys(false);
            lives = new TObjectIntHashMap<>(keys.size(), Constants.DEFAULT_LOAD_FACTOR, 0);
            for(String id : keys){
                lives.put(UUID.fromString(id), livesConfig.getInt(section.getCurrentPath() + "." + id));
            }
        }else{
            lives = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0);
        }
    }

    @Override
    public void saveDeathbanData(){
        Map<String, Integer> saveMap = new LinkedHashMap<>(lives.size());
        lives.forEachEntry((uuid, i) -> {
            saveMap.put(uuid.toString(), i);
            return true;
        });

        livesConfig.set("lives", saveMap);
        livesConfig.save();
    }
}
