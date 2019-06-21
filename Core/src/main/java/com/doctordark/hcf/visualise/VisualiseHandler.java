package com.doctordark.hcf.visualise;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import technology.brk.util.cuboid.Cuboid;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class VisualiseHandler{

    private final Table<UUID, Location, VisualBlock> storedVisualises = HashBasedTable.create();

    public VisualBlock getVisualBlockAt(Player player, Location location) throws NullPointerException{
        return storedVisualises.get(player.getUniqueId(), location);
    }

    public LinkedHashMap<Location, VisualBlockData> generate(Player player, Cuboid cuboid, VisualType visualType, boolean canOverwrite){
        Collection<Location> locations = new HashSet<>(cuboid.getSizeX() * cuboid.getSizeY() * cuboid.getSizeZ());
        Iterator<Location> iterator = cuboid.locationIterator();
        while(iterator.hasNext()){
            locations.add(iterator.next());
        }

        LinkedHashMap<Location, VisualBlockData> results = new LinkedHashMap<>();
        ArrayList<VisualBlockData> filled = visualType.blockFiller().bulkGenerate(player, locations);

        if(filled != null){
            int count = 0;
            Map<Location, MaterialData> updatedBlocks = new HashMap<>();
            for(Location location : locations){
                if(!canOverwrite && storedVisualises.contains(player.getUniqueId(), location)){
                    continue;
                }

                Material previousType = location.getBlock().getType();
                if(previousType.isSolid() || previousType != Material.AIR){
                    continue;
                }

                VisualBlockData visualBlockData = filled.get(count++);
                results.put(location, visualBlockData);
                updatedBlocks.put(location, visualBlockData);
                storedVisualises.put(player.getUniqueId(), location, new VisualBlock(visualType, visualBlockData, location));
            }

            try{
                VisualiseUtil.handleBlockChanges(player, updatedBlocks);
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }

        return results;
    }

    public void clearVisualBlock(Player player, Location location, boolean sendRemovalPacket){
        VisualBlock visualBlock = storedVisualises.remove(player.getUniqueId(), location);
        if(sendRemovalPacket && visualBlock != null){
            // Have to send a packet to the original block type, don't send if the fake block has the same data properties though.
            Block block = location.getBlock();
            VisualBlockData visualBlockData = visualBlock.getBlockData();
            if(visualBlockData.getBlockType() != block.getType() || visualBlockData.getData() != block.getData()){
                player.sendBlockChange(location, block.getType(), block.getData());
            }
        }
    }

    public void clearVisualBlocks(Chunk chunk){
        if(!storedVisualises.isEmpty()){
            Set<Location> keys = storedVisualises.columnKeySet();
            for(Location location : new HashSet<>(keys)){
                if(location.getWorld().equals(chunk.getWorld()) && chunk.getX() == (((int) location.getX()) >> 4) && chunk.getZ() == (((int) location.getZ()) >> 4)){
                    keys.remove(location);
                }
            }
        }
    }

    public void clearVisualBlocks(Player player, @Nullable VisualType visualType, @Nullable Predicate<VisualBlock> predicate){
        clearVisualBlocks(player, visualType, predicate, true);
    }

    @Deprecated
    public void clearVisualBlocks(Player player,
                                  @Nullable VisualType visualType,
                                  @Nullable Predicate<VisualBlock> predicate,
                                  boolean sendRemovalPackets){

        if(!storedVisualises.containsRow(player.getUniqueId())){
            return;
        }

        Map<Location, VisualBlock> results = new HashMap<>(storedVisualises.row(player.getUniqueId())); // copy to prevent commodification
        Map<Location, VisualBlock> removed = new HashMap<>();
        for(Map.Entry<Location, VisualBlock> entry : results.entrySet()){
            VisualBlock visualBlock = entry.getValue();
            if((predicate == null || predicate.test(visualBlock)) && (visualType == null || visualBlock.getVisualType() == visualType)){
                Location location = entry.getKey();
                if(removed.put(location, visualBlock) == null){ // not really necessary, but might as well
                    clearVisualBlock(player, location, sendRemovalPackets); // this will call remove on storedVisualises.
                }
            }
        }
    }
}
