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

package com.doctordark.hcf.economy;

import com.doctordark.hcf.HCF;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MongoEconomyManager implements EconomyManager{

    private final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private final MongoCollection<Document> collection;
    private final HCF plugin;

    public MongoEconomyManager(HCF plugin){
        collection = plugin.getMongoManager().getDatabase().getCollection("balances");
        reloadEconomyData();
        this.plugin = plugin;
    }

    @Override
    public Map<UUID, Double> getBalanceMap(){
        return balances;
    }

    @Override
    public double getBalance(UUID uuid){
        return balances.getOrDefault(uuid, 0.0);
    }

    @Override
    public double setBalance(UUID uuid, double amount){
        balances.put(uuid, amount);
        plugin.getServer().getPluginManager().callEvent(new BalanceUpdateEvent(uuid, amount));
        return amount;
    }

    @Override
    public double addBalance(UUID uuid, double amount){
        return setBalance(uuid, getBalance(uuid) + amount);
    }

    @Override
    public double subtractBalance(UUID uuid, double amount){
        return setBalance(uuid, getBalance(uuid) - amount);
    }

    @Override
    public void reloadEconomyData(){
        for(Document document : collection.find()){
            UUID uuid = UUID.fromString(document.getString("_id"));

            if(document.containsKey("balance")){
                Object value = document.get("balance");

                if(value instanceof Integer){
                    balances.put(uuid, Double.valueOf((Integer) value));
                }else if(value instanceof Double){
                    balances.put(uuid, (Double) value);
                }else{
                    throw new RuntimeException("Unknown numeric type " + value.getClass().getSimpleName() + value);
                }
            }
        }
    }

    @Override
    public void saveEconomyData(){
        Iterator<Map.Entry<UUID, Double>> iterator = balances.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<UUID, Double> entry = iterator.next();
            collection.updateOne(new Document("_id", entry.getKey().toString()), new Document("$set", new Document("balance", entry.getValue())), new UpdateOptions().upsert(true));
        }
    }

    @Override
    public boolean isLoaded(UUID uuid){
        return balances.containsKey(uuid);
    }
}
