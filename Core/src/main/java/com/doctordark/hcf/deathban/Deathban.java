package com.doctordark.hcf.deathban;

import lombok.Getter;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import technology.brk.util.PersistableLocation;
import technology.brk.util.mongo.Mongoable;

import java.util.LinkedHashMap;
import java.util.Map;

public class Deathban implements ConfigurationSerializable, Mongoable{

    @Getter
    private final String reason;

    @Getter
    private final long creationMillis;

    private final long expiryMillis;

    private final PersistableLocation deathPoint;

    @Getter
    private final boolean eotwDeathban;

    public Deathban(String reason, long duration, PersistableLocation deathPoint, boolean eotwDeathban){
        this.reason = reason;
        this.creationMillis = System.currentTimeMillis();
        this.expiryMillis = this.creationMillis + duration;
        this.deathPoint = deathPoint;
        this.eotwDeathban = eotwDeathban;
    }

    public Deathban(Map<String, Object> map){
        this.reason = (String) map.get("reason");
        this.creationMillis = Long.parseLong((String) map.get("creationMillis"));
        this.expiryMillis = Long.parseLong((String) map.get("expiryMillis"));

        this.deathPoint = (PersistableLocation) map.get("deathPoint");
        this.eotwDeathban = (Boolean) map.get("eotwDeathban");
    }

    public Deathban(Document document){
        reason = document.getString("reason");
        creationMillis = document.getLong("creationMillis");
        expiryMillis = document.getLong("expiryMillis");
        deathPoint = new PersistableLocation(document.get("deathPoint", Document.class));
        eotwDeathban = document.getBoolean("eotwDeathban");
    }

    @Override
    public Map<String, Object> serialize(){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("reason", reason);
        map.put("creationMillis", Long.toString(creationMillis));
        map.put("expiryMillis", Long.toString(expiryMillis));
        if(deathPoint != null){
            map.put("deathPoint", deathPoint);
        }

        map.put("eotwDeathban", eotwDeathban);
        return map;
    }

    @Override
    public Document toDocument(){
        Document document = new Document();
        document.put("reason", reason);
        document.put("creationMillis", creationMillis);
        document.put("expiryMillis", expiryMillis);
        document.put("deathPoint", deathPoint.toDocument());
        document.put("eotwDeathban", eotwDeathban);
        return document;
    }

    /**
     * Gets the initial duration of this {@link Deathban} in milliseconds.
     *
     * @return the initial duration
     */
    public long getInitialDuration(){
        return expiryMillis - creationMillis;
    }

    /**
     * Checks if this {@link Deathban} is active.
     *
     * @return true if is active
     */
    public boolean isActive(){
        return eotwDeathban || getRemaining() > 0L;
    }

    /**
     * Gets the remaining time in milliseconds until this {@link Deathban}
     * is no longer active.
     *
     * @return the remaining time until expired
     */
    public long getRemaining(){
        return expiryMillis - System.currentTimeMillis();
    }

    /**
     * Gets the {@link Location} where this player died during {@link Deathban}.
     *
     * @return death {@link Location}
     */
    public Location getDeathPoint(){
        return deathPoint == null ? null : deathPoint.getLocation();
    }
}
