package com.doctordark.hcf.eventgame.tracker;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventTimer;
import com.doctordark.hcf.eventgame.EventType;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import com.doctordark.hcf.eventgame.faction.KothFaction;
import com.doctordark.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import technology.brk.util.DateTimeFormats;

import java.util.concurrent.TimeUnit;

public class KothTracker implements EventTracker{

    public static final long DEFAULT_CAP_MILLIS = TimeUnit.MINUTES.toMillis(15L);
    /**
     * Minimum time the KOTH has to be controlled before this tracker will announce when control has been lost.
     */
    private static final long MINIMUM_CONTROL_TIME_ANNOUNCE = TimeUnit.SECONDS.toMillis(25L);
    private final HCF plugin;

    public KothTracker(HCF plugin){
        this.plugin = plugin;
    }

    @Override
    public EventType getEventType(){
        return EventType.KOTH;
    }

    @Override
    public void tick(EventTimer eventTimer, EventFaction eventFaction){
        CaptureZone captureZone = ((KothFaction) eventFaction).getCaptureZone();
        captureZone.updateScoreboardRemaining();
        long remainingMillis = captureZone.getRemainingCaptureMillis();
        if(remainingMillis <= 0L){ // has been captured.
            plugin.getTimerManager().getEventTimer().handleWinner(captureZone.getCappingPlayer());
            eventTimer.clearCooldown();
            return;
        }

        if(remainingMillis == captureZone.getDefaultCaptureMillis()) return;

        int remainingSeconds = (int) (remainingMillis / 1000L);
        if(remainingSeconds > 0 && remainingSeconds % 30 == 0){
            Bukkit.getServer().broadcastMessage(plugin.getMessagesOld().getString("Event-Koth-ControllingCapture")
                    .replace("{eventFactionName}", eventFaction.getEventType().getDisplayName())
                    .replace("{eventFaction}", captureZone.getDisplayName())
                    .replace("{timeRemaining}", DateTimeFormats.KOTH_FORMAT.format(remainingMillis)));
        }
    }

    @Override
    public void onContest(EventFaction eventFaction, EventTimer eventTimer){
        Bukkit.getServer().broadcastMessage(plugin.getMessagesOld().getString("Event-Koth-ContestAvailable")
                .replace("{eventFactionName}", eventFaction.getEventType().getDisplayName())
                .replace("{eventFaction}", eventFaction.getName())
                .replace("{timeRemaining}", DateTimeFormats.KOTH_FORMAT.format(eventTimer.getRemaining())));
    }

    @Override
    public boolean onControlTake(Player player, CaptureZone captureZone){
        if(player.hasPermission(Permissions.STAFF_IDENTIFIER)){
            return false;
        }

        player.sendMessage(plugin.getMessagesOld().getString("Event-Koth-NowInControl")
                .replace("{captureZoneName}", captureZone.getDisplayName()));
        return true;
    }

    @Override
    public void onControlLoss(Player player, CaptureZone captureZone, EventFaction eventFaction){
        player.sendMessage(plugin.getMessagesOld().getString("Event-Koth-NoLongerInControl")
                .replace("{captureZoneName}", captureZone.getDisplayName()));

        // Only broadcast if the KOTH has been controlled for at least 25 seconds to prevent spam.
        long remainingMillis = captureZone.getRemainingCaptureMillis();
        if(remainingMillis > 0L && captureZone.getDefaultCaptureMillis() - remainingMillis > MINIMUM_CONTROL_TIME_ANNOUNCE){
            Bukkit.broadcastMessage(ChatColor.GOLD + "[" + eventFaction.getEventType().getDisplayName() + "] " +
                    ChatColor.DARK_AQUA + player.getName() + ChatColor.BLUE + " has lost control of " +
                    ChatColor.DARK_AQUA + captureZone.getDisplayName() + ChatColor.BLUE + '.' +
                    ChatColor.RED + " (" + captureZone.getScoreboardRemaining() + ')');
        }
    }

    @Override
    public void stopTiming(){

    }
}
