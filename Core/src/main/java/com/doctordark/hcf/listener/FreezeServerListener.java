package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Created by Sam on 28/09/2016.
 */
@RequiredArgsConstructor
public class FreezeServerListener implements Listener{

    private final HCF plugin;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(plugin.isServerFrozen() && !event.getPlayer().hasPermission("freezeserver.bypass")){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "The server is frozen.");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(plugin.isServerFrozen() && !event.getPlayer().hasPermission("freezeserver.bypass")){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "The server is frozen.");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        if((event.getFrom().getBlockX() == event.getTo().getBlockX()) && (event.getFrom().getBlockZ() == event.getTo().getBlockZ())){
            return;
        }

        if(plugin.isServerFrozen() && !event.getPlayer().hasPermission("freezeserver.bypass")){
            event.setTo(event.getFrom().getBlock().getLocation());
            event.getPlayer().sendMessage(ChatColor.RED + "The server is frozen.");
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event){
        if(plugin.isServerFrozen()){
            event.setCancelled(true);
            event.getAffectedEntities().clear();
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event){
        if(plugin.isServerFrozen() && !event.getPlayer().hasPermission("freezeserver.bypass")){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "The server is frozen.");
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event){
        if(plugin.isServerFrozen() && !event.getPlayer().hasPermission("freezeserver.bypass")){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event){
        if(plugin.isServerFrozen() && !event.getPlayer().hasPermission("freezeserver.bypass")){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "The server is frozen.");
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event){
        if(plugin.isServerFrozen() && !event.getPlayer().hasPermission("freezeserver.bypass")){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "The server is frozen.");
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event){
        if(plugin.isServerFrozen()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(plugin.isServerFrozen()){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
        if(plugin.isServerFrozen() && !event.getPlayer().hasPermission("freezeserver.bypass")){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "The server is frozen.");
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
        if(plugin.isServerFrozen() && !event.getPlayer().hasPermission("freezeserver.bypass")){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPreprocessCommand(PlayerCommandPreprocessEvent event){
        if(plugin.isServerFrozen() && !event.getPlayer().hasPermission("freezeserver.bypass")){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "The server is frozen.");
        }
    }

}
