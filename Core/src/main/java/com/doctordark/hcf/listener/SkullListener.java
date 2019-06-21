package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.doctordark.util.Permissions;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

//TODO Rewrite
@RequiredArgsConstructor
public class SkullListener implements Listener{

    private final HCF plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity();

        Player killer = event.getEntity().getKiller();

        if(killer == null){
            return;
        }

        ItemStack killItem = killer.getItemInHand();

        if(killItem == null || killItem.getType() != Material.DIAMOND_SWORD){
            return;
        }

        if(killer.hasPermission("prime.skull.drop")){
            ItemStack skullItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta itemMeta = (SkullMeta) skullItem.getItemMeta();

            itemMeta.setOwner(player.getName());
            itemMeta.setDisplayName(plugin.getMessagesOld().getString("Event-Death-SkullName")
                    .replace("{player}", player.getName()));

            skullItem.setItemMeta(itemMeta);
            event.getDrops().add(skullItem);
        }

        String loreMessage = plugin.getMessagesOld().getString("Event-Death-SwordLore")
                .replace("{player}", player.getName())
                .replace("{killer}", player.getKiller().getName())
                .replace("{date}", new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").format(new Date()));

        ItemMeta itemMeta = killItem.getItemMeta();

        if(!itemMeta.hasLore()){
            itemMeta.setLore(Collections.singletonList((loreMessage)));
        }else{
            List<String> lore = itemMeta.getLore();

            if(lore.size() > 10){
                return;
            }

            lore.add(loreMessage);
            itemMeta.setLore(lore);
        }

        killItem.setItemMeta(itemMeta);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event){
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            return;
        }

        Player player = event.getPlayer();
        BlockState blockState = event.getClickedBlock().getState();

        if(!(blockState instanceof Skull)){
            return;
        }

        Skull skull = (Skull) blockState;

        player.sendMessage(plugin.getMessagesOld().getString("Interact-Skull-Message")
                .replace("{player}", skull.getSkullType() == SkullType.PLAYER && skull.hasOwner() ?
                        skull.getOwner() :
                        "a " + WordUtils.capitalizeFully(skull.getSkullType().name())));
    }
}
