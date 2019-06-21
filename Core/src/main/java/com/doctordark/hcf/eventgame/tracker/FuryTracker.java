/*
 *   COPYRIGHT NOTICE
 *
 *   Copyright (C) 2016, SystemUpdate, <admin@systemupdate.io>.
 *
 *   All rights reserved.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS. IN
 *   NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *   DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 *   OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 *   OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *   Except as contained in this notice, the name of a copyright holder shall not
 *   be used in advertising or otherwise to promote the sale, use or other dealings
 *   in this Software without prior written authorization of the copyright holder.
 */

package com.doctordark.hcf.eventgame.tracker;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventTimer;
import com.doctordark.hcf.eventgame.EventType;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import com.doctordark.hcf.eventgame.faction.FuryFaction;
import com.doctordark.util.ConcurrentValueOrderedMap;
import com.doctordark.util.Permissions;
import lombok.Getter;
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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class FuryTracker implements EventTracker, Listener{

    public static final long DEFAULT_CAP_MILLIS = TimeUnit.SECONDS.toMillis(25L);
    private static final long MINIMUM_CONTROL_TIME_ANNOUNCE = TimeUnit.SECONDS.toMillis(5L);
    private final ConcurrentValueOrderedMap<PlayerFaction, Integer> factionPointsMap = new ConcurrentValueOrderedMap<>();
    private final Map<PlayerFaction, Integer> factionsPointsDifference = ExpiringMap.builder().expiration(7, TimeUnit.SECONDS).build();
    private final HCF plugin;
    private AtomicBoolean switching = new AtomicBoolean();
    @Getter
    private AtomicLong switchTime;
    private CaptureZone activeCaptureZone;

    public FuryTracker(HCF plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public ConcurrentValueOrderedMap<PlayerFaction, Integer> getFactionPointsMap(){
        return this.factionPointsMap;
    }

    public int getPoints(PlayerFaction faction){
        return GuavaCompat.firstNonNull(this.factionPointsMap.get(faction), 0);
    }

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

    public int takePoints(PlayerFaction faction, int amount){
        return setPoints(faction, getPoints(faction) - amount);
    }

    public int addPoints(PlayerFaction faction, int amount){
        return setPoints(faction, getPoints(faction) + amount);
    }

    public int getPointsDiff(PlayerFaction faction){
        return factionsPointsDifference.getOrDefault(faction, 0);
    }

    @Override
    public EventType getEventType(){
        return EventType.FURY;
    }

    @Override
    public void tick(EventTimer eventTimer, EventFaction eventFaction){
        if(activeCaptureZone == null){
            switchZone(eventFaction);
        }

        if(switching.get()){
            return;
        }

        if(switchTime == null){
            switchTime = new AtomicLong(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15L));
        }else if(System.currentTimeMillis() >= switchTime.get()){
            switchTime.set(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15L));
            switchZone(eventFaction);
            return;
        }

        activeCaptureZone.updateScoreboardRemaining();
        Player cappingPlayer = activeCaptureZone.getCappingPlayer();
        if(cappingPlayer == null) return;

        // The capture zone has been controlled.
        long remainingMillis = activeCaptureZone.getRemainingCaptureMillis();
        if(remainingMillis <= 0L){
            UUID uuid = cappingPlayer.getUniqueId();

            PlayerFaction playerFaction;
            try{
                playerFaction = plugin.getFactions().getFactionManager().getPlayerFaction(uuid);
            }catch(NoFactionFoundException ignored){
                return;
            }
            if(playerFaction != null){
                int newPoints = addPoints(playerFaction, 1);
                if(newPoints < plugin.getConfiguration().getFuryRequiredVictoryPoints()){
                    // Reset back to the default for this tracker.
                    activeCaptureZone.setRemainingCaptureMillis(activeCaptureZone.getDefaultCaptureMillis());
                    Bukkit.broadcastMessage(plugin.getMessagesOld().getString("Event-Fury-CappingAreaBroadcast")
                            .replace("{eventFactionName}", eventFaction.getName())
                            .replace("{factionName}", playerFaction.getName())
                            .replace("{captureZoneName}", activeCaptureZone.getDisplayName())
                            .replace("{newCapturePoints}", String.valueOf(newPoints))
                            .replace("{requiredVictoryPoints}", String.valueOf(plugin.getConfiguration().getFuryRequiredVictoryPoints())));
                }else{
                    // Clear all the points for the next Fury event.
                    this.factionPointsMap.clear();
                    plugin.getTimerManager().getEventTimer().handleWinner(cappingPlayer);
                    return;
                }
            }
            return;
        }

        int remainingSeconds = (int) Math.round((double) remainingMillis / 1000L);
        if(remainingSeconds % 5 == 0){
            cappingPlayer.sendMessage(plugin.getMessagesOld().getString("Event-Fury-CappingArea")
                    .replace("{eventFactionName}", eventFaction.getName())
                    .replace("{captureZoneName}", activeCaptureZone.getDisplayName())
                    .replace("{remainingTime}", String.valueOf(remainingSeconds)));
        }
    }

    private void switchZone(EventFaction faction){
        switching.set(true);
        int random = ThreadLocalRandom.current().nextInt(faction.getCaptureZones().size());
        CaptureZone next = faction.getCaptureZones().get(random);

        if(activeCaptureZone != null && next.equals(activeCaptureZone)){
            switchZone(faction);
            return;
        }

        activeCaptureZone = next;
        faction.getCaptureZones().stream().filter(zone -> !zone.equals(next)).forEach(zone -> {
            zone.setCappingPlayer(null);
            zone.setRemainingCaptureMillis(zone.getDefaultCaptureMillis());
        });

        plugin.getServer().broadcastMessage(plugin.getMessagesOld().getString("Event-Fury-Switched").replace("{newZone}",
                activeCaptureZone.getDisplayName()));
        switching.set(false);
    }

    @Override
    public void onContest(EventFaction eventFaction, EventTimer eventTimer){
        switchZone(eventFaction);
        plugin.getServer().broadcastMessage(ChatColor.YELLOW + (eventFaction instanceof FuryFaction ? "" : "[" +
                eventFaction.getName() + "] ") + ChatColor.GOLD + eventFaction.getName() + " can now be contested.");
    }

    @Override
    public boolean onControlTake(Player player, CaptureZone captureZone){
        if(!captureZone.equals(activeCaptureZone)){
            return false;
        }

        if(player.hasPermission(Permissions.STAFF_IDENTIFIER)){
            return false;
        }

        try{
            plugin.getFactions().getFactionManager().getPlayerFaction(player.getUniqueId());
        }catch(NoFactionFoundException e){
            player.sendMessage(ChatColor.RED + "You must be in a faction to capture Fury.");
            return false;
        }

        return true;
    }

    @Override
    public void onControlLoss(Player player, CaptureZone captureZone, EventFaction eventFaction){
        if(!captureZone.equals(activeCaptureZone)){
            return;
        }

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

    public CaptureZone getActiveCaptureZone(){
        return activeCaptureZone;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRemove(FactionRemoveEvent event){
        Faction faction = event.getFaction();
        if(faction instanceof PlayerFaction){
            this.factionPointsMap.remove((PlayerFaction) faction);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event){
        Faction currentEventFac = plugin.getTimerManager().getEventTimer().getEventFaction();
        if(currentEventFac instanceof FuryFaction){
            Player player = event.getEntity();
            PlayerFaction playerFaction;
            try{
                playerFaction = plugin.getFactions().getFactionManager().getPlayerFaction(player);
            }catch(NoFactionFoundException e){
                return;
            }
            if(plugin.getConfiguration().getFuryPointLossPerDeath() > 0){
                int oldPoints = getPoints(playerFaction);
                if(oldPoints == 0) return;

                int newPoints = takePoints(playerFaction, plugin.getConfiguration().getConquestPointLossPerDeath());
                event.setDeathMessage(null); // for some reason if it isn't handled manually, weird colour coding happens
                Bukkit.getServer().broadcastMessage(plugin.getMessagesOld().getString("Event-Fury-PlayerDeathBroadcast")
                        .replace("{eventFactionName}", currentEventFac.getName())
                        .replace("{playerFactionName}", playerFaction.getName())
                        .replace("{deathLostPoints}", String.valueOf(plugin.getConfiguration().getFuryPointLossPerDeath()))
                        .replace("{player}", player.getName())
                        .replace("{newCapturePoints}", String.valueOf(newPoints))
                        .replace("{requiredVictoryPoints}", String.valueOf(plugin.getConfiguration().getFuryRequiredVictoryPoints())));
            }
        }
    }

    public long getRemainingTimeTillSwitch(){
        return switchTime == null ? 0 : switchTime.get() - System.currentTimeMillis();
    }
}
