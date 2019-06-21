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

package org.hcgames.hcfactions.manager;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.faction.system.SystemFaction;
import org.hcgames.hcfactions.focus.FocusHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface FactionManager {

    long MAX_DTR_REGEN_MILLIS = TimeUnit.HOURS.toMillis(3L);
    String MAX_DTR_REGEN_WORDS = DurationFormatUtils.formatDurationWords(MAX_DTR_REGEN_MILLIS, true, true);

    SystemFactions systemFactions = new SystemFactions();

    Map<String, ?> getFactionNameMap();

    ImmutableList<Faction> getFactions();

    default Claim getClaimAt(Location location){
        return getClaimAt(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    Claim getClaimAt(World world, int x, int z);

    default Faction getFactionAt(Location location){
        return getFactionAt(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    default Faction getFactionAt(Block block){
        return getFactionAt(block.getLocation());
    }

    Faction getFactionAt(World world, int x, int z);

    <T extends Faction> T getFaction(UUID factionUUID, Class<T> clazz) throws NoFactionFoundException, ClassCastException;

    default Faction getFaction(UUID factionUUID) throws NoFactionFoundException{
        return getFaction(factionUUID, Faction.class);
    }

    <T extends Faction> T getFaction(String factionName, Class<T> clazz) throws NoFactionFoundException, ClassCastException;

    default Faction getFaction(String factionName) throws NoFactionFoundException{
        return getFaction(factionName, Faction.class);
    }

    PlayerFaction getPlayerFaction(UUID playerUUID) throws NoFactionFoundException;

    default PlayerFaction getPlayerFaction(Player player) throws NoFactionFoundException{
        return getPlayerFaction(player.getUniqueId());
    }

    default boolean createFaction(Faction faction){
        return createFaction(faction, Bukkit.getServer().getConsoleSender());
    }

    boolean createFaction(Faction faction, CommandSender sender);

    default boolean removeFaction(Faction faction){
        return createFaction(faction, Bukkit.getServer().getConsoleSender());
    }

    boolean removeFaction(Faction faction, CommandSender sender);

    default boolean hasFaction(Player player){
        try{
            return getPlayerFaction(player) != null;
        }catch (NoFactionFoundException e){
            return false;
        }
    }

    static void registerSystemFaction(Class<? extends SystemFaction> clazz){
        systemFactions.registerSystemFaction(clazz);
    }

    /**
     * This is an advanced method for searching for a faction
     * it is designed to be called from within a command
     * and will perform a series of actions as followed before.
     *
     * To find a faction it will first attempt to find a one
     * by the name. When this fails it will return to looking
     * for a player faction if that is given otherwise it will fail.
     *
     * Once it starts it query for a player faction, it will first
     * try to find a player online with that name to resolve the UUID
     * however if no player is found it will automatically switch to an
     * async based search and load the players UUID from Mojang's Web API.
     *
     * @param query The search string
     * @param callback The method to call once finished.
     */
    default <T extends Faction> void advancedSearch(String query, Class<T> classType, SearchCallback<T> callback){
        advancedSearch(query, classType, callback, false);
    }

    <T extends Faction> void advancedSearch(String query, Class<T> classType, SearchCallback<T> callback, boolean forcePlayerSearch);

    boolean containsFaction(Faction faction);

    FocusHandler getFocusHandler();

    void reloadFactionData();

    void saveFactionData();
}
