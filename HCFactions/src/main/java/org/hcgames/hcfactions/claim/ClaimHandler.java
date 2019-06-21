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

package org.hcgames.hcfactions.claim;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.visualise.VisualType;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.ClaimableFaction;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.faction.system.RoadFaction;
import org.hcgames.hcfactions.faction.system.WildernessFaction;
import org.hcgames.hcfactions.manager.FactionManager;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.ItemBuilder;
import technology.brk.util.cuboid.Cuboid;
import technology.brk.util.cuboid.CuboidDirection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ClaimHandler {

    public static final int MIN_CLAIM_HEIGHT = 0;
    public static final int MAX_CLAIM_HEIGHT = 256;

    public static final long PILLAR_BUFFER_DELAY_MILLIS = 200L;

    private static final int NEXT_PRICE_MULTIPLIER_AREA = 250;   // the area a claim cuboid needs until the price multiplier is increased
    private static final int NEXT_PRICE_MULTIPLIER_CLAIM = 500;  // the amount each claim a faction has will add onto the final price

    public static final int MIN_CLAIM_RADIUS = 5;
    private static final int MAX_CHUNKS_PER_LIMIT = 16;
    private static final int CLAIM_BUFFER_RADIUS = 4;

    public final Map<UUID, ClaimSelection> claimSelectionMap;
    private final HCFactions plugin;

    @Getter
    private final ItemStack claimWand;

    public ClaimHandler(HCFactions plugin) {
        this.plugin = plugin;
        this.claimSelectionMap = new HashMap<>();

        claimWand = new ItemBuilder(Material.DIAMOND_HOE).displayName(plugin.getMessages().getString("factions.claiming.wand.item.name"))
                .lore(plugin.getMessages().getStringList("factions.claiming.wand.item.lore")).build();
    }

    //TODO: Better configurability
    private static final double CLAIM_SELL_MULTIPLIER = 0.8;
    private static final double CLAIM_PRICE_PER_BLOCK = 0.25;

    /**
     * Gets the price of this {@link Claim} for a given {@link Faction}.
     *
     * @param claim         the {@link Cuboid} to calculate
     * @param currentClaims the current amount of claims the object being looked up has
     * @param selling       if the {@link Faction} is selling the claim
     * @return the price of the {@link Claim}
     */
    public int calculatePrice(Cuboid claim, int currentClaims, boolean selling) {
        if (currentClaims == -1 || !claim.hasBothPositionsSet()) {
            return 0;
        }

        int multiplier = 1;
        int remaining = claim.getArea();
        double price = 0;
        while (remaining > 0) {
            if (--remaining % NEXT_PRICE_MULTIPLIER_AREA == 0) {
                multiplier++;
            }

            price += (CLAIM_PRICE_PER_BLOCK * multiplier);
        }

        if (currentClaims != 0) {
            currentClaims = Math.max(currentClaims + (selling ? -1 : 0), 0);
            price += (currentClaims * NEXT_PRICE_MULTIPLIER_CLAIM);
        }

        if (selling) {
            price *= CLAIM_SELL_MULTIPLIER; // if selling the claim, make the price cheaper (currently 80%).
        }

        return (int) price;
    }

    public boolean clearClaimSelection(Player player) {
        ClaimSelection claimSelection = plugin.getClaimHandler().claimSelectionMap.remove(player.getUniqueId());
        if (claimSelection != null) {
            HCF.getPlugin().getVisualiseHandler().clearVisualBlocks(player, VisualType.CREATE_CLAIM_SELECTION, null);
            return true;
        }

        return false;
    }

    /**
     * Checks if a {@link Player} is eligible to {@link Claim} at a  given {@link Location}.
     *
     * @param player   the {@link Player} to check for
     * @param location the {@link Location} to check at
     * @return true if the {@link Player} can Claim at the {@link Location}
     */
    public boolean canClaimHere(Player player, Location location) {
        World world = location.getWorld();

        if (world.getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage(plugin.getMessages().getString("factions.claiming.overworld_only"));
            return false;
        }

        if (!(plugin.getFactionManager().getFactionAt(location) instanceof WildernessFaction)) {
            player.sendMessage(plugin.getMessages().getString("factions.claiming.wilderness_only")
                    .replace("{wildernessColour}", plugin.getConfiguration().getRelationColourWilderness().toString())
                    .replace("{warzoneRadius}", String.valueOf(plugin.getConfiguration().getWarzoneRadiusOverworld())));
            return false;
        }

        PlayerFaction playerFaction;
        try{
            playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        }catch (NoFactionFoundException e){
            player.sendMessage(plugin.getMessages().getString("factions.claiming.faction_required"));
            return false;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            player.sendMessage(plugin.getMessages().getString("factions.claiming.officer_required"));
            return false;
        }

        if (playerFaction.getClaims().size() >= plugin.getConfiguration().getFactionMaxClaims()) {
            player.sendMessage(plugin.getMessages().getString("factions.claiming.max_claims_reached")
                    .replace("{maxClaims}", String.valueOf(plugin.getConfiguration().getFactionMaxClaims())));
            return false;
        }

        int locX = location.getBlockX();
        int locZ = location.getBlockZ();

        final FactionManager factionManager = plugin.getFactionManager();
        boolean flag = plugin.getConfiguration().isAllowClaimsBesidesRoads();

        for (int x = locX - CLAIM_BUFFER_RADIUS; x < locX + CLAIM_BUFFER_RADIUS; x++) {
            for (int z = locZ - CLAIM_BUFFER_RADIUS; z < locZ + CLAIM_BUFFER_RADIUS; z++) {
                Faction factionAtNew = factionManager.getFactionAt(world, x, z);
                if(factionAtNew instanceof ClaimableFaction && playerFaction != factionAtNew){
                    if(factionAtNew instanceof RoadFaction && flag){
                        continue;
                    }

                    player.sendMessage(plugin.getMessages().getString("factions.claiming.enemy_claims_nearby")
                            .replace("{radius}", String.valueOf(CLAIM_BUFFER_RADIUS)));
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Tries to purchase a {@link Claim} for a {@link Player}s {@link PlayerFaction}'.
     *
     * @param player the {@link Player} that is attempting to create the {@link Claim}
     * @param claim  the {@link Claim} to be created
     * @return true if {@link Player} could create the {@link Claim}
     */
    public boolean tryPurchasing(Player player, Claim claim) {
        Objects.requireNonNull(claim, "Claim is null");
        World world = claim.getWorld();

        if (world.getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage(plugin.getMessages().getString("factions.claiming.overworld_only"));
            return false;
        }

        PlayerFaction playerFaction;
        try{
            playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        }catch (NoFactionFoundException e){
            player.sendMessage(plugin.getMessages().getString("factions.claiming.faction_required"));
            return false;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            player.sendMessage(plugin.getMessages().getString("factions.claiming.officer_required"));
            return false;
        }

        if (playerFaction.getClaims().size() >= plugin.getConfiguration().getFactionMaxClaims()) {
            player.sendMessage(plugin.getMessages().getString("factions.claiming.max_claims_reached")
                    .replace("{maxClaims}", String.valueOf(plugin.getConfiguration().getFactionMaxClaims())));
            return false;
        }

        int factionBalance = playerFaction.getBalance();
        int claimPrice = calculatePrice(claim, playerFaction.getClaims().size(), false);

        if (claimPrice > factionBalance) {
            player.sendMessage(plugin.getMessages().getString("factions.claiming.insufficient_funds")
                    .replace("{factionBalance}", String.valueOf(factionBalance))
                    .replace("{claimPrice}", String.valueOf(claimPrice)));
            return false;
        }

        if (claim.getChunks().size() > MAX_CHUNKS_PER_LIMIT && !player.hasPermission("hcf.bypass.max_claim_size")) {
            player.sendMessage(plugin.getMessages().getString("factions.claiming.too_many_chunks")
                    .replace("{maxChunks}", String.valueOf(MAX_CHUNKS_PER_LIMIT)));
            return false;
        }

        // Is not enough blocks wide.
        if (claim.getWidth() < MIN_CLAIM_RADIUS || claim.getLength() < MIN_CLAIM_RADIUS) {
            player.sendMessage(plugin.getMessages().getString("factions.claiming.not_wide_enough")
                    .replace("{minClaimRadius}", String.valueOf(MIN_CLAIM_RADIUS))
                    .replace("{maxClaimRadius}", String.valueOf(MIN_CLAIM_RADIUS)));
            return false;
        }

        int minimumX = claim.getMinimumX();
        int maximumX = claim.getMaximumX();
        int minimumZ = claim.getMinimumZ();
        int maximumZ = claim.getMaximumZ();

        final FactionManager factionManager = plugin.getFactionManager();
        for (int x = minimumX; x < maximumX; x++) {
            for (int z = minimumZ; z < maximumZ; z++) {
                Faction factionAt = factionManager.getFactionAt(world, x, z);
                if (factionAt != null && !(factionAt instanceof WildernessFaction)) {
                    player.sendMessage(plugin.getMessages().getString("factions.claiming.claim_part_in_wilderness"));
                    return false;
                }
            }
        }

        boolean flag = plugin.getConfiguration().isAllowClaimsBesidesRoads();
        for (int x = minimumX - CLAIM_BUFFER_RADIUS; x < maximumX + CLAIM_BUFFER_RADIUS; x++) {
            for (int z = minimumZ - CLAIM_BUFFER_RADIUS; z < maximumZ + CLAIM_BUFFER_RADIUS; z++) {
                Faction factionAtNew = factionManager.getFactionAt(world, x, z);
                if(factionAtNew instanceof ClaimableFaction && playerFaction != factionAtNew){
                    if(factionAtNew instanceof RoadFaction && flag){
                        continue;
                    }

                    player.sendMessage(plugin.getMessages().getString("factions.claiming.enemy_claims_nearby")
                            .replace("{radius}", String.valueOf(CLAIM_BUFFER_RADIUS)));
                    return false;
                }
            }
        }

        Location minimum = claim.getMinimumPoint();
        Location maximum = claim.getMaximumPoint();

        Collection<Claim> otherClaims = playerFaction.getClaims();
        boolean conjoined = otherClaims.isEmpty();
        if (!conjoined) {
            for (Claim otherClaim : otherClaims) {
                Cuboid outset = otherClaim.clone().outset(CuboidDirection.HORIZONTAL, 1);
                if (outset.contains(minimum) || outset.contains(maximum)) {
                    conjoined = true;
                    break;
                }
            }

            if (!conjoined) {
                player.sendMessage(plugin.getMessages().getString("factions.claiming.claims_must_be_conjoined"));
                return false;
            }
        }

        // Fit the region.
        claim.setY1(ClaimHandler.MIN_CLAIM_HEIGHT);
        claim.setY2(ClaimHandler.MAX_CLAIM_HEIGHT);

        if (playerFaction.addClaim(claim, player)){
            Location center = claim.getCenter();
            player.sendMessage(plugin.getMessages().getString("factions.claiming.purchased")
                    .replace("{claimPrice}", String.valueOf(claimPrice)));
            playerFaction.setBalance(factionBalance - claimPrice);
            playerFaction.broadcast(plugin.getMessages().getString("factions.claiming.purchased_broadcast")
                    .replace("{player}", player.getName())
                    .replace("{claimX}", String.valueOf(center.getBlockX()))
                    .replace("{claimZ}", String.valueOf(center.getBlockZ())),
                    player.getUniqueId());
            return true;
        }


        return false;
    }
}
