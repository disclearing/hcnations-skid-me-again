/*
 * Copyright (C) 2016 SystemUpdate (https://systemupdate.io) All Rights Reserved
 */

package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.user.FactionUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NightVisionListener implements Listener{

    private final PotionEffect NIGHT_VISION_EFFECT = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true);
    private final HCF plugin;

    public NightVisionListener(HCF plugin){
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event){
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            FactionUser user = plugin.getUserManager().getUserAsync(event.getPlayer().getUniqueId());

            if(user == null){
                return;
            }

            if(user.hasNightVisionEnabled()){
                if(!event.getPlayer().hasPermission("hcf.command.nightvision")){
                    user.setNightVisionEnabled(false);
                    return;
                }

                event.getPlayer().addPotionEffect(NIGHT_VISION_EFFECT);
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            FactionUser user = plugin.getUserManager().getUserAsync(event.getPlayer().getUniqueId());

            if(user == null){
                return;
            }

            if(user.hasNightVisionEnabled()){
                event.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event){
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            FactionUser user = plugin.getUserManager().getUserAsync(event.getPlayer().getUniqueId());

            if(user == null){
                return;
            }

            if(user.hasNightVisionEnabled()){
                event.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event){
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            FactionUser user = plugin.getUserManager().getUserAsync(event.getPlayer().getUniqueId());

            if(user == null){
                return;
            }

            if(user.hasNightVisionEnabled()){
                if(!event.getPlayer().hasPermission("hcf.command.nightvision")){
                    user.setNightVisionEnabled(false);
                    return;
                }

                event.getPlayer().addPotionEffect(NIGHT_VISION_EFFECT);
            }
        });
    }
}
