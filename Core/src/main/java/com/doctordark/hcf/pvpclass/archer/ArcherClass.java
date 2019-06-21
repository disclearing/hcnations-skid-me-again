/*
 * Copyright (C) 2016 SystemUpdate (https://systemupdate.io) All Rights Reserved
 */

package com.doctordark.hcf.pvpclass.archer;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.pvpclass.PvpClass;
import com.doctordark.hcf.pvpclass.event.PvpClassEquipEvent;
import com.doctordark.hcf.pvpclass.event.PvpClassUnequipEvent;
import com.doctordark.hcf.timer.event.TimerExpireEvent;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import net.minecraft.util.io.netty.util.internal.ConcurrentSet;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.hcgames.hcfactions.event.faction.FactionCreateEvent;
import org.hcgames.hcfactions.event.faction.FactionRemoveEvent;
import org.hcgames.hcfactions.event.playerfaction.PlayerJoinedFactionEvent;
import org.hcgames.hcfactions.event.playerfaction.PlayerLeftFactionEvent;
import org.hcgames.hcfactions.faction.Faction;
import technology.brk.util.DurationFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ArcherClass extends PvpClass implements Listener{

    private static final int MARK_TIMEOUT_SECONDS = 15;
    private static final float MINIMUM_FORCE = 0.5F;

    private static final PotionEffect ARCHER_SPEED_EFFECT = new PotionEffect(PotionEffectType.SPEED, 160, 3);
    private static final long ARCHER_SPEED_COOLDOWN_DELAY = TimeUnit.MINUTES.toMillis(1L);
    private static final PotionEffect ARCHER_JUMP_EFFECT = new PotionEffect(PotionEffectType.JUMP, 200, 2);
    private static final long ARCHER_JUMP_COOLDOWN_DELAY = TimeUnit.SECONDS.toMillis(45L);
    final ConcurrentSet<UUID> marked = new ConcurrentSet<>();
    final HCF plugin;
    private final TObjectLongMap<UUID> archerSpeedCooldowns = new TObjectLongHashMap<>();
    private final TObjectLongMap<UUID> archerJumpCooldowns = new TObjectLongHashMap<>();
    private final ConcurrentHashMap<UUID, Marks> archerMarks = new ConcurrentHashMap<>();

    public ArcherClass(HCF plugin){
        super("Archer", TimeUnit.SECONDS.toMillis(10L));
        this.plugin = plugin;

        this.passiveEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
    }

    @Override
    public boolean onEquip(Player player){
        return super.onEquip(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerClassUnequip(PvpClassUnequipEvent event){
        Marks marks = archerMarks.remove(event.getPlayer().getUniqueId());
        if(marks != null){
            for(UUID marked : marks.getMarkedPlayers()){
                marks.removeMarkedPlayer(marked, true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event){
        Marks marks = archerMarks.remove(event.getEntity().getUniqueId());
        if(marks != null){
            for(UUID marked : marks.getMarkedPlayers()){
                marks.removeMarkedPlayer(marked, true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        Marks marks = archerMarks.remove(event.getPlayer().getUniqueId());
        if(marks != null){
            for(UUID marked : marks.getMarkedPlayers()){
                marks.removeMarkedPlayer(marked, true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event){
        Marks marks = archerMarks.remove(event.getPlayer().getUniqueId());
        if(marks != null){
            for(UUID marked : marks.getMarkedPlayers()){
                marks.removeMarkedPlayer(marked, true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if(!entity.equals(damager) && entity instanceof Player && damager instanceof Arrow){
            Arrow arrow = (Arrow) damager;
            float force = arrow.getShotForce();
            if(force == -1.0){
                return;
            }

            ProjectileSource source = arrow.getShooter();
            if(source instanceof Player){
                Player shooter = (Player) source;
                if(plugin.getPvpClassManager().hasClassEquipped(shooter, this)){
                    if(force <= MINIMUM_FORCE){
                        shooter.sendMessage(plugin.getMessages().getString("PvPClass.Archer.Minimum-Force-Not-Met")
                                .replace("{archerMinimumForce}", String.valueOf(MINIMUM_FORCE)));
                        return;
                    }

                    event.setDamage(event.getDamage() + 3.0);

                    Player attacked = (Player) entity;

                    Marks sentMarks;

                    if(archerMarks.containsKey(shooter.getUniqueId())){
                        sentMarks = archerMarks.get(shooter.getUniqueId());
                    }else{
                        sentMarks = new Marks(this, shooter.getUniqueId());
                        archerMarks.put(shooter.getUniqueId(), sentMarks);
                    }

                    if(!sentMarks.isMarked(attacked.getUniqueId())){
                        shooter.sendMessage(plugin.getMessages().getString("PvPClass.Archer.Archer.Marked")
                                .replace("{player}", attacked.getName())
                                .replace("{time}", String.valueOf(MARK_TIMEOUT_SECONDS)));
                        attacked.sendMessage(plugin.getMessages().getString("PvPClass.Archer.Marked.Marked")
                                .replace("{player}", shooter.getName())
                                .replace("{time}", String.valueOf(MARK_TIMEOUT_SECONDS)));

                        marked.add(attacked.getUniqueId());
                        sentMarks.markPlayer(attacked);

                        for(Player player : plugin.getServer().getOnlinePlayers()){
                            plugin.getScoreboardHandler().getBoard(player).addUpdate(attacked);
                        }
                        return;
                    }

                    sentMarks.refreshTimer(attacked);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event){
        Action action = event.getAction();
        if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK){
            if(event.hasItem()){
                if(event.getItem().getType() == Material.SUGAR){
                    if(plugin.getPvpClassManager().getEquippedClass(event.getPlayer()) != this){
                        return;
                    }

                    Player player = event.getPlayer();
                    UUID uuid = player.getUniqueId();
                    long timestamp = archerSpeedCooldowns.get(uuid);
                    long millis = System.currentTimeMillis();
                    long remaining = timestamp == archerSpeedCooldowns.getNoEntryValue() ? -1L : timestamp - millis;
                    if(remaining > 0L){
                        player.sendMessage(plugin.getMessages().getString("PvPClass.Archer.Speed-Cooldown")
                                .replace("{speedCoolDownRemaining}", DurationFormatUtils.formatDurationWords(remaining, true, true)));
                    }else{
                        ItemStack stack = player.getItemInHand();
                        if(stack.getAmount() == 1){
                            player.setItemInHand(new ItemStack(Material.AIR, 1));
                        }else{
                            stack.setAmount(stack.getAmount() - 1);
                        }

                        plugin.getEffectRestorer().setRestoreEffect(player, ARCHER_SPEED_EFFECT);
                        archerSpeedCooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis() + ARCHER_SPEED_COOLDOWN_DELAY);
                    }
                }else if(event.getItem().getType() == Material.FEATHER){
                    if(plugin.getPvpClassManager().getEquippedClass(event.getPlayer()) != this){
                        return;
                    }

                    Player player = event.getPlayer();
                    UUID uuid = player.getUniqueId();
                    long timestamp = archerJumpCooldowns.get(uuid);
                    long millis = System.currentTimeMillis();
                    long remaining = timestamp == archerJumpCooldowns.getNoEntryValue() ? -1L : timestamp - millis;
                    if(remaining > 0L){
                        player.sendMessage(ChatColor.RED + "You cannot use this ability for another " + DurationFormatUtils.formatDurationWords(remaining, true, true) + " seconds.");
                    }else{
                        ItemStack stack = player.getItemInHand();
                        if(stack.getAmount() == 1){
                            player.setItemInHand(new ItemStack(Material.AIR, 1));
                        }else{
                            stack.setAmount(stack.getAmount() - 1);
                        }

                        plugin.getEffectRestorer().setRestoreEffect(player, ARCHER_JUMP_EFFECT);
                        archerJumpCooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis() + ARCHER_JUMP_COOLDOWN_DELAY);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTimerExpire(TimerExpireEvent event){
        Optional<UUID> userUUID = event.getUserUUID();

        if(!userUUID.isPresent()){
            return;
        }

        UUID user = userUUID.get();

        marked.remove(user);
        for(Marks marks : archerMarks.values()){
            if(marks.isMarked(user) && event.getTimer().getName().equals(marks.getTimerName())){
                marks.removeMarkedPlayer(user, false);

                Player marked = event.getPlayer().orElseGet(() -> plugin.getServer().getPlayer(user));
                Player archer = plugin.getServer().getPlayer(marks.getArcherUUID());

                if(marked != null && marked.isOnline()){
                    for(Player player : plugin.getServer().getOnlinePlayers()){
                        plugin.getScoreboardHandler().getBoard(player).addUpdate(marked);
                    }

                    if(archer == null || !archer.isOnline()){
                        return;
                    }
                }else{
                    return;
                }

                marked.sendMessage(plugin.getMessages().getString("PvPClass.Archer.Marked.Expired")
                        .replace("{player}", archer.getName()));
                archer.sendMessage(plugin.getMessages().getString("PvPClass.Archer.Archer.Expired")
                        .replace("{player}", marked.getName()));
            }
        }
    }

    @Override
    public boolean isApplicableFor(Player player){
        PlayerInventory playerInventory = player.getInventory();

        ItemStack helmet = playerInventory.getHelmet();
        if(helmet == null || helmet.getType() != Material.LEATHER_HELMET) return false;

        ItemStack chestplate = playerInventory.getChestplate();
        if(chestplate == null || chestplate.getType() != Material.LEATHER_CHESTPLATE) return false;

        ItemStack leggings = playerInventory.getLeggings();
        if(leggings == null || leggings.getType() != Material.LEATHER_LEGGINGS) return false;

        ItemStack boots = playerInventory.getBoots();
        return !(boots == null || boots.getType() != Material.LEATHER_BOOTS);
    }

    @Override
    public void provideScoreboard(Player player, List<String> lines){
        if(hasMarks(player.getUniqueId())){
            Marks playerMarks = getMarks(player.getUniqueId());

            playerMarks.getMarkedPlayers().stream().limit(3).forEachOrdered(i -> {
                Player target = Bukkit.getPlayer(i);

                if(target != null){
                    String targetName = target.getName();
                    targetName = targetName.substring(0, Math.min(targetName.length(), 16));

                    for(String message : plugin.getMessages().getString("scoreboard.classes.archer.target").split("\n")){
                        lines.add(message.replace("{target}", targetName).replace("{remaining}", DurationFormatter.getRemaining(playerMarks.getTimer().getRemaining(i), true)));
                    }
                }
            });
        }

        long remaining;
        if((remaining = getArcherJumpCooldownRemaining(player.getUniqueId())) > 0){
            lines.add(plugin.getMessages().getString("scoreboard.classes.archer.effects.jump").replace("{remaining}", DurationFormatter.getRemaining(remaining, true)));
        }

        if((remaining = getArcherSpeedCooldownRemaining(player.getUniqueId())) > 0){
            lines.add(plugin.getMessages().getString("scoreboard.classes.archer.effects.speed").replace("{remaining}", DurationFormatter.getRemaining(remaining, true)));
        }
    }

    public Marks getMarks(UUID uuid){
        return archerMarks.get(uuid);
    }

    public boolean hasMarks(UUID uuid){
        return archerMarks.containsKey(uuid);
    }

    public ConcurrentHashMap<UUID, Marks> getAllMarks(){
        return archerMarks;
    }

    public boolean isMarked(UUID uuid){
        return marked.contains(uuid);
    }

    public long getArcherSpeedCooldownRemaining(UUID uuid){
        long timestamp = archerSpeedCooldowns.get(uuid);
        return timestamp == archerSpeedCooldowns.getNoEntryValue() ? -1L : archerSpeedCooldowns.get(uuid) - System.currentTimeMillis();
    }

    public long getArcherJumpCooldownRemaining(UUID uuid){
        long timestamp = archerJumpCooldowns.get(uuid);
        return timestamp == archerJumpCooldowns.getNoEntryValue() ? -1L : archerSpeedCooldowns.get(uuid) - System.currentTimeMillis();
    }
}
