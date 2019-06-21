package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Julio on 7/23/2016.
 */
public class ElevatorListener implements Listener{

    @EventHandler
    public void onMinecartExit(VehicleExitEvent event){
        if(event.getVehicle() instanceof Minecart && event.getExited() instanceof Player){
            Player player = (Player) event.getExited();
            Location location = event.getVehicle().getLocation();

            if(location.clone().getBlock().getType() != Material.FENCE_GATE) return;

            new BukkitRunnable(){ //We need to create a new thread for this operation
                @Override
                public void run(){
                    player.teleport(teleportSpot(event.getVehicle().getLocation(), event.getVehicle().getLocation().getBlockY(), 254));
                }
            }.runTaskLater(HCF.getPlugin(), 1L);
        }
    }

    public Location teleportSpot(final Location loc, final int min, final int max){
        for(int k = min; k < max; ++k){
            Material first = new Location(loc.getWorld(), loc.getBlockX(), k, loc.getBlockZ()).getBlock().getType();
            Material second = new Location(loc.getWorld(), loc.getBlockX(), (k + 1), loc.getBlockZ()).getBlock().getType();
            if(first == Material.AIR && second == Material.AIR){
                return new Location(loc.getWorld(), loc.getBlockX(), k, loc.getBlockZ());
            }
        }
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()), loc.getBlockZ());
    }

}
