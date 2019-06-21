package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class KitMapListener implements Listener{

    final HCF plugin;

    public KitMapListener(HCF plugin){
        this.plugin = plugin;
    }

//    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
//    public void onFactionClaimChange(FactionClaimChangeEvent event){
//        if(event.getReason() == ClaimChangeEvent.ClaimChangeReason.CLAIM && plugin.getConfiguration().isKitMap() && event.getFaction() instanceof PlayerFaction){
//            event.setCancelled(true);
//            event.getSender().sendMessage(ChatColor.RED + "Player based land cannot be claimed during a kit map.");
//        }
//    }

    //TODO: Memory leak
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event){
        if(plugin.getConfiguration().isKitMap()){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event){
        if(plugin.getConfiguration().isKitMap() && plugin.getFactions().getFactionManager().getFactionAt(event.getItemDrop().getLocation()).isSafezone()){
            event.getItemDrop().remove();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onItemSpawn(ItemSpawnEvent event){
        if(plugin.getConfiguration().isKitMap() && plugin.getFactions().getFactionManager().getFactionAt(event.getLocation()).isSafezone()){
            event.getEntity().remove();
        }
    }
}

