package com.doctordark.hcf.listener.fixes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import technology.brk.util.BukkitUtils;

import java.util.UUID;

public class BlockHitFixListener implements Listener{

    private static final long THRESHOLD = 850L;
    private static final ImmutableSet<Material> NON_TRANSPARENT_ATTACK_BREAK_TYPES = Sets.immutableEnumSet(
            Material.GLASS,
            Material.STAINED_GLASS,
            Material.STAINED_GLASS_PANE
    );

    private final TObjectLongMap<UUID> lastInteractTimes = new TObjectLongHashMap<>();


    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event){
        if(event.isCancelled() && NON_TRANSPARENT_ATTACK_BREAK_TYPES.contains(event.getBlock().getType())){
            cancelAttackingMillis(event.getPlayer().getUniqueId(), THRESHOLD);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageEvent event){
        Player attacker = BukkitUtils.getFinalAttacker(event, true);
        if(attacker != null){
            long lastInteractTime = this.lastInteractTimes.get(attacker.getUniqueId());
            if(lastInteractTime != this.lastInteractTimes.getNoEntryValue() && lastInteractTime - System.currentTimeMillis() > 0L){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        this.lastInteractTimes.remove(event.getPlayer().getUniqueId());
    }

    private void cancelAttackingMillis(UUID uuid, long delay){
        this.lastInteractTimes.put(uuid, System.currentTimeMillis() + delay);
    }
}
