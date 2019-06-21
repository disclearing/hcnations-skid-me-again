package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FastBrewListener implements Listener{

    private final HCF plugin;

    private final Table<Chunk, Location, BrewingStand> brewingStands;

    public FastBrewListener(HCF plugin){
        this.plugin = plugin;

        brewingStands = HashBasedTable.create();
        if(plugin.getConfiguration().getPotionBrewSpeedMultiplier() != 0){
            new BrewerTick().runTaskTimer(plugin, 1L, 1L);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event){
        if(plugin.getConfiguration().getPotionBrewSpeedMultiplier() > 0){
            Chunk chunk = event.getChunk();

            for(BlockState entity : chunk.getTileEntities()){
                if(entity instanceof BrewingStand){
                    brewingStands.put(chunk, entity.getLocation(), (BrewingStand) entity);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event){
        brewingStands.row(event.getChunk()).clear();
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();

        if(plugin.getConfiguration().getPotionBrewSpeedMultiplier() > 0 && block.getType() == Material.BREWING_STAND){
            Location location = block.getLocation();
            brewingStands.remove(location.getChunk(), location);
        }
    }

    private class BrewerTick extends BukkitRunnable{

        @Override
        public void run(){
            for(BrewingStand brewingStand : brewingStands.values()){
                if(brewingStand.getBrewingTime() > 1){
                    brewingStand.setBrewingTime(Math.max(1, brewingStand.getBrewingTime() - plugin.getConfiguration().getPotionBrewSpeedMultiplier()));
                }
            }
        }
    }
}
