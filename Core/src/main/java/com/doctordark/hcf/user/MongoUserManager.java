/*
 * Copyright (C) 2016 SystemUpdate (https://systemupdate.io) All Rights Reserved
 */

package com.doctordark.hcf.user;

import com.doctordark.hcf.HCF;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bukkit.event.Listener;
import technology.brk.base.GuavaCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//TODO: Caching users in redis, load in when asked for. (Caching entire Java objects into Redis)
public class MongoUserManager implements UserManager, Listener{

    private final Map<UUID, FactionUser> users = new HashMap<>();
    private final MongoCollection<Document> collection;

    public MongoUserManager(HCF plugin){
        collection = plugin.getMongoManager().getDatabase().
                getCollection("users");
        reloadUserData();
    }

    @Override
    public Map<UUID, FactionUser> getUsers(){
        return users;
    }

    @Override
    public FactionUser getUserAsync(UUID uuid){
        synchronized(users){
            FactionUser revert;
            FactionUser user = users.putIfAbsent(uuid, revert = new FactionUser(uuid));
            return GuavaCompat.firstNonNull(user, revert);
        }
    }

    @Override
    public FactionUser getUser(UUID uuid){
        FactionUser revert;
        FactionUser user = users.putIfAbsent(uuid, revert = new FactionUser(uuid));
        return GuavaCompat.firstNonNull(user, revert);
    }

    @Override
    public void reloadUserData(){
        int loaded = 0;

        for(Document document : collection.find()){
            FactionUser user = new FactionUser(document);
            users.put(UUID.fromString(document.getString("_id")), user);
            loaded++;
        }

        System.out.println("Loaded " + loaded + " profiles.");
    }

    @Override
    public void saveUserData(){
        System.out.println("Saving " + users.size() + " users...");
        int saved = 0;

        //TODO: Use bulkWrite
        for(Map.Entry<UUID, FactionUser> user : users.entrySet()){
            FactionUser factionUser = user.getValue();
            if(factionUser.needsSaving()){
                Document document = factionUser.toDocument();
                document.remove("lastSaveHash");
                document.put("lastSaveHash", document.hashCode());
                collection.updateOne(new Document("_id", document.getString("_id")), new Document("$set", document), new UpdateOptions().upsert(true));
                saved++;
            }
        }

        System.out.println("Saved " + saved + " users... (Skipped " + String.valueOf(users.size() - saved) + ")");
    }

    @Override
    public boolean userExists(UUID uuid){
        return users.containsKey(uuid);
    }
}
