package com.doctordark.hcf.eventgame.eotw;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import com.doctordark.hcf.eventgame.faction.KothFaction;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.ClaimableFaction;
import org.hcgames.hcfactions.faction.Faction;
import technology.brk.util.DurationFormatter;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Handles the EndOfTheWorld mini-game which shrinks the border and runs a KOTH event.
 */
public class EotwHandler{

    public static final int BORDER_DECREASE_MINIMUM = 1000;
    public static final int BORDER_DECREASE_AMOUNT = 200;

    public static final long BORDER_DECREASE_TIME_MILLIS = TimeUnit.SECONDS.toMillis(20L);
    public static final int BORDER_DECREASE_TIME_SECONDS = (int) TimeUnit.MILLISECONDS.toSeconds(BORDER_DECREASE_TIME_MILLIS);
    public static final int BORDER_DECREASE_TIME_SECONDS_HALVED = BORDER_DECREASE_TIME_SECONDS / 2;
    public static final String BORDER_DECREASE_TIME_WORDS = DurationFormatUtils.formatDurationWords(BORDER_DECREASE_TIME_MILLIS, true, true);
    public static final String BORDER_DECREASE_TIME_ALERT_WORDS = DurationFormatUtils.formatDurationWords(BORDER_DECREASE_TIME_MILLIS / 2, true, true);

    public static final long EOTW_WARMUP_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    public static final int EOTW_WARMUP_WAIT_SECONDS = (int) (TimeUnit.MILLISECONDS.toSeconds(EOTW_WARMUP_WAIT_MILLIS));

    private static final long EOTW_CAPPABLE_WAIT_MILLIS = TimeUnit.MINUTES.toMillis(20L);
    private static final int WITHER_INTERVAL_SECONDS = 10;
    private final HCF plugin;
    private EotwRunnable runnable;

    public EotwHandler(HCF plugin){
        this.plugin = plugin;
    }

    public EotwRunnable getRunnable(){
        return runnable;
    }

    /**
     * Checks if the map is currently in 'End of the World' mode.
     *
     * @return true if the map is the end of world
     */
    public boolean isEndOfTheWorld(){
        return isEndOfTheWorld(true);
    }

    /**
     * Sets if the server is currently in 'End of the World' mode.
     *
     * @param yes the value to set
     */
    public void setEndOfTheWorld(boolean yes){
        // Don't unnecessary edit task.
        if(yes == isEndOfTheWorld(false)){
            return;
        }

        if(yes){
            plugin.getTimerManager().getInvincibilityTimer().clearAllCooldowns();
            runnable = new EotwRunnable(plugin);
            runnable.runTaskTimer(plugin, 20L, 20L);
        }else{
            if(runnable != null){
                runnable.cancel();
                runnable = null;
            }
        }
    }

    /**
     * Checks if the map is currently in 'End of the World' mode.
     *
     * @param ignoreWarmup if the warmup stage is ignored
     * @return true if the map is the end of world
     */
    public boolean isEndOfTheWorld(boolean ignoreWarmup){
        return runnable != null && (!ignoreWarmup || runnable.getElapsedMilliseconds() > 0);
    }

    public void provideScoreboard(List<String> lines){
        if (runnable != null){
            long remaining = runnable.getMillisUntilStarting();
            if (remaining > 0L){
                lines.add(this.plugin.getMessages().getString("scoreboard.eotw.starts").replace("{remaining}", DurationFormatter.getRemaining(remaining, true)));
            }else if((remaining = runnable.getMillisUntilCappable()) > 0L) {
                lines.add(this.plugin.getMessages().getString("scoreboard.eotw.cappable").replace("{cappable}", DurationFormatter.getRemaining(remaining, true)));
            }
        }
    }

    public static final class EotwRunnable extends BukkitRunnable{

        private final HCF plugin;
        private long startStamp;
        private int elapsedSeconds;

        public EotwRunnable(HCF plugin){
            this.startStamp = System.currentTimeMillis() + EOTW_WARMUP_WAIT_MILLIS;
            this.elapsedSeconds = -EOTW_WARMUP_WAIT_SECONDS;
            this.plugin = plugin;
        }

        public void handleDisconnect(Player player){
        }

        //TODO: Cleanup these millisecond managements
        public long getMillisUntilStarting(){
            long difference = System.currentTimeMillis() - startStamp;
            return difference > 0L ? -1L : Math.abs(difference);
        }

        public long getMillisUntilCappable(){
            return EOTW_CAPPABLE_WAIT_MILLIS - getElapsedMilliseconds();
        }

        public long getElapsedMilliseconds(){
            return System.currentTimeMillis() - startStamp;
        }

        @Override
        public void run(){
            elapsedSeconds++;

            if(elapsedSeconds == 0){
                for(Faction faction : HCF.getPlugin().getFactions().getFactionManager().getFactions()){
                    if(faction instanceof ClaimableFaction){
                        ClaimableFaction claimableFaction = (ClaimableFaction) faction;
                        claimableFaction.removeClaims(claimableFaction.getClaims(), Bukkit.getConsoleSender());
                    }
                }

                Bukkit.broadcastMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "EndOfTheWorld" + ChatColor.DARK_AQUA + " has began.");

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    Faction faction = null;
                    try{
                        faction = plugin.getFactions().getFactionManager().getFaction("EOTW");
                    }catch(NoFactionFoundException e){

                    }

                    if(faction instanceof KothFaction){
                        plugin.getTimerManager().getEventTimer().tryContesting(((EventFaction) faction), plugin.getServer().getConsoleSender());
                    }
                }, (20 * 60) * 20);

                cancel();
                return;
            }

            if(elapsedSeconds < 0 && elapsedSeconds >= -EOTW_WARMUP_WAIT_SECONDS){
                Bukkit.broadcastMessage(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "EndOfTheWorld" + ChatColor.DARK_AQUA + " is starting in " +
                        DurationFormatter.getRemaining(Math.abs(elapsedSeconds) * 1000L, true, false) + '.');

                return;
            }

            // Wither those outside of the border every 10 seconds.
            /*if (elapsedSeconds % WITHER_INTERVAL_SECONDS == 0) {
                Iterator<Player> iterator = outsideBorder.iterator();
                BorderData borderData = HCF.getPlugin().getWorldBorder().getWorldBorder("world");
                while (iterator.hasNext()) {
                    Player player = iterator.next();
                    if (player.getWorld().getName().equals("world") && borderData.insideBorder(player.getLocation())) {
                        iterator.remove();
                        continue;
                    }

                    player.sendMessage(ChatColor.RED + "You are currently outside of the border during EOTW, so you were withered.");
                    player.addPotionEffect(WITHER, true);
                }
            }*/

            /*int newRadiusX = radiusX - BORDER_DECREASE_AMOUNT;
            int newRadiusZ = radiusZ - BORDER_DECREASE_AMOUNT;
            if (elapsedSeconds % BORDER_DECREASE_TIME_SECONDS == 0) {
                ConfigurationService.BORDER_SIZES.put(World.Environment.NORMAL, borderSize = newBorderSize);
                Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Border has been decreased to " + ChatColor.YELLOW + newBorderSize + ChatColor.DARK_AQUA + " blocks.");

                // Update list of players outside of the border now it has shrunk.
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!BorderListener.isWithinBorder(player.getLocation())) {
                        outsideBorder.add(player);
                    }
                }
            } else if (elapsedSeconds % BORDER_DECREASE_TIME_SECONDS_HALVED == 0) {
                Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Border decreasing to " + ChatColor.YELLOW + newBorderSize + ChatColor.DARK_AQUA + " blocks in " +
                        ChatColor.YELLOW + BORDER_DECREASE_TIME_ALERT_WORDS + ChatColor.DARK_AQUA + '.');
            }*/
        }
    }
}
