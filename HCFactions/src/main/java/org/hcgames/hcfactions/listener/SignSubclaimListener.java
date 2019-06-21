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
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.FactionMember;
import org.hcgames.hcfactions.structure.Role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class SignSubclaimListener implements Listener{

    private static final int MAX_SIGN_LINE_CHARS = 16;
    private static final BlockFace[] SIGN_FACES = new BlockFace[]{
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.UP
    };

    private final HCFactions plugin;

    private SubclaimType getSubclaimType(String value, boolean creating) {
        if (creating) {
            value = value.toUpperCase();
        }

        for (SubclaimType type : SubclaimType.values()) {
            if (creating) {
                if (type.aliases.contains(value)) {
                    return type;
                }
            } else {
                if (type.outputText.equals(value)) {
                    return type;
                }
            }
        }

        return null;
    }

    private boolean isSubclaimable(Block block) {
        Material type = block.getType();
        return type == Material.FENCE_GATE || type == Material.TRAP_DOOR || block.getState() instanceof InventoryHolder;
    }

    private SubclaimType getSubclaimType(Sign sign, boolean creating) {
        SubclaimType subclaimType = getSubclaimType(sign.getLine(0), creating);
        return subclaimType != null && subclaimType.isEnabled() ? subclaimType : null;
    }

    private SubclaimType getSubclaimType(Block block, boolean creating) {
        if (isSubclaimable(block)) {
            Collection<Sign> attachedSigns = getAttachedSigns(block);
            for (Sign attachedSign : attachedSigns) {
                SubclaimType subclaimType = getSubclaimType(attachedSign, creating);
                if (subclaimType != null) {
                    return subclaimType;
                }
            }
        }

        return null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        Block block = event.getBlock();
        MaterialData materialData = block.getState().getData();
        if (materialData instanceof org.bukkit.material.Sign) {
            org.bukkit.material.Sign sign = (org.bukkit.material.Sign) materialData;
            Block attachedBlock = block.getRelative(sign.getAttachedFace());
            if (isSubclaimable(attachedBlock)) {
                Player player = event.getPlayer();
                PlayerFaction playerFaction;

                try {
                    playerFaction = plugin.getFactionManager().getPlayerFaction(player);
                } catch (NoFactionFoundException e) {
                    return;
                }

                Faction factionAt = plugin.getFactionManager().getFactionAt(block.getLocation());
                if (playerFaction == factionAt) {
                    SubclaimType subclaimType = getSubclaimType(attachedBlock, false);
                    if (subclaimType != null) {
                        player.sendMessage(plugin.getMessages().getString("factions.subclaims.already_exists")
                                .replace("{subclaimName}", subclaimType.displayName)
                                .replace("{block}", attachedBlock.getName()));
                        return;
                    }

                    String[] lines = event.getLines();
                    subclaimType = getSubclaimType(lines[0], true);
                    if (subclaimType == null || !subclaimType.isEnabled()) {
                        return;
                    }

                    List<String> memberList = null;
                    if (subclaimType == SubclaimType.MEMBER) {
                        memberList = new ArrayList<>(3);
                        for (int i = 1; i < lines.length; i++) {
                            String line = lines[i];
                            if (StringUtils.isNotBlank(line)) {
                                memberList.add(line);
                            }
                        }

                        if (memberList.isEmpty()) {
                            player.sendMessage(plugin.getMessages().getString("factions.subclaims.members_required"));
                            return;
                        }
                    } else if (subclaimType == SubclaimType.CAPTAIN) {
                        if (playerFaction.getMember(player).getRole() == Role.MEMBER) {
                            player.sendMessage(plugin.getMessages().getString("factions.subclaims.officer_required"));
                            return;
                        }

                        // Clear the other lines.
                        event.setLine(1, null);
                        event.setLine(2, null);
                        event.setLine(3, null);
                    } else if (subclaimType == SubclaimType.LEADER) {
                        if (playerFaction.getMember(player).getRole() != Role.LEADER) {
                            player.sendMessage(plugin.getMessages().getString("factions.subclaims.leader_required"));
                            return;
                        }

                        // Clear the other lines.
                        event.setLine(1, null);
                        event.setLine(2, null);
                        event.setLine(3, null);
                    }
                    // Finalise the subclaim.
                    event.setLine(0, subclaimType.outputText);
                    StringBuilder builder = new StringBuilder(plugin.getMessages().getString("factions.subclaims.created_broadcast")
                            .replace("{teammateRelationColour}", String.valueOf(plugin.getConfiguration().getRelationColourTeammate()))
                            .replace("{player}", player.getName())
                            .replace("{block}", attachedBlock.getName())
                            .replace("{blockX}", String.valueOf(attachedBlock.getX()))
                            .replace("{blockZ}", String.valueOf(attachedBlock.getZ())));

                    if (subclaimType == SubclaimType.LEADER) {
                        builder.append("leaders");
                    } else if (subclaimType == SubclaimType.CAPTAIN) {
                        builder.append("captains");
                    } else if (memberList != null) { // Should never be null, but best safe; SubclaimType.PRIVATE
                        builder.append("members ").append(ChatColor.RED).append('[');

                        List<String> membersToAdd = new ArrayList<>();
                        for(String member : memberList){
                            for(FactionMember factionMember : playerFaction.getMembers().values()){
                                if(factionMember.getCachedName().equals(member)){
                                    membersToAdd.add(member);
                                }
                            }
                        }

                        builder.append(Joiner.on(", ").join(membersToAdd)).append("]");
                    }

                    playerFaction.broadcast(builder.toString());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (HCF.getPlugin().getEotwHandler().isEndOfTheWorld()) {//TODO: Use "CoreHook"
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(ProtectionListener.PROTECTION_BYPASS_PERMISSION)) {//TODO: Use org.hcgames.listener.protectionlistener
            return;
        }

        Block block = event.getBlock();
        BlockState state = block.getState();

        Block subclaimObjectBlock = null;
        if (state instanceof Sign) {
            Sign sign = (Sign) state;
            MaterialData signData = sign.getData();
            if (signData instanceof org.bukkit.material.Sign) {
                org.bukkit.material.Sign materialSign = (org.bukkit.material.Sign) signData;
                subclaimObjectBlock = block.getRelative(materialSign.getAttachedFace());
            }
        } else {
            subclaimObjectBlock = block;
        }

        if (subclaimObjectBlock != null && !checkSubclaimIntegrity(player, subclaimObjectBlock)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessages().getString("factions.subclaims.cannot_break").replace("{block}", subclaimObjectBlock.getName()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (HCF.getPlugin().getEotwHandler().isEndOfTheWorld() || !plugin.getConfiguration().isSubclaimHopperCheck()) {//TODO: "CoreHook"
            return;
        }

        // Have to do this hackery since Bukkit doesn't
        // provide an API for us to do this
        InventoryHolder holder = event.getSource().getHolder();
        Collection<Block> sourceBlocks;
        if (holder instanceof Chest) {
            sourceBlocks = Collections.singletonList(((Chest) holder).getBlock());
        } else if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) holder;
            sourceBlocks = Lists.newArrayList(((Chest) doubleChest.getLeftSide()).getBlock(), ((Chest) doubleChest.getRightSide()).getBlock());
        } else {
            return;
        }

        for (Block block : sourceBlocks) {
            if (getSubclaimType(block, false) != null) {
                event.setCancelled(true);
                break;
            }
        }
    }

    private String getShortenedName(String originalName) {
        if (originalName.length() >= MAX_SIGN_LINE_CHARS) {
            originalName = originalName.substring(0, MAX_SIGN_LINE_CHARS);
        }

        return originalName;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(ProtectionListener.PROTECTION_BYPASS_PERMISSION)) {
                return;
            }

            if (HCF.getPlugin().getEotwHandler().isEndOfTheWorld() || HCF.getPlugin().getConfiguration().isKitMap()) {//TODO: Core hook
                return;
            }

            Block block = event.getClickedBlock();
            if (!checkSubclaimIntegrity(player, block)) {
                event.setUseInteractedBlock(Event.Result.DENY);
                player.sendMessage(plugin.getMessages().getString("factions.subclaims.no_access").replace("{block}", block.getName()));
            }
        }
    }

    /**
     * Checks subclaim integrity of a {@link Block} for a {@link Player}.
     *
     * @param player         the {@link Player} to check
     * @param subclaimObject the {@link Block} to check
     * @return true if allowed to open
     */
    private boolean checkSubclaimIntegrity(Player player, Block subclaimObject) {
        if (!isSubclaimable(subclaimObject)) {
            return true; // Not even subclaimed.
        }

        PlayerFaction playerFaction;
        try {
            playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        } catch (NoFactionFoundException e) {
            return true;
        }

        if(playerFaction.isRaidable()){
            return true;
        }

        Role role = playerFaction.getMember(player).getRole();
        if (role == Role.LEADER || role  == Role.COLEADER) {
            return true; // Let leaders & co leaders open regardless.
        }

        if (!playerFaction.getName().equals(plugin.getFactionManager().getFactionAt(subclaimObject).getName())) {
            return true; // Let enemies be able to open
        }

        Collection<Sign> attachedSigns = getAttachedSigns(subclaimObject);
        if (attachedSigns.isEmpty()) {
            return true;
        }

        boolean flag = true;
        String playerName = getShortenedName(player.getName());

        for (Sign attachedSign : attachedSigns) {
            SubclaimType subclaimType = getSubclaimType(attachedSign, false);

            if (subclaimType == null) {
                continue;
            }

            // No need to conditional check leaders as they can open anything.
            if (subclaimType == SubclaimType.CAPTAIN) {
                if (role == Role.MEMBER) {
                    flag = false;
                    continue;
                }

                return true;
            }

            if (subclaimType == SubclaimType.MEMBER){
                if(role == Role.CAPTAIN){
                    return true;
                }

                for(String line : attachedSign.getLines()){
                    if(line.equalsIgnoreCase(playerName)){
                        return true;
                    }
                }
                flag = false;
            }
        }

        return flag;
    }

    /**
     * Gets the attached {@link Sign}s on a {@link Block}.
     *
     * @param block the {@link Block} to get for
     * @return collection of attached {@link Sign}s
     */
    public Collection<Sign> getAttachedSigns(Block block) {
        LinkedHashSet<Sign> results = new LinkedHashSet<>();
        getSignsAround(block, results);

        BlockState state = block.getState();
        if (state instanceof Chest) {
            Inventory chestInventory = ((Chest) state).getInventory();
            if (chestInventory instanceof DoubleChestInventory) {
                DoubleChest doubleChest = ((DoubleChestInventory) chestInventory).getHolder();
                Block left = ((Chest) doubleChest.getLeftSide()).getBlock();
                Block right = ((Chest) doubleChest.getRightSide()).getBlock();
                getSignsAround(left.equals(block) ? right : left, results);
            }
        }

        return results;
    }

    /**
     * Populates a given set with the {@link Sign}s that are attached
     * to a given {@link Block}.
     *
     * @param block   the {@link Block} to get around
     * @param results the input to add to
     * @return the updated set of {@link Sign}s
     */
    private Set<Sign> getSignsAround(Block block, LinkedHashSet<Sign> results) {
        for (BlockFace face : SIGN_FACES) {
            Block relative = block.getRelative(face);
            BlockState relativeState = relative.getState();
            if (relativeState instanceof Sign) {
                org.bukkit.material.Sign materialSign = (org.bukkit.material.Sign) relativeState.getData();
                if (relative.getRelative(materialSign.getAttachedFace()).equals(block)) {
                    results.add((Sign) relative.getState());
                }
            }
        }

        return results;
    }

    private enum SubclaimType {

        LEADER(ImmutableList.of("[LEADER]", Role.LEADER.getAstrix()),
                ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Leader", "Leader"),

        CAPTAIN(ImmutableList.of("[CAPTAIN]", "[OFFICER]", Role.CAPTAIN.getAstrix()),
                ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Captain", "Captain"),

        MEMBER(ImmutableList.of("[PRIVATE]", "[PERSONAL]", "[SUBCLAIM]", "[MEMBER]"),
                ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Subclaim", "Member");

        private final List<String> aliases;
        private final String outputText;
        private final String displayName;

        SubclaimType(List<String> aliases, String outputText, String displayName) {
            this.aliases = aliases;
            this.outputText = outputText;
            this.displayName = displayName;
        }

        public boolean isEnabled() {
            switch (this) {
                case LEADER:
                    return JavaPlugin.getPlugin(HCFactions.class).getConfiguration().isSubclaimSignLeader();
                case CAPTAIN:
                    return JavaPlugin.getPlugin(HCFactions.class).getConfiguration().isSubclaimSignCaptain();
                case MEMBER:
                    return JavaPlugin.getPlugin(HCFactions.class).getConfiguration().isSubclaimSignPrivate();
                default:
                    return false;
            }
        }
    }
}
