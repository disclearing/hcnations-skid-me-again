package com.doctordark.hcf.timer.type;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.timer.PlayerTimer;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

/**
 * Timer used to prevent {@link Player}s from using Notch Apples too often.
 */
public class GappleTimer extends PlayerTimer implements Listener{

    private String scoreboardPrefix = null;

    public GappleTimer(HCF plugin){
        super("Gapple", TimeUnit.HOURS.toMillis(6L));

        if(scoreboardPrefix == null || scoreboardPrefix.isEmpty() || scoreboardPrefix.equals("Error! Please contact an administrator")){
            scoreboardPrefix = null;
        }
    }

    @Override
    public String getScoreboardPrefix(){
        if(!(scoreboardPrefix == null)){
            return scoreboardPrefix;
        }

        return ChatColor.YELLOW.toString() + ChatColor.BOLD;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerConsume(PlayerItemConsumeEvent event){
        ItemStack stack = event.getItem();
        if(stack != null && stack.getType() == Material.GOLDEN_APPLE && stack.getDurability() == 1){
            Player player = event.getPlayer();
            if(setCooldown(player, player.getUniqueId(), defaultCooldown, false, value -> false)){
                player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Timer-GApple-ConsumedGApple")
                        .replace("{timeLeft}", DurationFormatUtils.formatDurationWords(defaultCooldown, true, true)));
            }else{
                event.setCancelled(true);
                player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Timer-GApple-ConsumedGApple")
                        .replace("{timerName}", getDisplayName())
                        .replace("{timeLeft}", DurationFormatUtils.formatDurationWords(defaultCooldown, true, true)));
            }
        }
    }
}
