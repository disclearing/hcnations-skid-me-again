package com.doctordark.hcf.timer.type;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.timer.PlayerTimer;
import com.doctordark.hcf.timer.TimerCooldown;
import com.doctordark.hcf.timer.event.TimerClearEvent;
import com.doctordark.hcf.visualise.VisualType;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.event.claim.ClaimChangeEvent;
import org.hcgames.hcfactions.event.claim.FactionClaimChangedEvent;
import org.hcgames.hcfactions.event.claim.PlayerClaimEnterEvent;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.faction.system.RoadFaction;
import org.hcgames.hcfactions.faction.system.WarzoneFaction;
import org.hcgames.hcfactions.faction.system.WildernessFaction;
import technology.brk.util.BukkitUtils;
import technology.brk.util.DurationFormatter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Timer used to apply PVP Protection to {@link Player}s.
 */
public class InvincibilityTimer extends PlayerTimer implements Listener{

    //TODO: Future proof
    private static final String PVP_COMMAND = "/pvp enable";

    // The PlayerPickupItemEvent spams if cancelled, needs a delay between messages to look clean.
    private static final long ITEM_PICKUP_DELAY = TimeUnit.SECONDS.toMillis(30L);
    private static final long ITEM_PICKUP_MESSAGE_DELAY = 1250L;
    private static final String ITEM_PICKUP_MESSAGE_META_KEY = "pickupMessageDelay";

    private final TObjectLongMap<UUID> itemUUIDPickupDelays = new TObjectLongHashMap<>();
    private final HCF plugin;

    private String scoreboardPrefix = null;

    public InvincibilityTimer(HCF plugin){
        super("Invincibility", TimeUnit.MINUTES.toMillis(30L));
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

        return ChatColor.DARK_GREEN.toString() + ChatColor.BOLD;
    }

    @Override
    public void handleExpiry(@Nullable Player player, UUID playerUUID){
        if(player != null){
            plugin.getVisualiseHandler().clearVisualBlocks(player, VisualType.CLAIM_BORDER, null);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTimerClear(TimerClearEvent event){
        if(event.getTimer() == this){
            Optional<Player> playerOptional = event.getPlayer();
            if(playerOptional.isPresent()){
                plugin.getVisualiseHandler().clearVisualBlocks(playerOptional.get(), VisualType.CLAIM_BORDER, null);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onClaimChange(FactionClaimChangedEvent event){
        if(event.getReason() == ClaimChangeEvent.ClaimChangeReason.CLAIM){
            Collection<Claim> claims = event.getClaims();
            for(Claim claim : claims){
                Collection<Player> players = claim.getPlayers();
                if(players.isEmpty()){
                    continue;
                }

                Location location = new Location(claim.getWorld(), claim.getMinimumX() - 1, 0, claim.getMinimumZ() - 1);
                location = BukkitUtils.getHighestLocation(location, location);
                for(Player player : players){
                    if(getRemaining(player) > 0L && player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN)){
                        player.sendMessage(plugin.getMessagesOld().getString("Timer-Invincibility-LandClaimedTeleported")
                                .replace("{timerName}", getDisplayName()));
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        if(setCooldown(player, player.getUniqueId(), defaultCooldown, true)){
            setPaused(player.getUniqueId(), true);
            player.sendMessage(plugin.getMessagesOld().getString("Timer-Invincibility-TimerStartedRespawn")
                    .replace("{timerName}", getDisplayName()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        World world = player.getWorld();
        Location location = player.getLocation();
        Collection<ItemStack> drops = event.getDrops();
        if(!drops.isEmpty()){
            Iterator<ItemStack> iterator = drops.iterator();

            // Drop the items manually so we can add meta to prevent
            // PVP Protected players from collecting them.
            long stamp = System.currentTimeMillis() + +ITEM_PICKUP_DELAY;
            while(iterator.hasNext()){
                itemUUIDPickupDelays.put(world.dropItemNaturally(location, iterator.next()).getUniqueId(), stamp);
                iterator.remove();
            }
        }

        clearCooldown(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent event){
        Player player = event.getPlayer();
        long remaining = getRemaining(player);
        if(remaining > 0L){
            event.setCancelled(true);
            player.sendMessage(plugin.getMessagesOld().getString("Timer-Invincibility-EmptyBucketNotAllowed")
                    .replace("{timeLeft}", DurationFormatter.getRemaining(remaining, true, false))
                    .replace("{timerName}", getDisplayName()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event){
        Player player = event.getPlayer();
        if(player == null) return;
        long remaining = getRemaining(player);
        if(remaining > 0L){
            event.setCancelled(true);
            player.sendMessage(plugin.getMessagesOld().getString("Timer-Invincibility-IgniteBlocksNotAllowed")
                    .replace("{timeLeft}", DurationFormatter.getRemaining(remaining, true, false))
                    .replace("{timerName}", getDisplayName()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onItemPickup(PlayerPickupItemEvent event){
        Player player = event.getPlayer();
        long remaining = getRemaining(player);
        if(remaining > 0L){
            UUID itemUUID = event.getItem().getUniqueId();
            long delay = itemUUIDPickupDelays.get(itemUUID);
            if(delay == itemUUIDPickupDelays.getNoEntryValue()){
                return;
            }

            // The item has been spawned for over the required pickup time for
            // PVP Protected players, let them pick it up.
            long millis = System.currentTimeMillis();
            if(delay - millis <= 0L){
                itemUUIDPickupDelays.remove(itemUUID);
                return;
            }

            // Don't let the pickup event spam the player.
            event.setCancelled(true);
            MetadataValue value = player.getMetadata(ITEM_PICKUP_MESSAGE_META_KEY).isEmpty() ? null : player.getMetadata(ITEM_PICKUP_MESSAGE_META_KEY).get(0);
            if(value != null && value.asLong() - millis <= 0L){
                player.setMetadata(ITEM_PICKUP_MESSAGE_META_KEY, new FixedMetadataValue(plugin, millis + ITEM_PICKUP_MESSAGE_DELAY));
                player.sendMessage(plugin.getMessagesOld().getString("Timer-Invincibility-PickupNotAllowed")
                        .replace("{timeLeft}", DurationFormatter.getRemaining(remaining, true, false))
                        .replace("{timerName}", getDisplayName()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        TimerCooldown runnable = cooldowns.get(event.getPlayer().getUniqueId());
        if(runnable != null && runnable.getRemaining() > 0L){
            runnable.setPaused(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(!player.hasPlayedBefore()){
            if(canApply() && setCooldown(player, player.getUniqueId(), defaultCooldown, true)){
                setPaused(player.getUniqueId(), true);
                player.sendMessage(plugin.getMessagesOld().getString("Timer-Invincibility-TimerStartedSpawn")
                        .replace("{timerName}", getDisplayName()));
            }
        }else{
            // If a player has their timer paused and they are not in a safezone, un-pause for them.
            // We do this because disconnection pauses PVP Protection.
            if(isPaused(player) && getRemaining(player) > 0L && !plugin.getFactions().getFactionManager().getFactionAt(player.getLocation()).isSafezone()){
                setPaused(player.getUniqueId(), false);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerClaimEnterMonitor(PlayerClaimEnterEvent event){
        Player player = event.getPlayer();
        if(event.getTo().getWorld().getEnvironment() == World.Environment.THE_END){
            clearCooldown(player);
            return;
        }

        boolean flag = getRemaining(player.getUniqueId()) > 0L;
        if(flag){
            Faction toFaction = event.getToFaction();
            Faction fromFaction = event.getFromFaction();
            if(fromFaction.isSafezone() && !toFaction.isSafezone()){
                setPaused(player.getUniqueId(), false);
            }else if(!fromFaction.isSafezone() && toFaction.isSafezone()){
                setPaused(player.getUniqueId(), true);
            }

            if(event.getEnterCause() == PlayerClaimEnterEvent.EnterCause.TELEPORT){
                // Allow player to enter own claim, but just remove PVP Protection when teleporting.
                if(toFaction instanceof PlayerFaction && (plugin.getFactions().getFactionManager().hasFaction(player) && plugin.getFactions().getFactionManager().getPlayerFaction(player) == toFaction)){
                    player.sendMessage(plugin.getMessagesOld().getString("Timer-Invincibility-EnterOwnClaim")
                            .replace("{timerName}", getDisplayName()));
                    clearCooldown(player);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerClaimEnter(PlayerClaimEnterEvent event){
        Player player = event.getPlayer();
        Faction toFaction = event.getToFaction();
        long remaining; // lazy load
        if(toFaction != null && (remaining = getRemaining(player)) > 0L){
            if(!toFaction.isSafezone() && !(toFaction instanceof RoadFaction) && !(toFaction instanceof WarzoneFaction) && !(toFaction instanceof WildernessFaction)){
                event.setCancelled(true);
                player.sendMessage(plugin.getMessagesOld().getString("Timer-Invincibility-CannotEnterFactionLand")
                        .replace("{timeLeft}", DurationFormatter.getRemaining(remaining, true, false))
                        .replace("{factionName}", toFaction.getFormattedName(player))
                        .replace("{pvpCommand}", PVP_COMMAND)
                        .replace("{timerName}", getDisplayName()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageEvent(EntityDamageEvent event){
        if(!(event.getEntity() instanceof Player)){
            return;
        }

        Player player = (Player) event.getEntity();

        long remaining = getRemaining(player);

        if(remaining > 0L){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof Player){
            Player attacker = BukkitUtils.getFinalAttacker(event, true);
            if(attacker == null){
                return;
            }

            long remaining;
            Player player = (Player) entity;
            if((remaining = getRemaining(player)) > 0L){
                event.setCancelled(true);
                attacker.sendMessage(plugin.getMessagesOld().getString("Events-PVP-TimerRunning")
                        .replace("{player}", player.getName())
                        .replace("{pvpTimerName}", getDisplayName())
                        .replace("{pvpTimerTimeRemaining}", DurationFormatter.getRemaining(remaining, true, false)));

                return;
            }

            if((remaining = getRemaining(attacker)) > 0L){
                event.setCancelled(true);
                attacker.sendMessage(plugin.getMessagesOld().getString("Timer-Invincibility-CannotAttackPlayers")
                        .replace("{timeLeft}", DurationFormatter.getRemaining(remaining, true, false))
                        .replace("{pvpCommand}", PVP_COMMAND)
                        .replace("{timerName}", getDisplayName()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPotionSplash(PotionSplashEvent event){
        ThrownPotion potion = event.getPotion();
        if(potion.getShooter() instanceof Player && BukkitUtils.isDebuff(potion)){
            for(LivingEntity livingEntity : event.getAffectedEntities()){
                if(livingEntity instanceof Player){
                    if(getRemaining((Player) livingEntity) > 0L){
                        event.setIntensity(livingEntity, 0);
                    }
                }
            }
        }
    }

    @Override
    public boolean setCooldown(@Nullable Player player, UUID playerUUID, long duration, boolean overwrite, @Nullable Predicate<Long> callback){
        return canApply() && super.setCooldown(player, playerUUID, duration, overwrite, callback);
    }

    private boolean canApply(){
        return !plugin.getEotwHandler().isEndOfTheWorld() && !plugin.getConfiguration().isKitMap() && !plugin.getSOTWManager().isActive();
    }
}
