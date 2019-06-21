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

package com.doctordark.hcf.invrestore;

import com.doctordark.hcf.HCF;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.faction.PlayerFaction;
import technology.brk.util.file.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class InvManager{

    //TODO: Switch restore on join to use redis, or edit the document with boolean
    private final List<UUID> restoreOnJoin = new ArrayList<>();
    private final Config config;

    private final MongoCollection<Document> inventoriesCollection;
    private final HCF plugin;

    public InvManager(HCF plugin){
        this.plugin = plugin;
        inventoriesCollection = plugin.getMongoManager().getDatabase().getCollection("inventories");

        this.config = new Config(plugin, "inventories-roj");
        if(config.contains("restoreonjoin")){
            restoreOnJoin.addAll(config.getStringList("restoreonjoin").stream().map(UUID::fromString).collect(Collectors.toList()));
        }
    }

    boolean shouldRestoreOnJoin(UUID uuid){
        return restoreOnJoin.contains(uuid);
    }

    void setShouldRestoreOnJoin(UUID uuid, boolean should){
        if(should && !restoreOnJoin.contains(uuid)){
            restoreOnJoin.add(uuid);
        }else if(!should && restoreOnJoin.contains(uuid)){
            restoreOnJoin.remove(uuid);
        }
    }

    void storeState(InventoryState state){
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> inventoriesCollection.updateOne(Filters.eq("_id", state.getOwner().toString()), new Document("$set", state.toDocument()), new UpdateOptions().upsert(true)));
    }

    public InventoryState getState(UUID uuid){
        Document document = inventoriesCollection.find(Filters.eq("_id", uuid.toString())).limit(1).first();
        if(document == null) return null;
        return new InventoryState(document);
    }

    void restore(Player player){
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            InventoryState state = getState(player.getUniqueId());
            if(state != null) restore(player, state);
        });
    }

    void restore(Player player, InventoryState state){
        if(!plugin.getConfiguration().isKitMap() && plugin.getFactions().getFactionManager().hasFaction(player)){
            PlayerFaction playerFaction =  plugin.getFactions().getFactionManager().getPlayerFaction(player.getUniqueId());
            playerFaction.setDeathsUntilRaidable(Math.min(playerFaction.getMaximumDeathsUntilRaidable(), playerFaction.getDeathsUntilRaidable() + 1));
            if(playerFaction.getDeathsUntilRaidable() == playerFaction.getMaximumDeathsUntilRaidable() && playerFaction.getRemainingRegenerationTime() > 0){
                playerFaction.setRemainingRegenerationTime(0L);
            }
        }

        state.restore(player);
        inventoriesCollection.deleteOne(Filters.eq("_id", player.getUniqueId().toString()));
    }

    public void save(){
        config.set("restoreonjoin", restoreOnJoin.stream().map(UUID::toString).collect(Collectors.toList()));
        config.save();
    }
}
