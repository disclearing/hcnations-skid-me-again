package com.doctordark.hcf.timer.type;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.timer.PlayerTimer;
import com.doctordark.hcf.timer.TimerCooldown;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.structure.Relation;
import technology.brk.util.BukkitUtils;
import technology.brk.util.DurationFormatter;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class StuckTimer extends PlayerTimer implements Listener{

    // The maximum distance a player can move before
    // this timer will self-cancel
    public static final int MAX_MOVE_DISTANCE = 5;

    private final Map<UUID, Location> startedLocations = new HashMap<>();

    private String scoreboardPrefix = null;

    public StuckTimer(){
        super("Stuck", TimeUnit.MINUTES.toMillis(2L) + TimeUnit.SECONDS.toMillis(45L), false);

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

        return ChatColor.DARK_AQUA.toString() + ChatColor.BOLD;
    }

    @Override
    public TimerCooldown clearCooldown(@Nullable Player player, UUID uuid){
        TimerCooldown runnable = super.clearCooldown(player, uuid);
        if(runnable != null){
            startedLocations.remove(uuid);
        }

        return runnable;
    }

    @Override
    public boolean setCooldown(@Nullable Player player, UUID playerUUID, long millis, boolean force, @Nullable Predicate<Long> callback){
        if(player != null && super.setCooldown(player, playerUUID, millis, force, callback)){
            startedLocations.put(playerUUID, player.getLocation());
            return true;
        }

        return false;
    }

    private void checkMovement(Player player, Location from, Location to){
        UUID uuid = player.getUniqueId();
        if(getRemaining(uuid) > 0L){
            if(from == null){
                clearCooldown(player, uuid);
                return;
            }

            int xDiff = Math.abs(from.getBlockX() - to.getBlockX());
            int yDiff = Math.abs(from.getBlockY() - to.getBlockY());
            int zDiff = Math.abs(from.getBlockZ() - to.getBlockZ());
            if(xDiff > MAX_MOVE_DISTANCE || yDiff > MAX_MOVE_DISTANCE || zDiff > MAX_MOVE_DISTANCE){
                clearCooldown(player, uuid);
                player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Timer-Stuck-MovedTooFar")
                        .replace("{timerName}", getDisplayName())
                        .replace("{maxMoveDistance}", String.valueOf(MAX_MOVE_DISTANCE)));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(getRemaining(uuid) > 0L){
            Location from = startedLocations.get(uuid);
            checkMovement(player, from, event.getTo());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(getRemaining(uuid) > 0L){
            Location from = startedLocations.get(uuid);
            checkMovement(player, from, event.getTo());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event){
        UUID uuid = event.getPlayer().getUniqueId();
        if(getRemaining(event.getPlayer().getUniqueId()) > 0L){
            clearCooldown(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        UUID uuid = event.getPlayer().getUniqueId();
        if(getRemaining(event.getPlayer().getUniqueId()) > 0L){
            clearCooldown(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof Player){
            Player player = (Player) entity;
            if(getRemaining(player) > 0L){
                player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Timer-Stuck-TookDamage")
                        .replace("{timerName}", getDisplayName()));
                clearCooldown(player);
            }
        }
    }

    @Override
    public void handleExpiry(@Nullable Player player, UUID userUUID){
        if(player == null){
            player = Bukkit.getPlayer(userUUID);

            if(player == null){
                return;
            }
        }

        Claim claimAt = HCF.getPlugin().getFactions().getFactionManager().getClaimAt(player.getLocation());

        try{
            if(!claimAt.getFaction().getRelation(player).equals(Relation.ENEMY)){
                player.sendMessage(ChatColor.RED + "You can only f stuck from within enemy land.");
                return;
            }
        }catch(NoFactionFoundException e){
            player.sendMessage(ChatColor.RED + "You can only f stuck from within enemy land.");
            return;
        }

        Location nearest = getTeleportLocation(player);
        if(nearest == null){
            HCF.getPlugin().getCombatLogListener().safelyDisconnect(player, ChatColor.RED + HCF.getPlugin().getMessagesOld().getString("Timer-Stuck-NoSafeLocation"));
            return;
        }

        if(player.teleport(nearest, PlayerTeleportEvent.TeleportCause.PLUGIN)){
            player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Timer-Stuck-Teleported").replace("{timerName}", getDisplayName()));
        }
    }

    public void run(Player player){
        long remainingMillis = getRemaining(player);
        if(remainingMillis > 0L){
            player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Timer-Stuck-Teleported")
                    .replace("{timeLeft}", DurationFormatter.getRemaining(remainingMillis, true, false))
                    .replace("{timerName}", getDisplayName()));
        }
    }

    private Location getTeleportLocation(Player player){
        Claim claimAt = HCF.getPlugin().getFactions().getFactionManager().getClaimAt(player.getLocation());
        Location closest = null;

        for(Location claim : claimAt.getCornerLocations()){
            if(closest == null){
                closest = claim;
            }else{
                if(claim.distance(player.getLocation()) < closest.distance(player.getLocation())){
                    closest = claim;
                }
            }
        }

        if(closest == null){
            return null;
        }

        return BukkitUtils.getHighestLocation(closest).add(0, 1.5, 0);
    }
}
