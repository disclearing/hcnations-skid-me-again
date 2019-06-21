package com.doctordark.hcf.timer.type;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.timer.PlayerTimer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import technology.brk.util.DurationFormatter;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LogoutTimer extends PlayerTimer implements Listener{

    private String scoreboardPrefix = null;

    public LogoutTimer(){
        super("Logout", TimeUnit.SECONDS.toMillis(30L), false);

        scoreboardPrefix = HCF.getPlugin().getMessagesOld().getString("Timer-" + name.replace(" ", "") + "-SBPrefix");

        if(scoreboardPrefix == null || scoreboardPrefix.isEmpty() || scoreboardPrefix.equals("Error! Please contact an administrator")){
            scoreboardPrefix = null;
        }
    }

    @Override
    public String getScoreboardPrefix(){
        if(!(scoreboardPrefix == null)){
            return scoreboardPrefix;
        }

        return ChatColor.RED.toString() + ChatColor.BOLD;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event){
        Location from = event.getFrom();
        Location to = event.getTo();
        if(from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()){
            return;
        }

        Player player = event.getPlayer();
        if(getRemaining(player) > 0L){
            //player.sendMessage(ChatColor.RED + "You moved a block, " + getDisplayName() + ChatColor.RED + " timer cancelled.");
            player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Timer-Logout-MovedABlock")
                    .replace("{timerName}", getDisplayName()));
            clearCooldown(player);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        onPlayerMove(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event){
        UUID uuid = event.getPlayer().getUniqueId();
        if(getRemaining(event.getPlayer().getUniqueId()) > 0L){
            clearCooldown(event.getPlayer(), uuid);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        UUID uuid = event.getPlayer().getUniqueId();
        if(getRemaining(event.getPlayer().getUniqueId()) > 0L){
            clearCooldown(event.getPlayer(), uuid);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof Player){
            Player player = (Player) entity;
            if(getRemaining(player) > 0L){
                player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Timer-Logout-TookDamage")
                        .replace("{timerName}", getDisplayName()));
                clearCooldown(player);
                return;
            }
        }

        if(event instanceof EntityDamageByEntityEvent){
            EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent) event;
            Entity damager = ede.getDamager();
            if(damager instanceof Player && entity instanceof Player){
                Player attacker = (Player) damager;
                if(getRemaining(attacker) > 0L){
                    attacker.sendMessage(ChatColor.RED + "You cannot attack players during logout, logout cancelled.");
                    attacker.sendMessage(HCF.getPlugin().getMessagesOld().getString("Timer-Logout-AttackedPlayers")
                            .replace("{timerName}", getDisplayName()));
                    clearCooldown(attacker);
                }
            }
        }
    }

    @Override
    public void handleExpiry(@Nullable Player player, UUID userUUID){
        if(player != null){
            HCF.getPlugin().getCombatLogListener().safelyDisconnect(player, HCF.getPlugin().getMessagesOld().getString("Timer-Logout-LoggedOut")
                    .replace("{timerName}", getDisplayName()));
        }
    }

    public void run(Player player){
        long remainingMillis = getRemaining(player);
        if(remainingMillis > 0L){
            player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Timer-Logout-AttackedPlayers")
                    .replace("{timeLeft}", DurationFormatter.getRemaining(remainingMillis, true, false))
                    .replace("{timerName}", getDisplayName()));
        }
    }
}
