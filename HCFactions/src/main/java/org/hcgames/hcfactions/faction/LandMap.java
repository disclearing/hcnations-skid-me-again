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

package org.hcgames.hcfactions.faction;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.visualise.VisualBlockData;
import com.doctordark.hcf.visualise.VisualType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.claim.ClaimHandler;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.manager.FactionManager;
import technology.brk.base.BasePlugin;
import technology.brk.util.BukkitUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LandMap {

    private static final int FACTION_MAP_RADIUS_BLOCKS = 22;

    /**
     * Updates the {@link Faction} {@link Claim} map for a {@link Player}.
     *
     * @param player     the {@link Player} to update for
     * @param plugin     the {@link org.bukkit.plugin.java.JavaPlugin} to update for
     * @param visualType the {@link VisualType} to update for
     * @param inform     if the {@link VisualType} should be informed
     * @return true if their are {@link Claim}s to update the map for
     */
    public static boolean updateMap(Player player, HCFactions plugin, VisualType visualType, boolean inform) {
        Location location = player.getLocation();
        World world = player.getWorld();
        int locationX = location.getBlockX();
        int locationZ = location.getBlockZ();

        int minimumX = locationX - FACTION_MAP_RADIUS_BLOCKS;
        int minimumZ = locationZ - FACTION_MAP_RADIUS_BLOCKS;
        int maximumX = locationX + FACTION_MAP_RADIUS_BLOCKS;
        int maximumZ = locationZ + FACTION_MAP_RADIUS_BLOCKS;

        final Set<Claim> board = new LinkedHashSet<>();

        for (int x = minimumX; x <= maximumX; x++) {
            for (int z = minimumZ; z <= maximumZ; z++) {
                Claim claim = plugin.getFactionManager().getClaimAt(world, x, z);
                if (claim != null) {
                    board.add(claim);
                }
            }
        }

        if (board.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Nothing to visualise for " + visualType.name().toLowerCase() + " within " + FACTION_MAP_RADIUS_BLOCKS + " blocks of you.");
            return false;
        }

        for (Claim claim : board) {
            int maxHeight = Math.min(world.getMaxHeight(), ClaimHandler.MAX_CLAIM_HEIGHT);
            Location[] corners = claim.getCornerLocations();
            List<Location> shown = new ArrayList<>(maxHeight * corners.length);
            for (Location corner : corners) {
                for (int y = 0; y < maxHeight; y++) {
                    shown.add(world.getBlockAt(corner.getBlockX(), y, corner.getBlockZ()).getLocation());
                }
            }

            Map<Location, VisualBlockData> dataMap = HCF.getPlugin().getVisualiseHandler().generate(player, shown, visualType, true);
            if (dataMap.isEmpty()) continue;
            String materialName = BasePlugin.getPlugin().getItemDb().getName(new ItemStack(dataMap.entrySet().iterator().next().getValue().getItemType(), 1));

            if (inform) {
                try {
                    player.sendMessage(ChatColor.YELLOW + claim.getFaction().getFormattedName(player) + ChatColor.YELLOW + " owns land " + ChatColor.WHITE + claim.getName() +
                            ChatColor.GRAY + " (displayed with " + materialName + ')' + ChatColor.YELLOW + '.');
                } catch (NoFactionFoundException ignored) {}
            }
        }

        return true;
    }

    /**
     * Finds the nearest safe {@link Location} from a given position.
     *
     * @param player       the {@link Player} to find for
     * @param origin       the {@link Location} to begin searching at
     * @param searchRadius the radius to search for
     * @return the nearest safe {@link Location} from origin
     */
    public static Location getNearestSafePosition(Player player, Location origin, int searchRadius) {
        FactionManager factionManager = JavaPlugin.getPlugin(HCFactions.class).getFactionManager();

        if(!factionManager.hasFaction(player)){
            return null;
        }

        Faction playerFaction = factionManager.getPlayerFaction(player.getUniqueId());

        int minX = origin.getBlockX() - searchRadius;
        int maxX = origin.getBlockX() + searchRadius;
        int minZ = origin.getBlockZ() - searchRadius;
        int maxZ = origin.getBlockZ() + searchRadius;

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                Location atPos = origin.clone().add(x, 0, z);
                Faction factionAtPos = factionManager.getFactionAt(atPos);
                if (Objects.equals(factionAtPos, playerFaction) || !(factionAtPos instanceof PlayerFaction)) {
                    return BukkitUtils.getHighestLocation(atPos, atPos).add(0, 1.5, 0);
                }

                Location atNeg = origin.clone().add(x, 0, z);
                Faction factionAtNeg = factionManager.getFactionAt(atNeg);
                if (Objects.equals(factionAtNeg, playerFaction) || !(factionAtNeg instanceof PlayerFaction)) {
                    return BukkitUtils.getHighestLocation(atNeg, atNeg).add(0, 1.5, 0);
                }
            }
        }

        return null;
    }

}
