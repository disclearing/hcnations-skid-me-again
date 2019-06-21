package com.doctordark.hcf.pvpclass.type;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.pvpclass.PvpClass;
import com.doctordark.hcf.pvpclass.event.PvpClassEquipEvent;
import com.doctordark.hcf.timer.event.TimerClearEvent;
import com.doctordark.hcf.timer.event.TimerStartEvent;
import com.doctordark.hcf.timer.type.CombatTimer;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionEffectExpireEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import technology.brk.util.BukkitUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Represents a {@link PvpClass} used to enhance mining quality.
 */
public class MinerClass extends PvpClass implements Listener{

    // The minimum height level to obtain the Invisibility effect.
    private static final int INVISIBILITY_HEIGHT_LEVEL = 30;
    private static final PotionEffect HEIGHT_INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0);

    private final HashMultimap<UUID, PotionEffectType> appliedEffects = HashMultimap.create();
    private final ImmutableMap<Integer, PotionEffect> milestoneEffects;

    private final HCF plugin;

    public MinerClass(HCF plugin){
        super("Miner", TimeUnit.SECONDS.toMillis(10L));

        this.plugin = plugin;
        this.passiveEffects.add(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 1));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));

        milestoneEffects = ImmutableMap.<Integer, PotionEffect>builder().
                put(200, new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 2)).
                put(300, new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0)).
                put(400, new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0)).
                put(500, new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 3)).
                put(600, new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1)).
                build();
    }

    private void removeInvisibilitySafely(Player player){
        for(PotionEffect active : player.getActivePotionEffects()){
            if(active.getType().equals(PotionEffectType.INVISIBILITY) && active.getDuration() > DEFAULT_MAX_DURATION){
                player.sendMessage(ChatColor.RED + getName() + ChatColor.GOLD + " invisibility removed.");
                player.removePotionEffect(active.getType());
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageByEntityEvent event){
        Entity entity = event.getEntity();
        if(entity instanceof Player && BukkitUtils.getFinalAttacker(event, false) != null){
            Player player = (Player) entity;
            if(plugin.getPvpClassManager().hasClassEquipped(player, this)){
                removeInvisibilitySafely(player);
            }
        }
    }

    @Override
    public void onUnequip(Player player){
        super.onUnequip(player);
        removeInvisibilitySafely(player);

        if(appliedEffects.containsKey(player.getUniqueId())){
            appliedEffects.removeAll(player.getUniqueId()).forEach(player::removePotionEffect);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event){
        conformMinerInvisibility(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        conformMinerInvisibility(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event){
        if(!plugin.getPvpClassManager().hasClassEquipped(event.getPlayer(), this)){
            return;
        }

        if(event.getBlock().getType().equals(Material.DIAMOND_ORE)){
            int count = event.getPlayer().getStatistic(Statistic.MINE_BLOCK, Material.DIAMOND_ORE);

            if(count % 100 == 0){
                //Hit milestone
                checkAndApplyMilestoneEffects(event.getPlayer());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onClassEquip(PvpClassEquipEvent event){
        Player player = event.getPlayer();
        if(event.getPvpClass() == this){
            if(player.getLocation().getBlockY() <= INVISIBILITY_HEIGHT_LEVEL){
                player.addPotionEffect(HEIGHT_INVISIBILITY, true);
                player.sendMessage(ChatColor.AQUA + getName() + ChatColor.GREEN + " invisibility added.");
            }

            checkAndApplyMilestoneEffects(player);
        }
    }

    /**
     * Applies the {@link MinerClass} invisibility {@link PotionEffect} depending
     * on the {@link Player}s {@link Location}.
     *
     * @param player the {@link Player} to apply for
     * @param from   the from {@link Location}
     * @param to     the to {@link Location}
     */
    private void conformMinerInvisibility(Player player, Location from, Location to){
        int fromY = from.getBlockY();
        int toY = to.getBlockY();
        if(fromY != toY && plugin.getPvpClassManager().hasClassEquipped(player, this)){
            boolean isInvisible = player.hasPotionEffect(PotionEffectType.INVISIBILITY);
            if(toY > INVISIBILITY_HEIGHT_LEVEL){
                if(fromY <= INVISIBILITY_HEIGHT_LEVEL && isInvisible){
                    removeInvisibilitySafely(player);
                }
            }else{
                if(!isInvisible){
                    player.addPotionEffect(HEIGHT_INVISIBILITY, true);
                    player.sendMessage(ChatColor.AQUA + getName() + ChatColor.GREEN + " invisibility added.");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTimerStart(TimerStartEvent event){
        if(event.getTimer() instanceof CombatTimer && event.getPlayer().isPresent()){
            Player player = event.getPlayer().get();

            if(plugin.getPvpClassManager().hasClassEquipped(player, this)){
                if(appliedEffects.containsKey(player.getUniqueId())){
                    appliedEffects.removeAll(player.getUniqueId()).forEach(player::removePotionEffect);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTimerClear(TimerClearEvent event){
        if(event.getTimer() instanceof CombatTimer && event.getPlayer().isPresent()){
            Player player = event.getPlayer().get();

            if(plugin.getPvpClassManager().hasClassEquipped(player, this)){
                checkAndApplyMilestoneEffects(player);
            }
        }
    }

    @Override
    public boolean isApplicableFor(Player player){
        PlayerInventory playerInventory = player.getInventory();

        ItemStack helmet = playerInventory.getHelmet();
        if(helmet == null || helmet.getType() != Material.IRON_HELMET)
            return false;

        ItemStack chestplate = playerInventory.getChestplate();
        if(chestplate == null || chestplate.getType() != Material.IRON_CHESTPLATE)
            return false;

        ItemStack leggings = playerInventory.getLeggings();
        if(leggings == null || leggings.getType() != Material.IRON_LEGGINGS)
            return false;

        ItemStack boots = playerInventory.getBoots();
        return !(boots == null || boots.getType() != Material.IRON_BOOTS);
    }

    @Override
    public void provideScoreboard(Player player, List<String> lines){

    }

    private void checkAndApplyMilestoneEffects(Player player){
        int diamondsMined = player.getStatistic(Statistic.MINE_BLOCK, Material.DIAMOND_ORE);

        for(Map.Entry<Integer, PotionEffect> effectEntry : milestoneEffects.entrySet()){
            if(diamondsMined >= effectEntry.getKey()){

                if(canApply(player, effectEntry.getValue())){
                    player.addPotionEffect(effectEntry.getValue(), true);
                    appliedEffects.put(player.getUniqueId(), effectEntry.getValue().getType());
                }

            }
        }
    }

    private boolean canApply(Player player, PotionEffect effectEntry){
        for(PotionEffect effect : player.getActivePotionEffects()){
            if(!effect.getType().equals(effectEntry.getType())){
                continue;
            }

            if(effect.getAmplifier() > effectEntry.getAmplifier()){
                return false;
            }

            if(effect.getDuration() > effectEntry.getDuration()){
                return false;
            }
        }

        return true;
    }

    @EventHandler
    public void onPotionEffectExpire(PotionEffectExpireEvent event){
        if(event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();
            if(plugin.getPvpClassManager().hasClassEquipped(player, this)){
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> checkAndApplyMilestoneEffects(player), 2L);
            }
        }
    }
}
