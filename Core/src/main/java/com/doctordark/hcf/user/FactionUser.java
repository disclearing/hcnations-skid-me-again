package com.doctordark.hcf.user;

import com.doctordark.hcf.deathban.Deathban;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent;
import technology.brk.util.mongo.Mongoable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FactionUser implements Mongoable{

    private Deathban deathban;


    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Document backing;

    public FactionUser(UUID userUUID){
        backing = new Document("_id", userUUID.toString());
    }

    public FactionUser(Document document){
        this.backing = document;
        if(backing.containsKey("filteredTypes")){
            backing.put("filteredTypes", null);
        }
    }

    public void incrementKills(){
        lock.writeLock().lock();
        try{
            Document kills;

            if(backing.containsKey("kills")){
                kills = backing.get("kills", Document.class);
                kills.put("count", kills.getInteger("count", 0) + 1);
            }else{
                kills = new Document("count", 1);
                backing.put("kills", kills);
            }
        }finally{
            lock.writeLock().unlock();
        }
    }

    public void incrementDeaths(){
        lock.writeLock().lock();
        try{
            Document deaths;

            if(backing.containsKey("deaths")){
                deaths = backing.get("deaths", Document.class);
                deaths.put("count", deaths.getInteger("count", 0) + 1);
            }else{
                deaths = new Document("count", 1);
                backing.put("deaths", deaths);
            }
        }finally{
            lock.writeLock().unlock();
        }
    }

    public void addKill(String whom, UUID whomUUID, @Nullable String item, EntityDamageEvent.DamageCause cause){
        if(item == null) item = "";
        lock.writeLock().lock();
        try{
            Document kills;

            if(backing.containsKey("kills")){
                kills = backing.get("kills", Document.class);
            }else{
                kills = new Document();
                backing.put("kills", kills);
            }

            List<Document> entries = kills.containsKey("entries") ? kills.get("entries", List.class) : new ArrayList<>();
            entries.add(new Document("whom", whom).append("whomUUID", whomUUID.toString()).append("item", item).append("cause", cause.name()).append("timestamp", System.currentTimeMillis()));
            kills.put("entries", entries);
        }finally{
            lock.writeLock().unlock();
        }
    }

    public void addDeath(String by, UUID byUUID, @Nullable String item, EntityDamageEvent.DamageCause cause){
        if(item == null) item = "";
        lock.writeLock().lock();
        try{
            Document deaths;

            if(backing.containsKey("deaths")){
                deaths = backing.get("deaths", Document.class);
            }else{
                deaths = new Document();
                backing.put("deaths", deaths);
            }

            List<Document> entries = deaths.containsKey("entries") ? deaths.get("entries", List.class) : new ArrayList<>();
            entries.add(new Document("by", by).append("byUUID", byUUID.toString()).append("item", item).append("cause", cause.name()).append("timestamp", System.currentTimeMillis()));
            deaths.put("entries", entries);
        }finally{
            lock.writeLock().unlock();
        }
    }

    public int getKills(){
        lock.readLock().lock();
        try{
            return backing.containsKey("kills") ? backing.get("kills", Document.class).getInteger("count", 0) : 0;
        }finally{
            lock.readLock().unlock();
        }
    }

    public int getDeaths(){
        lock.readLock().lock();
        try{
            return backing.containsKey("deaths") ? backing.get("deaths", Document.class).getInteger("count", 0) : 0;
        }finally{
            lock.readLock().unlock();
        }
    }

    public void incrementOre(Material material){
        lock.writeLock().lock();
        try{
            Document ores;
            if(backing.containsKey("ores")){
                ores = backing.get("ores", Document.class);
            }else{
                ores = new Document();
                backing.put("ores", ores);
            }

            ores.put(material.name(), ores.getInteger(material.name(), 0) + 1);
        }finally{
            lock.writeLock().unlock();
        }
    }

    public int getOreCount(Material material){
        lock.readLock().lock();
        try{
            return backing.containsKey("ores") ? backing.get("ores", Document.class).getInteger(material.name(), 0) : 0;
        }finally{
            lock.readLock().unlock();
        }
    }

    public boolean hasNightVisionEnabled(){
        lock.readLock().lock();
        try{
            return backing.getBoolean("nightVision", false);
        }finally{
            lock.readLock().unlock();
        }
    }

    public void setNightVisionEnabled(boolean state){
        lock.writeLock().lock();
        try{
            backing.put("nightVision", state);
        }finally{
            lock.writeLock().unlock();
        }
    }

    public void setFiltered(Material material, boolean state){
        lock.writeLock().lock();
        try{
            List<String> filtered = backing.containsKey("filteredTypes") ? backing.get("filteredTypes", List.class) : new ArrayList<>();
            if(state){
                filtered.add(material.name());
            }else{
                filtered.remove(material.name());
            }
            backing.put("filteredTypesV2", filtered);
        }finally{
            lock.writeLock().unlock();
        }
    }

    public boolean isFiltered(Material material){
        lock.readLock().lock();
        try{
            return backing.containsKey("filteredTypes") && backing.get("filteredTypes", List.class).contains(material.name());
        }finally{
            lock.readLock().unlock();
        }
    }

    public List<Material> getFilteredBlocks(){
        lock.readLock().lock();
        try{
            return backing.containsKey("filteredTypes") ? backing.get("filteredTypes", List.class) : new ArrayList<>();
        }finally{
            lock.readLock().unlock();
        }
    }

    public List<String> getPastFactions(){
        lock.readLock().lock();
        try{
            return backing.containsKey("pastFactions") ? backing.get("pastFactions", List.class) : new ArrayList<>();
        }finally{
            lock.readLock().unlock();
        }
    }

    public void addPastFaction(String faction){
        lock.writeLock().lock();
        try{
            List<String> factions = backing.containsKey("pastFactions") ? backing.get("pastFactions", List.class) : new ArrayList<>();
            if(!factions.contains(faction)){
                factions.add(faction);
                backing.put("pastFactions", factions);
            }
        }finally{
            lock.writeLock().unlock();
        }
    }

    public void removePastFaction(String faction){
        lock.writeLock().lock();
        try{
            List<String> factions = backing.containsKey("pastFactions") ? backing.get("pastFactions", List.class) : new ArrayList<>();
            factions.remove(faction);
            backing.put("pastFactions", factions);
        }finally{
            lock.writeLock().unlock();
        }
    }

    public void setLastFactionLeaveMillis(long millis){
        lock.writeLock().lock();
        try{
            backing.put("lastFactionLeaveMillis", Long.toString(millis));
        }finally{
            lock.writeLock().unlock();
        }
    }

    public long getLastFactionLeaveMillis(){
        lock.readLock().lock();
        try{
            return backing.containsKey("lastFactionLeaveMillis") ? Long.valueOf(backing.getString("lastFactionLeaveMillis")) : 0L;
        }finally{
            lock.readLock().unlock();
        }
    }

    public Deathban getDeathban(){
        lock.readLock().lock();
        try{
            Object result = backing.get("deathban");

            if(result instanceof Deathban){
                return (Deathban) result;
            }

            if(result instanceof Document){
                return new Deathban((Document) result);
            }

            if(result instanceof String){
                return new Deathban(Document.parse((String) result));
            }

            return null;
        }finally{
            lock.readLock().unlock();
        }
    }

    public void removeDeathban(){
        lock.writeLock().lock();
        try{
            backing.put("deathban", null);
        }finally{
            lock.writeLock().unlock();
        }
    }

    public void setDeathban(Deathban deathban){
        lock.writeLock().lock();
        try{
            backing.put("deathban", deathban);
        }finally{
            lock.writeLock().unlock();
        }
    }

    public void updateName(String name){
        lock.writeLock().lock();
        try{
            backing.put("name", name);
        }finally{
            lock.writeLock().unlock();
        }
    }

    boolean needsSaving(){
        lock.writeLock().lock();
        try{
            if(!backing.containsKey("lastSaveHash")){
                return true;
            }

            int lastSaveHash = (int) backing.remove("lastSaveHash");
            boolean state = lastSaveHash != backing.hashCode();
            backing.put("lastSaveHash", lastSaveHash);
            return state;
        }finally{
            lock.writeLock().unlock();
        }
    }

    @Override
    public Document toDocument(){
        return backing;
    }
}
