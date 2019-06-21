package com.doctordark.hcf.eventgame;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.crate.EventKey;
import com.doctordark.hcf.eventgame.faction.ConquestFaction;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import com.doctordark.hcf.eventgame.faction.FuryFaction;
import com.doctordark.hcf.eventgame.faction.KothFaction;
import com.doctordark.hcf.eventgame.tracker.ConquestTracker;
import com.doctordark.hcf.eventgame.tracker.FuryTracker;
import com.doctordark.hcf.faction.event.CaptureZoneEnterEvent;
import com.doctordark.hcf.faction.event.CaptureZoneLeaveEvent;
import com.doctordark.hcf.listener.EventSignListener;
import com.doctordark.hcf.timer.GlobalTimer;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import technology.brk.util.DurationFormatter;
import technology.brk.util.GenericUtils;
import technology.brk.util.file.Config;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Timer that handles the cooldown for KingOfTheHill events.
 */
//TODO: Event system seems to need a rewrite
public class EventTimer extends GlobalTimer implements Listener{

    private static final long RESCHEDULE_FREEZE_MILLIS = TimeUnit.SECONDS.toMillis(15L);
    private static final String RESCHEDULE_FREEZE_WORDS = DurationFormatUtils.formatDurationWords(RESCHEDULE_FREEZE_MILLIS, true, true);
    private final HCF plugin;
    private long startStamp;                 // the milliseconds at when the current event started.
    private long lastContestedEventMillis;   // the milliseconds at when the last event was contested.
    private EventFaction eventFaction;
    private Config lastEventConfig;

    public EventTimer(HCF plugin){
        super("Event", 0L);
        this.plugin = plugin;
        lastEventConfig = new Config(plugin, "lastEvent.yml");

        if(lastEventConfig.contains("lastEvent.name")){
            Faction faction = plugin.getFactions().getFactionManager().getFaction(lastEventConfig.getString("lastEvent.name"));

            if(faction instanceof EventFaction && tryContesting((EventFaction) faction, Bukkit.getConsoleSender())){
                if(faction instanceof ConquestFaction){
                    Map<String, Integer> savedPoints = GenericUtils.castMap(lastEventConfig.get("lastEvent.points"), String.class, Integer.class);
                    ConquestTracker tracker = (ConquestTracker) EventType.CONQUEST.getEventTracker();

                    for(Map.Entry<String, Integer> entry : savedPoints.entrySet()){
                        PlayerFaction playerFaction;

                        try{
                            playerFaction = plugin.getFactions().getFactionManager().getFaction(entry.getKey(), PlayerFaction.class);
                        }catch(NoFactionFoundException | ClassCastException e){
                            continue;
                        }

                        tracker.setPoints(playerFaction, entry.getValue());
                    }
                }

                if(faction instanceof KothFaction){
                    KothFaction kothFaction = (KothFaction) faction;
                    kothFaction.getCaptureZone().setRestoreMillis(Long.valueOf(lastEventConfig.getString("lastEvent.time")));
                }
            }

            lastEventConfig.set("lastEvent", null);
        }

        new BukkitRunnable(){
            @Override
            public void run(){
                if(eventFaction != null){
                    eventFaction.getEventType().getEventTracker().tick(EventTimer.this, eventFaction);
                    return;
                }

                // There isn't an active event, find one!
                LocalDateTime now = LocalDateTime.now(plugin.getConfiguration().getServerTimeZoneID());
                int day = now.getDayOfYear();
                int hour = now.getHour();
                int minute = now.getMinute();
                for(Map.Entry<LocalDateTime, String> entry : plugin.getEventScheduler().getScheduleMap().entrySet()){
                    // Compare now with the scheduled time..
                    LocalDateTime scheduledTime = entry.getKey();
                    if(day != scheduledTime.getDayOfYear() || hour != scheduledTime.getHour() || minute != scheduledTime.getMinute()){
                        continue;
                    }

                    // Make sure the faction found in schedule exists.
                    Faction faction = plugin.getFactions().getFactionManager().getFaction(entry.getValue());
                    if(faction instanceof EventFaction && tryContesting((EventFaction) faction, Bukkit.getConsoleSender())){
                        break;
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public EventFaction getEventFaction(){
        return eventFaction;
    }

    @Override
    public String getScoreboardPrefix(){
        return ChatColor.AQUA.toString();
    }

    @Override
    public String getName(){
        return eventFaction != null ? eventFaction.getName() : "Event";
    }

    @Override
    public boolean clearCooldown(){
        boolean result = super.clearCooldown();
        if(eventFaction != null){
            for(CaptureZone captureZone : eventFaction.getCaptureZones()){
                captureZone.setCappingPlayer(null);
            }

            // Make sure to set the land back as Deathban.
            eventFaction.setDeathban(true);
            eventFaction.getEventType().getEventTracker().stopTiming();
            eventFaction = null;
            startStamp = -1L;
            result = true;
        }

        return result;
    }

    @Override
    public long getRemaining(){
        if(eventFaction == null){
            return 0L;
        }else if(eventFaction instanceof KothFaction){
            return ((KothFaction) eventFaction).getCaptureZone().getRemainingCaptureMillis();
        }else{
            return super.getRemaining();
        }
    }

    /**
     * Handles the winner for this event.
     *
     * @param winner the {@link Player} that won
     */
    public void handleWinner(Player winner){
        if(eventFaction != null && winner != null){
            EventType eventType = eventFaction.getEventType();
            World world = winner.getWorld();
            Location location = winner.getLocation();
            EventKey eventKey = plugin.getKeyManager().getEventKey();
            Collection<Inventory> inventories = eventKey.getInventories(eventType);
            ItemStack keyStack = eventKey.getItemStack(new EventKey.EventKeyData(eventType, inventories.isEmpty() ? 1 : ThreadLocalRandom.current().nextInt(inventories.size()) + 1));
            Map<Integer, ItemStack> excess = winner.getInventory().addItem(keyStack, EventSignListener.getEventSign(eventFaction.getName(), winner.getName()));
            for(ItemStack entry : excess.values()){
                world.dropItemNaturally(location, entry);
            }

            plugin.getServer().broadcastMessage(plugin.getMessagesOld().getString("Event-" + ChatColor.stripColor(eventFaction.getEventType().getDisplayName()) + "-WinnerBroadcast")
                    .replace("{eventFactionName}", eventFaction.getEventType().getDisplayName())
                    .replace("{player}", winner.getName())
                    .replace("{playerFactionName}", plugin.getFactions().getFactionManager().hasFaction(winner) ? plugin.getFactions().getFactionManager().getPlayerFaction(winner).getFormattedName() : "*")
                    .replace("{time}", DurationFormatUtils.formatDurationWords(getUptime(), true, true)));

            clearCooldown(); // must always be cleared last as this nulls some variables.
        }
    }

    /**
     * Tries contesting an {@link EventFaction}.
     *
     * @param eventFaction the {@link EventFaction} to be contested
     * @param sender       the contesting {@link CommandSender}
     * @return true if the {@link EventFaction} was successfully contested
     */
    public boolean tryContesting(EventFaction eventFaction, CommandSender sender){
        if(this.eventFaction != null){
            sender.sendMessage(ChatColor.RED + "There is already an active event, use /event cancel to end it.");
            return false;
        }

        if(eventFaction instanceof KothFaction){
            KothFaction kothFaction = (KothFaction) eventFaction;
            if(kothFaction.getCaptureZone() == null){
                sender.sendMessage(ChatColor.RED + "Cannot schedule " + eventFaction.getName() + " as its' capture zone is not set.");
                return false;
            }
        }else if(eventFaction instanceof ConquestFaction){
            ConquestFaction conquestFaction = (ConquestFaction) eventFaction;
            Collection<ConquestFaction.ConquestZone> zones = conquestFaction.getConquestZones();
            for(ConquestFaction.ConquestZone zone : ConquestFaction.ConquestZone.values()){
                if(!zones.contains(zone)){
                    sender.sendMessage(ChatColor.RED + "Cannot schedule " + eventFaction.getName() + " as capture zone '" + zone.getDisplayName() + ChatColor.RED + "' is not set.");
                    return false;
                }
            }
        }else if(eventFaction instanceof FuryFaction){
            FuryFaction conquestFaction = (FuryFaction) eventFaction;
            Collection<FuryFaction.FuryZone> zones = conquestFaction.getConquestZones();
            for(FuryFaction.FuryZone zone : FuryFaction.FuryZone.values()){
                if(!zones.contains(zone)){
                    sender.sendMessage(ChatColor.RED + "Cannot schedule " + eventFaction.getName() + " as capture zone '" + zone.getDisplayName() + ChatColor.RED + "' is not set.");
                    return false;
                }
            }
        }

        // Don't allow events to reschedule their-self before they are allowed to.
        long millis = System.currentTimeMillis();
        if(this.lastContestedEventMillis + EventTimer.RESCHEDULE_FREEZE_MILLIS - millis > 0L){
            sender.sendMessage(ChatColor.RED + "Cannot reschedule events within " + EventTimer.RESCHEDULE_FREEZE_WORDS + '.');
            return false;
        }

        lastContestedEventMillis = millis;
        startStamp = millis;
        this.eventFaction = eventFaction;

        eventFaction.getEventType().getEventTracker().onContest(eventFaction, this);
        if(eventFaction instanceof ConquestFaction){
            setRemaining(1000L, true); //TODO: Add a unpredicated timer impl instead of this xD.
            setPaused(true);
        }

        Collection<CaptureZone> captureZones = eventFaction.getCaptureZones();
        for(CaptureZone captureZone : captureZones){
            if(captureZone.isActive()){
                Player player = Iterables.getFirst(captureZone.getCuboid().getPlayers(), null);
                if(player != null && !player.isDead() && eventFaction.getEventType().getEventTracker().onControlTake(player, captureZone)){
                    captureZone.setCappingPlayer(player);
                }
            }
        }

        eventFaction.setDeathban(false); // the event should be lowered deathban whilst active.
        return true;
    }

    /**
     * Gets the total uptime in milliseconds since the event started.
     *
     * @return the time in milliseconds since event started
     */
    public long getUptime(){
        return System.currentTimeMillis() - startStamp;
    }

    /**
     * Gets the time in milliseconds since the event started.
     *
     * @return the time in milliseconds since event started
     */
    public long getStartStamp(){
        return startStamp;
    }

    private void handleDisconnect(Player player){
        if(eventFaction != null){
            Objects.requireNonNull(player);
            Collection<CaptureZone> captureZones = eventFaction.getCaptureZones();
            for(CaptureZone captureZone : captureZones){
                if(Objects.equals(captureZone.getCappingPlayer(), player)){
                    captureZone.setCappingPlayer(null);
                    eventFaction.getEventType().getEventTracker().onControlLoss(player, captureZone, eventFaction);
                    break;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event){
        handleDisconnect(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLogout(PlayerQuitEvent event){
        handleDisconnect(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event){
        handleDisconnect(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneEnter(CaptureZoneEnterEvent event){
        if(eventFaction != null){
            CaptureZone captureZone = event.getCaptureZone();
            if(eventFaction.getCaptureZones().contains(captureZone)){
                Player player = event.getPlayer();
                if(captureZone.getCappingPlayer() == null && eventFaction.getEventType().getEventTracker().onControlTake(player, captureZone)){
                    captureZone.setCappingPlayer(player);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneLeave(CaptureZoneLeaveEvent event){
        if(Objects.equals(event.getFaction(), eventFaction)){
            Player player = event.getPlayer();
            CaptureZone captureZone = event.getCaptureZone();
            if(Objects.equals(player, captureZone.getCappingPlayer())){
                captureZone.setCappingPlayer(null);
                eventFaction.getEventType().getEventTracker().onControlLoss(player, captureZone, eventFaction);

                // Try and find a new capper.
                for(Player target : captureZone.getCuboid().getPlayers()){
                    if(target != null && !target.equals(player) && !target.isDead() && eventFaction.getEventType().getEventTracker().onControlTake(target, captureZone)){
                        captureZone.setCappingPlayer(target);
                        break;
                    }
                }
            }
        }
    }

    public void handleDisable(){
        if(getEventFaction() != null){
            EventFaction lastEvent = getEventFaction();
            lastEventConfig.set("lastEvent.name", lastEvent.getName());
            if(lastEvent instanceof ConquestFaction){
                Map<String, Integer> savePointsMap = new HashMap<>();
                for(Map.Entry<PlayerFaction, Integer> entry : ((ConquestTracker)EventType.CONQUEST.getEventTracker()).getFactionPointsMap().entrySet()){
                    savePointsMap.put(entry.getKey().getUniqueID().toString(), entry.getValue());
                }

                lastEventConfig.set("lastEvent.points", savePointsMap);
            }

            if(lastEvent instanceof KothFaction){
                lastEventConfig.set("lastEvent.time", ((KothFaction) lastEvent).getCaptureZone().getRemainingCaptureMillis());
            }
        }

        lastEventConfig.save();
    }

    public void provideScoreboard(List<String> lines){
        EventFaction eventFaction = getEventFaction();

        if(eventFaction instanceof KothFaction){
            lines.add(plugin.getMessages().getString("scoreboard.events.koth").replace("{name}", eventFaction.getName())
                    .replace("{remaining}", DurationFormatter.getRemaining(getRemaining(), true)));
        }else if(eventFaction instanceof ConquestFaction){
            ConquestFaction conquestFaction = (ConquestFaction) eventFaction;

            for(String line : plugin.getMessages().getString("scoreboard.events.conquest.header").split("\n")){
                lines.add(line.replace("{redRemaining}", conquestFaction.getRed().getScoreboardRemaining())
                        .replace("{yellowRemaining}", conquestFaction.getYellow().getScoreboardRemaining())
                        .replace("{greenRemaining}", conquestFaction.getGreen().getScoreboardRemaining())
                        .replace("{blueRemaining}", conquestFaction.getBlue().getScoreboardRemaining()));
            }

            ConquestTracker conquestTracker = (ConquestTracker) conquestFaction.getEventType().getEventTracker();
            int count = 1;
            for(Map.Entry<PlayerFaction, Integer> entry : conquestTracker.getFactionPointsMap().entrySet()){
                String factionName = entry.getKey().getName();
                if(factionName.length() > 14) factionName = factionName.substring(0, 14);

                int diffInt = conquestTracker.getPointsDiff(entry.getKey());
                String diff;

                if(diffInt < 0){
                    diff = plugin.getMessages().getString("scoreboard.events.fury.diff.negative").replace("{diff}", String.valueOf(diffInt));
                }else if(diffInt > 0){
                    diff = plugin.getMessages().getString("scoreboard.events.fury.diff.positive").replace("{diff}", "+" + String.valueOf(diffInt));
                }else{
                    diff = "";
                }

                lines.add(plugin.getMessages().getString("scoreboard.events.conquest.entry").replace("{count}", String.valueOf(count))
                        .replace("{name}", factionName).replace("{points}", String.valueOf(entry.getValue())).replace("{diff}", diff));
                if(++count == 4) break;
            }
        }else if(eventFaction instanceof FuryFaction){
            FuryFaction furyFaction = (FuryFaction) eventFaction;
            FuryTracker furyTracker = (FuryTracker) furyFaction.getEventType().getEventTracker();

            for(String line : plugin.getMessages().getString("scoreboard.events.fury.header").split("\n")){
                lines.add(line.replace("{capzone}", (furyTracker.getActiveCaptureZone() == null ? "None" :
                        furyTracker.getActiveCaptureZone().getName())).replace("{remaining}", (furyTracker.getActiveCaptureZone() == null ?
                        "00:00" : DurationFormatter.getRemaining(furyTracker.getActiveCaptureZone().getRemainingCaptureMillis(), true)))
                        .replace("{switchremaining}", DurationFormatter.getRemaining(furyTracker.getRemainingTimeTillSwitch(), true, true)));
            }

            // Show the top 3 factions next.
            int count = 1;
            for(Map.Entry<PlayerFaction, Integer> entry : furyTracker.getFactionPointsMap().entrySet()){
                String factionName = entry.getKey().getName();
                if(factionName.length() > 14) factionName = factionName.substring(0, 14);

                int diffInt = furyTracker.getPointsDiff(entry.getKey());
                String diff;

                if(diffInt < 0){
                    diff = plugin.getMessages().getString("scoreboard.events.fury.diff.negative").replace("{diff}", String.valueOf(diffInt));
                }else if(diffInt > 0){
                    diff = plugin.getMessages().getString("scoreboard.events.fury.diff.positive").replace("{diff}", "+" + String.valueOf(diffInt));
                }else{
                    diff = "";
                }

                lines.add(plugin.getMessages().getString("scoreboard.events.fury.entry").replace("{count}", String.valueOf(count))
                        .replace("{name}", factionName).replace("{points}", String.valueOf(entry.getValue())).replace("{diff}", diff));
                if(++count == 4) break;
            }
        }

    }
}
