package com.doctordark.hcf.listener.fixes;

import com.google.common.collect.HashMultimap;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Copyright SystemUpdate (https://systemupdate.io) to present.
 * Please see included licence file for licensing terms.
 * File created on 06/04/2016.
 */

public class PearlDeathGlitchListener implements Listener{

    private final HashMultimap<UUID, Projectile> enderpearls = HashMultimap.create();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event){
        if(!(event.getEntity() instanceof EnderPearl) && !(event.getEntity().getShooter() instanceof Player)){
            return;
        }

        Player shooter = (Player) event.getEntity().getShooter();
        enderpearls.put(shooter.getUniqueId(), event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event){
        if(event.getEntity() == null || !(event.getEntity() instanceof EnderPearl)
                || event.getEntity().getShooter() instanceof Player){
            return;
        }

        Player shooter = (Player) event.getEntity().getShooter();
        enderpearls.remove(shooter.getUniqueId(), event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event){
        enderpearls.removeAll(event.getEntity().getUniqueId()).forEach(Entity::remove);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        enderpearls.removeAll(event.getPlayer().getUniqueId());
    }

}
