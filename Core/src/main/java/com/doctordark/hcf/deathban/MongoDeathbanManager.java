/*
 * Copyright (C) 2016 SystemUpdate (https://systemupdate.io) All Rights Reserved
 */

package com.doctordark.hcf.deathban;

import com.doctordark.hcf.HCF;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.hcgames.hcfactions.faction.Faction;
import technology.brk.util.PersistableLocation;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MongoDeathbanManager implements DeathbanManager, Listener{

    private static final int MAX_DEATHBAN_MULTIPLIER = 300;

    private final TObjectIntMap<UUID> lives = new TObjectIntHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0);

    private MongoCollection<Document> collection;
    private HCF plugin;

    public MongoDeathbanManager(HCF plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;

        collection = plugin.getMongoManager().getDatabase().getCollection("lives");
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
    public int setLives(UUID uuid, int amount){
        lives.put(uuid, amount);
        plugin.getServer().getPluginManager().callEvent(new LivesUpdateEvent(uuid, amount));
        return amount;
    }

    @Override
    public int addLives(UUID uuid, int amount){
        return setLives(uuid, getLives(uuid) + amount);
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
        for(Document document : collection.find()){
            UUID uuid = UUID.fromString(document.getString("_id"));

            if(document.containsKey("lives")){
                lives.put(uuid, document.getInteger("lives"));
            }
        }
    }

    @Override
    public void saveDeathbanData(){
        for(UUID uuid : lives.keySet()){
            collection.updateOne(new Document("_id", uuid.toString()), new Document("$set", new Document("lives",
                    lives.get(uuid))), new UpdateOptions().upsert(true));
        }
    }

}
