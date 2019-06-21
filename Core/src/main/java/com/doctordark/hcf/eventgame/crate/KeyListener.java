package com.doctordark.hcf.eventgame.crate;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.EventType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.system.SpawnFaction;
import technology.brk.util.InventoryUtils;

import java.util.List;

/**
 * Listener that listens to when an {@link Key} has been used.
 */
public class KeyListener implements Listener{

    private final HCF plugin;

    public KeyListener(HCF plugin){
        this.plugin = plugin;
    }

    // Prevent placing the keys on blocks.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event){
        Key key = plugin.getKeyManager().getKey(event.getItemInHand());
        if(key != null){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event){
        Inventory inventory = event.getInventory();
        Inventory topInventory = event.getView().getTopInventory();
        if(inventory != null && topInventory != null && topInventory.equals(inventory) && topInventory.getTitle().endsWith(" Key Reward" + ChatColor.AQUA + ChatColor.BOLD)){ //TODO: More reliable
            Player player = (Player) event.getPlayer();
            Location location = player.getLocation();
            World world = player.getWorld();
            boolean isEmpty = true;
            for(ItemStack stack : topInventory.getContents()){
                if(stack != null && stack.getType() != Material.AIR){
                    world.dropItemNaturally(location, stack);
                    isEmpty = false;
                }
            }

            if(!isEmpty){
                player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "You closed your loot crate reward inventory, dropped on the ground for you.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event){
        Inventory inventory = event.getInventory();
        Inventory topInventory = event.getView().getTopInventory();
        if(inventory != null && topInventory != null && topInventory.equals(inventory) && topInventory.getTitle().endsWith(" Key Reward" + ChatColor.AQUA + ChatColor.BOLD)){ //TODO: More reliable
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event){
        Inventory clickedInventory = event.getClickedInventory();
        Inventory topInventory = event.getView().getTopInventory();
        if(clickedInventory == null || topInventory == null || !topInventory.getTitle().endsWith(" Key Reward" + ChatColor.AQUA + ChatColor.BOLD)){
            return;
        }

        InventoryAction action = event.getAction();
        if(!topInventory.equals(clickedInventory) && (action == InventoryAction.MOVE_TO_OTHER_INVENTORY)){
            event.setCancelled(true);
        }else if(topInventory.equals(clickedInventory) && (action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE || action == InventoryAction.PLACE_SOME)){
            event.setCancelled(true);
        }
    }

    private void decrementHand(Player player){
        ItemStack stack = player.getItemInHand();
        if(stack.getAmount() <= 1){
            player.setItemInHand(new ItemStack(Material.AIR, 1));
        }else{
            stack.setAmount(stack.getAmount() - 1);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack stack = event.getItem();

        // Keys can only be used by right clicking blocks.
        if(action != Action.RIGHT_CLICK_BLOCK) return;

        Key key = plugin.getKeyManager().getKey(stack);

        // No keys were used in the making of this video.
        if(key == null) return;

        Block block = event.getClickedBlock();
        BlockState state = block.getState();
        if(key instanceof EventKey && state instanceof Chest){
            EventKey eventKey = (EventKey) key;
            EventKey.EventKeyData eventKeyData = eventKey.getData(stack.getItemMeta().getLore());
            EventType eventType = eventKeyData.getEventType();
            List<Inventory> inventories = eventKey.getInventories(eventType);
            int inventoryNumber = eventKeyData.getInventoryNumber();

            if(inventories.size() < inventoryNumber){
                player.sendMessage(ChatColor.RED + "This key is for " + eventType.getDisplayName() + ChatColor.RED + " loottable " +
                        inventoryNumber + ", whilst there are only " + inventories.size() + " possible. Please inform an admin.");

                return;
            }

            Location clicked = block.getLocation();
            Faction faction = plugin.getFactions().getFactionManager().getFactionAt(clicked);
            if (!(faction instanceof SpawnFaction)) {
                player.sendMessage(ChatColor.RED + "You may only open event keys in Spawn.");
                return;
            }

            Inventory inventory = inventories.get(inventoryNumber - 1);
            ItemStack[] contents = inventory.getContents();

            // Copy contents into a new EventKey inventory
            Inventory eventInventory = EventKeyInventoryHolder.createInventory(contents.length);
            eventInventory.setContents(contents);

            // Decrement key count and open the inventory for the player
            decrementHand(player);
            player.openInventory(eventInventory);
            event.setCancelled(true);
            player.sendMessage(ChatColor.YELLOW + "You have claimed the loot from your " + ChatColor.AQUA + eventType.getDisplayName()
                    + " " + eventKey.getDisplayName() + ChatColor.YELLOW + " key.");
        }
    }


    @EventHandler
    public void onEventInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof EventKeyInventoryHolder) {
            if (InventoryUtils.isEmpty(inventory)) {
                return;
            }

            // Drop any remaining items at the feet of the player
            for (ItemStack item : inventory) {
                if(item == null) continue;
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            player.sendMessage(ChatColor.YELLOW + "All remaining items in your event inventory have been dropped at your location.");
        }
    }
}
