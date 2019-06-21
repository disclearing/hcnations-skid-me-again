/*
 *   COPYRIGHT NOTICE
 *
 *   Copyright (C) 2016, SystemUpdate, <admin@systemupdate.io>.
 *
 *   All rights reserved.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS. IN
 *   NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *   DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 *   OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 *   OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *   Except as contained in this notice, the name of a copyright holder shall not
 *   be used in advertising or otherwise to promote the sale, use or other dealings
 *   in this Software without prior written authorization of the copyright holder.
 */

package org.hcgames.hcfactions.listener;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.visualise.VisualType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.claim.ClaimHandler;
import org.hcgames.hcfactions.claim.ClaimSelection;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimWandListener implements Listener{

    private final HCFactions plugin;

    public ClaimWandListener(HCFactions plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        // They didn't use a claiming wand for this action, so ignore.
        if (action == Action.PHYSICAL || !event.hasItem() || !isClaimingWand(event.getItem())) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Clearing the claim selection of player.
        if (action == Action.RIGHT_CLICK_AIR) {
            plugin.getClaimHandler().clearClaimSelection(player);
            player.setItemInHand(new ItemStack(Material.AIR, 1));
            player.sendMessage(plugin.getMessages().getString("factions.claiming.wand.cleared"));
            return;
        }

        PlayerFaction playerFaction;
        try{
            playerFaction = plugin.getFactionManager().getPlayerFaction(uuid);
        } catch (NoFactionFoundException e){
            plugin.getClaimHandler().clearClaimSelection(player);
            player.setItemInHand(new ItemStack(Material.AIR, 1));
            return;
        }

        // Purchasing the claim from the selections.
        if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && player.isSneaking()) {
            ClaimSelection claimSelection = plugin.getClaimHandler().claimSelectionMap.get(uuid);
            if (claimSelection == null || !claimSelection.hasBothPositionsSet()) {
                player.sendMessage(plugin.getMessages().getString("factions.claiming.wand.need_both_position"));
                return;
            }

            if (plugin.getClaimHandler().tryPurchasing(player, claimSelection.toClaim(playerFaction))) {
                plugin.getClaimHandler().clearClaimSelection(player);
                player.setItemInHand(new ItemStack(Material.AIR, 1));
            }
            return;
        }

        // Setting the positions for the claim selection;
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            Location blockLocation = block.getLocation();

            // Don't hoe the soil block.
            if (action == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }

            if (plugin.getClaimHandler().canClaimHere(player, blockLocation)) {
                ClaimSelection revert;
                ClaimSelection claimSelection = plugin.getClaimHandler().claimSelectionMap.putIfAbsent(uuid, revert = new ClaimSelection(blockLocation.getWorld()));
                if (claimSelection == null) claimSelection = revert;

                Location oldPosition;
                Location opposite;
                int selectionId;
                switch (action) {
                    case LEFT_CLICK_BLOCK:
                        oldPosition = claimSelection.getPos1();
                        opposite = claimSelection.getPos2();
                        selectionId = 1;
                        break;
                    case RIGHT_CLICK_BLOCK:
                        oldPosition = claimSelection.getPos2();
                        opposite = claimSelection.getPos1();
                        selectionId = 2;
                        break;
                    default:
                        return; // This should never happen.
                }

                // Prevent players clicking in the same spot twice.
                int blockX = blockLocation.getBlockX();
                int blockZ = blockLocation.getBlockZ();
                if (oldPosition != null && blockX == oldPosition.getBlockX() && blockZ == oldPosition.getBlockZ()) {
                    return;
                }

                // Allow at least 1 tick before players can update one of the positions to prevent lag/visual glitches with delayed task below.
                if ((System.currentTimeMillis() - claimSelection.getLastUpdateMillis()) <= ClaimHandler.PILLAR_BUFFER_DELAY_MILLIS) {
                    return;
                }

                if (opposite != null) {
                    int xDiff = Math.abs(opposite.getBlockX() - blockX) + 1; // Add one as it gets a weird offset
                    int zDiff = Math.abs(opposite.getBlockZ() - blockZ) + 1; // Add one as it gets a weird offset
                    if (xDiff < ClaimHandler.MIN_CLAIM_RADIUS || zDiff < ClaimHandler.MIN_CLAIM_RADIUS) {
                        player.sendMessage(plugin.getMessages().getString("factions.claiming.not_wide_enough")
                                .replace("{minClaimRadius}", String.valueOf(ClaimHandler.MIN_CLAIM_RADIUS))
                                .replace("{maxClaimRadius}", String.valueOf(ClaimHandler.MIN_CLAIM_RADIUS)));
                        return;
                    }
                }

                if (oldPosition != null) {
                    HCF.getPlugin().getVisualiseHandler().clearVisualBlocks(player, VisualType.CREATE_CLAIM_SELECTION, visualBlock -> {
                        Location location = visualBlock.getLocation();
                        return location.getBlockX() == oldPosition.getBlockX() && location.getBlockZ() == oldPosition.getBlockZ();
                    });
                }

                if (selectionId == 1) claimSelection.setPos1(blockLocation);
                if (selectionId == 2) claimSelection.setPos2(blockLocation);

                player.sendMessage(plugin.getMessages().getString("factions.claiming.wand.set")
                        .replace("{selectionId}", String.valueOf(selectionId))
                        .replace("{blockX}", String.valueOf(blockX))
                        .replace("{blockZ}", String.valueOf(blockZ)));

                if (claimSelection.hasBothPositionsSet()) {
                    Claim claim = claimSelection.toClaim(playerFaction);
                    int selectionPrice = claimSelection.getPrice(playerFaction, false);
                    player.sendMessage(plugin.getMessages().getString("factions.claiming.wand.selection_cost")
                            .replace("{canAffordColour}", (selectionPrice > playerFaction.getBalance() ? ChatColor.RED.toString() : ChatColor.GREEN.toString()))
                            .replace("{selectionPrice}", String.valueOf(selectionPrice))
                            .replace("{claimWidth}", String.valueOf(claim.getWidth()))
                            .replace("{claimLength}", String.valueOf(claim.getLength()))
                            .replace("{claimArea}", String.valueOf(claim.getArea())));
                }

                final int blockY = block.getY();
                final int maxHeight = player.getWorld().getMaxHeight();
                final List<Location> locations = new ArrayList<>(maxHeight);
                for (int i = blockY; i < maxHeight; i++) {
                    Location other = blockLocation.clone();
                    other.setY(i);
                    locations.add(other);
                }

                // Generate the new claiming pillar a tick later as right clicking using this
                // event doesn't update the bottom block clicked occasionally.
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        HCF.getPlugin().getVisualiseHandler().generate(player, locations, VisualType.CREATE_CLAIM_SELECTION, true);
                    }
                }.runTask(plugin);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isClaimingWand(event.getPlayer().getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (isClaimingWand(player.getItemInHand())) {
                player.setItemInHand(new ItemStack(Material.AIR, 1));
                plugin.getClaimHandler().clearClaimSelection(player);
            }
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        event.getPlayer().getInventory().remove(plugin.getClaimHandler().getClaimWand());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().getInventory().remove(plugin.getClaimHandler().getClaimWand());
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        if (isClaimingWand(item.getItemStack())) {
            item.remove();
            plugin.getClaimHandler().clearClaimSelection(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        if (isClaimingWand(item.getItemStack())) {
            item.remove();
            plugin.getClaimHandler().clearClaimSelection(event.getPlayer());
        }
    }

    // Prevents dropping Claiming Wands on death.
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getDrops().remove(plugin.getClaimHandler().getClaimWand())) {
            plugin.getClaimHandler().clearClaimSelection(event.getEntity());
        }
    }

    // Doesn't get called when opening own inventory.
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            if (player.getInventory().remove(plugin.getClaimHandler().getClaimWand())) {
                plugin.getClaimHandler().clearClaimSelection(player);
            }
        }
    }

    /**
     * Checks if an {@link ItemStack} is a Claiming Wand.
     *
     * @param stack the {@link ItemStack} to check
     * @return true if the {@link ItemStack} is a claiming wand
     */
    public boolean isClaimingWand(ItemStack stack) {
        return stack != null && stack.isSimilar(plugin.getClaimHandler().getClaimWand());
    }
}
