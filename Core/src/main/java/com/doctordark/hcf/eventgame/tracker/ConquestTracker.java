package com.doctordark.hcf.eventgame.tracker;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventTimer;
import com.doctordark.hcf.eventgame.EventType;
import com.doctordark.hcf.eventgame.faction.ConquestFaction;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import com.doctordark.util.ConcurrentValueOrderedMap;
import com.doctordark.util.Permissions;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.hcgames.hcfactions.event.faction.FactionRemoveEvent;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import technology.brk.base.GuavaCompat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Tracker used for handling the Conquest points.
 */
@Deprecated
public class ConquestTracker implements EventTracker, Listener{

    public static final long DEFAULT_CAP_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    /**
     * Minimum time the KOTH has to be controlled before this tracker will announce when control has been lost.
     */
    private static final long MINIMUM_CONTROL_TIME_ANNOUNCE = TimeUnit.SECONDS.toMillis(5L);
    private final ConcurrentValueOrderedMap<PlayerFaction, Integer> factionPointsMap = new ConcurrentValueOrderedMap<>();
    private final Map<PlayerFaction, Integer> factionsPointsDifference = ExpiringMap.builder().expiration(7, TimeUnit.SECONDS).build();

    private final HCF plugin;

    public ConquestTracker(HCF plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRemove(FactionRemoveEvent event){
        Faction faction = event.getFaction();
        if(faction instanceof PlayerFaction){
            this.factionPointsMap.remove((PlayerFaction) faction);
        }
    }

    /**
     * Gets the map containing the points for all factions.
     *
     * @return immutable copy of the faction points map
     */
    public ConcurrentValueOrderedMap<PlayerFaction, Integer> getFactionPointsMap(){
        return this.factionPointsMap;
    }

    /**
     * Gets the amount of points a {@link PlayerFaction} has
     * gained for this {@link ConquestTracker}.
     *
     * @param faction the faction to get for
     * @return the new points of the {@link PlayerFaction}.
     */
    public int getPoints(PlayerFaction faction){
        return GuavaCompat.firstNonNull(this.factionPointsMap.get(faction), 0);
    }

    /**
     * Sets the points a {@link PlayerFaction} has gained for this {@link ConquestTracker}.
     *
     * @param faction the faction to set for
     * @param amount  the amount to set
     * @return the new points of the {@link PlayerFaction}
     */
    public int setPoints(PlayerFaction faction, int amount){
        if(factionPointsMap.containsKey(faction)){
            int old = factionPointsMap.getOrDefault(faction, 1);
            factionsPointsDifference.put(faction, amount < 0 ? amount - old : (old > amount ? old - amount : amount - old));
        }else{
            factionsPointsDifference.put(faction, 1);
        }

        this.factionPointsMap.put(faction, amount);
        return amount;
    }

    /**
     * Takes points from a {@link PlayerFaction} gained from this {@link ConquestTracker}.has
     *
     * @param faction the faction to take from
     * @param amount  the amount to take
     * @return the new points of the {@link PlayerFaction}
     */
    public int takePoints(PlayerFaction faction, int amount){
        return setPoints(faction, getPoints(faction) - amount);
    }

    /**
     * Adds points to a {@link PlayerFaction} gained from this {@link ConquestTracker}.has
     *
     * @param faction the faction to add from
     * @param amount  the amount to add
     * @return the new points of the {@link PlayerFaction}
     */
    public int addPoints(PlayerFaction faction, int amount){
        return setPoints(faction, getPoints(faction) + amount);
    }

    public int getPointsDiff(PlayerFaction faction){
        return factionsPointsDifference.getOrDefault(faction, 0);
    }

    @Override
    public EventType getEventType(){
        return EventType.CONQUEST;
    }

    @Override
    public void tick(EventTimer eventTimer, EventFaction eventFaction){
        ConquestFaction conquestFaction = (ConquestFaction) eventFaction;
        List<CaptureZone> captureZones = conquestFaction.getCaptureZones();
        for(CaptureZone captureZone : captureZones){
            captureZone.updateScoreboardRemaining();
            Player cappingPlayer = captureZone.getCappingPlayer();
            if(cappingPlayer == null) continue;

            // The capture zone has been controlled.
            long remainingMillis = captureZone.getRemainingCaptureMillis();
            if(remainingMillis <= 0L){
                UUID uuid = cappingPlayer.getUniqueId();

                PlayerFaction playerFaction = null;
                try{
                    playerFaction = plugin.getFactions().getFactionManager().getPlayerFaction(uuid);
                }catch(NoFactionFoundException e){
                }
                if(playerFaction != null){
                    int newPoints = addPoints(playerFaction, 1);
                    if(newPoints < plugin.getConfiguration().getConquestRequiredVictoryPoints()){
                        // Reset back to the default for this tracker.
                        captureZone.setRemainingCaptureMillis(captureZone.getDefaultCaptureMillis());
                        Bukkit.broadcastMessage(plugin.getMessagesOld().getString("Event-Conquest-CappingAreaBroadcast")
                                .replace("{eventFactionName}", eventFaction.getName())
                                .replace("{factionName}", playerFaction.getName())
                                .replace("{captureZoneName}", captureZone.getDisplayName())
                                .replace("{newCapturePoints}", String.valueOf(newPoints))
                                .replace("{requiredVictoryPoints}", String.valueOf(plugin.getConfiguration().getConquestRequiredVictoryPoints())));
                    }else{
                        // Clear all the points for the next Conquest event.
                        this.factionPointsMap.clear();
                        plugin.getTimerManager().getEventTimer().handleWinner(cappingPlayer);
                        return;
                    }
                }
                return;
            }

            int remainingSeconds = (int) Math.round((double) remainingMillis / 1000L);
            if(remainingSeconds % 5 == 0){
                cappingPlayer.sendMessage(plugin.getMessagesOld().getString("Event-Conquest-CappingArea")
                        .replace("{eventFactionName}", eventFaction.getName())
                        .replace("{captureZoneName}", captureZone.getDisplayName())
                        .replace("{remainingTime}", String.valueOf(remainingSeconds)));
            }
        }
    }

    @Override
    public void onContest(EventFaction eventFaction, EventTimer eventTimer){
        Bukkit.broadcastMessage(ChatColor.YELLOW + (eventFaction instanceof ConquestFaction ? "" : "[" + eventFaction.getName() + "] ") + ChatColor.GOLD + eventFaction.getName() + " can now be contested.");
    }

    @Override
    public boolean onControlTake(Player player, CaptureZone captureZone){
        if(player.hasPermission(Permissions.STAFF_IDENTIFIER)){
            return false;
        }

        try{
            plugin.getFactions().getFactionManager().getPlayerFaction(player.getUniqueId());
        }catch(NoFactionFoundException e){
            player.sendMessage(ChatColor.RED + "You must be in a faction to capture Conquest.");
            return false;
        }

        return true;
    }

    @Override
    public void onControlLoss(Player player, CaptureZone captureZone, EventFaction eventFaction){
        long remainingMillis = captureZone.getRemainingCaptureMillis();
        if(remainingMillis > 0L && captureZone.getDefaultCaptureMillis() - remainingMillis > MINIMUM_CONTROL_TIME_ANNOUNCE){
            Bukkit.broadcastMessage(ChatColor.YELLOW + "[" + eventFaction.getName() + "] " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + player.getName() +
                    ChatColor.GOLD + " was knocked off " + captureZone.getDisplayName() + ChatColor.GOLD + '.');
        }
    }

    @Override
    public void stopTiming(){
        factionPointsMap.clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event){
        Faction currentEventFac = plugin.getTimerManager().getEventTimer().getEventFaction();
        if(currentEventFac instanceof ConquestFaction){
            Player player = event.getEntity();
            PlayerFaction playerFaction;
            try{
                playerFaction = plugin.getFactions().getFactionManager().getPlayerFaction(player);
            }catch(NoFactionFoundException e){
                return;
            }
            if(playerFaction != null && plugin.getConfiguration().getConquestPointLossPerDeath() > 0){
                int oldPoints = getPoints(playerFaction);
                if(oldPoints == 0) return;

                int newPoints = takePoints(playerFaction, plugin.getConfiguration().getConquestPointLossPerDeath());
                event.setDeathMessage(null); // for some reason if it isn't handled manually, weird colour coding happens
                Bukkit.getServer().broadcastMessage(plugin.getMessagesOld().getString("Event-Conquest-PlayerDeathBroadcast")
                        .replace("{eventFactionName}", currentEventFac.getName())
                        .replace("{playerFactionName}", playerFaction.getName())
                        .replace("{deathLostPoints}", String.valueOf(plugin.getConfiguration().getConquestPointLossPerDeath()))
                        .replace("{player}", player.getName())
                        .replace("{newCapturePoints}", String.valueOf(newPoints))
                        .replace("{requiredVictoryPoints}", String.valueOf(plugin.getConfiguration().getConquestRequiredVictoryPoints())));
            }
        }
    }
}
