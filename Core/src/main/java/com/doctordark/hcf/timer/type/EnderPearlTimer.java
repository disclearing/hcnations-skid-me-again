package com.doctordark.hcf.timer.type;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.timer.PlayerTimer;
import com.doctordark.hcf.timer.TimerCooldown;
import com.doctordark.util.NmsUtils;
import net.minecraft.server.v1_7_R4.ItemEnderPearl;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import technology.brk.util.DurationFormatter;
import technology.brk.util.file.Config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EnderPearlTimer extends PlayerTimer implements Listener{

    private static final long REFRESH_DELAY_TICKS = 2L;      // time in ticks it will update the remaining time on the Enderpearl.
    private static final long REFRESH_DELAY_TICKS_18 = 20L;  // time in ticks it will update the remaining time on the Enderpearl for a 1.8 client.

    private final Map<UUID, PearlNameFaker> itemNameFakes = new HashMap<>();
    private final HCF plugin;

    private String scoreboardPrefix = null;

    public EnderPearlTimer(HCF plugin){
        super("Enderpearl", TimeUnit.SECONDS.toMillis(15L));
        this.plugin = plugin;

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

        return ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD;
    }

    @Override
    public void load(Config config){
        super.load(config);
    }

    @Override
    public void onDisable(Config config){
        super.onDisable(config);
        for(Iterator<PearlNameFaker> iterator = itemNameFakes.values().iterator(); iterator.hasNext(); ){
            iterator.next().cancel();
            iterator.remove();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        clearCooldown(event.getPlayer(), event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event){
        clearCooldown(event.getPlayer(), event.getPlayer().getUniqueId());
    }

    public void refund(Player player){
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
        clearCooldown(player, player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onProjectileLaunch(ProjectileLaunchEvent event){
        Projectile projectile = event.getEntity();
        if(projectile instanceof EnderPearl){
            EnderPearl enderPearl = (EnderPearl) projectile;
            ProjectileSource source = enderPearl.getShooter();
            if(source instanceof Player){
                Player shooter = (Player) source;
                long remaining = getRemaining(shooter);
                if(remaining > 0L){
                    shooter.sendMessage(HCF.getPlugin().getMessagesOld().getString("Timer-EnderPearl-StillInCooldown")
                            .replace("{timerName}", getDisplayName())
                            .replace("{timeLeft}", DurationFormatter.getRemaining(remaining, true, false)));
                    shooter.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
                    event.setCancelled(true);
                    return;
                }

                if(plugin.getFactions().getFactionManager().getFactionAt(shooter.getLocation()).getName().equalsIgnoreCase("palace")){
                    shooter.sendMessage(ChatColor.RED + "You cannot pearl in this event zone.");
                    shooter.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
                    event.setCancelled(true);
                    return;
                }

                if(setCooldown(shooter, shooter.getUniqueId(), defaultCooldown, true)){
                    PearlNameFaker pearlNameFaker = new PearlNameFaker(this, shooter);
                    itemNameFakes.put(shooter.getUniqueId(), pearlNameFaker);
                    long ticks = NmsUtils.getProtocolVersion(shooter) >= 47 ? REFRESH_DELAY_TICKS_18 : REFRESH_DELAY_TICKS;
                    pearlNameFaker.runTaskTimerAsynchronously(plugin, ticks, ticks);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event){
        Player player = event.getPlayer();
        PearlNameFaker pearlNameFaker = itemNameFakes.get(player.getUniqueId());
        if(pearlNameFaker != null){
            int previousSlot = event.getPreviousSlot();
            net.minecraft.server.v1_7_R4.ItemStack stack = NmsUtils.getCleanItem(player, previousSlot);
            if(stack != null && stack.getItem() instanceof ItemEnderPearl){
                NmsUtils.sendItemPacketAtSlot(player, stack, previousSlot);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event){
        HumanEntity humanEntity = event.getWhoClicked();
        if(humanEntity instanceof Player){
            Player player = (Player) humanEntity;
            PearlNameFaker pearlNameFaker = itemNameFakes.get(player.getUniqueId());
            if(pearlNameFaker != null){
                Inventory clickedInventory = event.getClickedInventory();
                int heldSlot = player.getInventory().getHeldItemSlot();
                int slot = event.getSlot();
                int hotbarButton = event.getHotbarButton();
                if(hotbarButton != -1){
                    if(hotbarButton == heldSlot && slot != hotbarButton){
                        NmsUtils.sendItemPacketAtSlot(player, NmsUtils.getCleanItem(clickedInventory, hotbarButton), slot);
                        NmsUtils.sendItemPacketAtSlot(player, NmsUtils.getCleanItem(clickedInventory, slot), hotbarButton);
                    }
                }else if(slot == heldSlot){
                    NmsUtils.sendItemPacketAtSlot(player, NmsUtils.getCleanItem(clickedInventory, slot), slot);
                }
            }
        }
    }

    @Override
    public void handleExpiry(@Nullable Player player, UUID playerUUID){
        clearCooldown(player, playerUUID);
    }

    @Override
    public TimerCooldown clearCooldown(@Nullable Player player, UUID playerUUID){
        TimerCooldown cooldown = super.clearCooldown(player, playerUUID);

        PearlNameFaker pearlNameFaker = itemNameFakes.remove(playerUUID);
        if(pearlNameFaker != null){
            pearlNameFaker.cancel();
        }

        return cooldown;
    }

    /**
     * Runnable to show remaining Enderpearl cooldown on held item.
     */
    public static class PearlNameFaker extends BukkitRunnable{

        private final PlayerTimer timer;
        private final Player player;

        public PearlNameFaker(PlayerTimer timer, Player player){
            this.timer = timer;
            this.player = player;
        }

        @Override
        public void run(){
            net.minecraft.server.v1_7_R4.ItemStack stack = NmsUtils.getCleanHeldItem(player);
            if(stack != null && stack.getItem() instanceof ItemEnderPearl){
                stack = stack.cloneItemStack();
                stack.c(HCF.getPlugin().getMessagesOld().getString("Timer-EnderPearl-TimeLeft")
                        .replace("{timeLeft}", DurationFormatter.getRemaining(timer.getRemaining(player), true, true)));
                NmsUtils.sendItemPacketAtHeldSlot(player, stack);
            }
        }

        @Override
        public synchronized void cancel() throws IllegalStateException{
            super.cancel();
            NmsUtils.resendHeldItemPacket(player);
        }
    }
}
