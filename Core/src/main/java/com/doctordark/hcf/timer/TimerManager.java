package com.doctordark.hcf.timer;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.EventTimer;
import com.doctordark.hcf.timer.type.CombatTimer;
import com.doctordark.hcf.timer.type.EnderPearlTimer;
import com.doctordark.hcf.timer.type.GappleTimer;
import com.doctordark.hcf.timer.type.InvincibilityTimer;
import com.doctordark.hcf.timer.type.LogoutTimer;
import com.doctordark.hcf.timer.type.PvpClassWarmupTimer;
import com.doctordark.hcf.timer.type.StuckTimer;
import com.doctordark.hcf.timer.type.TeleportTimer;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import technology.brk.util.DurationFormatter;
import technology.brk.util.file.Config;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
public class TimerManager implements Listener{

    private final Set<Timer> timers;

    @Getter(AccessLevel.NONE) private final HCF plugin;
    @Getter(AccessLevel.NONE) private final Config config;

    private final CombatTimer combatTimer;
    private final LogoutTimer logoutTimer;
    private final EnderPearlTimer enderPearlTimer;
    private final EventTimer eventTimer;
    private final GappleTimer gappleTimer;
    private final InvincibilityTimer invincibilityTimer;
    private final PvpClassWarmupTimer pvpClassWarmupTimer;
    private final StuckTimer stuckTimer;
    private final TeleportTimer teleportTimer;

    public TimerManager(HCF plugin){
        this.plugin = plugin;
        timers = new LinkedHashSet<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        registerTimer(enderPearlTimer = new EnderPearlTimer(plugin));
        registerTimer(logoutTimer = new LogoutTimer());
        registerTimer(gappleTimer = new GappleTimer(plugin));
        registerTimer(stuckTimer = new StuckTimer());
        registerTimer(invincibilityTimer = new InvincibilityTimer(plugin));
        registerTimer(combatTimer = new CombatTimer(plugin));
        registerTimer(teleportTimer = new TeleportTimer(plugin));
        registerTimer(eventTimer = new EventTimer(plugin));
        registerTimer(pvpClassWarmupTimer = new PvpClassWarmupTimer(plugin));

        config = new Config(plugin, "timers");
        for(Timer timer : timers){
            timer.load(config);
        }
    }

    private void registerTimer(Timer timer){
        timers.add(timer);
        if(timer instanceof Listener){
            plugin.getServer().getPluginManager().registerEvents((Listener) timer, plugin);
        }
    }

    public void saveTimerData(){
        for(Timer timer : timers){
            timer.onDisable(config);
        }

        config.save();
    }

    public void provideScoreboard(Player player, List<String> lines){
        timers.stream().
                filter(PlayerTimer.class::isInstance)
                .map(PlayerTimer.class::cast).
                forEach(timer -> {
                    long remaining = timer.getRemaining(player);

                    if(remaining > 0){
                        lines.add(plugin.getMessages().getString("scoreboard.timer").replace("{name}", timer.getDisplayName()).replace("{remaining}", DurationFormatter.getRemaining(remaining, true)));
                    }
                });
    }
}
