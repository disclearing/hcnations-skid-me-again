/*
 * Copyright (C) 2016 SystemUpdate (https://systemupdate.io) All Rights Reserved
 */

package com.doctordark.hcf.pvpclass.archer;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import net.minecraft.util.io.netty.util.internal.ConcurrentSet;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class Marks{

    private final ConcurrentSet<UUID> markedPlayers = new ConcurrentSet<>();

    @Getter
    private final MarksTimer timer;

    @Getter
    private final UUID archerUUID;

    private final ArcherClass clazz;

    public Marks(ArcherClass clazz, UUID archerUUID){
        timer = new MarksTimer("archerMarks-" + archerUUID.toString());
        this.archerUUID = archerUUID;
        this.clazz = clazz;
    }

    public void markPlayer(Player player){
        markPlayer(player.getUniqueId());
    }

    public void markPlayer(UUID uuid){
        markedPlayers.add(uuid);
        timer.setCooldown(uuid);
    }

    public void removeMarkedPlayer(Player player){
        removeMarkedPlayer(player.getUniqueId(), false);
    }

    public void removeMarkedPlayer(UUID uuid, boolean c){
        markedPlayers.remove(uuid);

        if(c){
            clazz.marked.remove(uuid);
            Player player = clazz.plugin.getServer().getPlayer(uuid);

            for(Player op : clazz.plugin.getServer().getOnlinePlayers()){
                clazz.plugin.getScoreboardHandler().getBoard(op).addUpdate(player);
            }
        }
    }

    public boolean isMarked(Player player){
        return isMarked(player.getUniqueId());
    }

    public boolean isMarked(UUID uuid){
        return markedPlayers.contains(uuid);
    }

    public void refreshTimer(Player player){
        timer.setCooldown(player.getUniqueId());
    }

    public String getTimerName(){
        return timer.getName();
    }

    public Set<UUID> getMarkedPlayers(){
        return ImmutableSet.copyOf(markedPlayers);
    }
}
