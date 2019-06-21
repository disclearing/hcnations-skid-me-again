package com.doctordark.hcf.economy;

import org.bukkit.configuration.MemorySection;
import org.bukkit.plugin.java.JavaPlugin;
import technology.brk.util.file.Config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the {@link FlatFileEconomyManager} storing to YAML.
 */
public class FlatFileEconomyManager implements EconomyManager{

    private final JavaPlugin plugin;

    private Map<UUID, Double> balanceMap = new ConcurrentHashMap<>();
    private Config balanceConfig;

    public FlatFileEconomyManager(JavaPlugin plugin){
        this.plugin = plugin;
        reloadEconomyData();
    }

    @Override
    public Map<UUID, Double> getBalanceMap(){
        return balanceMap;
    }

    @Override
    public double getBalance(UUID uuid){
        return balanceMap.getOrDefault(uuid, 0.0);
    }

    @Override
    public double setBalance(UUID uuid, double amount){
        balanceMap.put(uuid, amount);
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
        balanceConfig = new Config(plugin, "balances");
        Object object = balanceConfig.get("balances");
        if(object instanceof MemorySection){
            MemorySection section = (MemorySection) object;
            Set<String> keys = section.getKeys(false);
            for(String id : keys){
                balanceMap.put(UUID.fromString(id), balanceConfig.getDouble("balances." + id));
            }
        }
    }

    @Override
    public void saveEconomyData(){
        Map<String, Double> saveMap = new LinkedHashMap<>(balanceMap.size());
        balanceMap.forEach((uuid, i) -> saveMap.put(uuid.toString(), i));

        balanceConfig.set("balances", saveMap);
        balanceConfig.save();
    }

    @Override
    public boolean isLoaded(UUID uuid){
        return balanceMap.containsKey(uuid);
    }
}
