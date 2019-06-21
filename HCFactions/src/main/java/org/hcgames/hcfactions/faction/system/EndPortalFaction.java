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

package org.hcgames.hcfactions.faction.system;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.faction.ClaimableFaction;
import technology.brk.util.mongo.Mongoable;

import java.util.Map;
import java.util.UUID;

public class EndPortalFaction extends ClaimableFaction implements ConfigurationSerializable, Mongoable, SystemFaction{

    private final static UUID FACTION_UUID = UUID.fromString("f251b1ae-cc56-4ef0-9154-b1da95cee84e");

    public EndPortalFaction() {
        super("EndPortal", FACTION_UUID);
        displayName = "End Portal";

        HCFactions plugin = JavaPlugin.getPlugin(HCFactions.class);

        World overworld = Bukkit.getServer().getWorlds().get(0);
        int maxHeight = overworld.getMaxHeight();

        int min = plugin.getConfiguration().getEndPortalCenter() - plugin.getConfiguration().getEndPortalRadius();
        int max = plugin.getConfiguration().getEndPortalCenter() + plugin.getConfiguration().getEndPortalRadius();

        // North East (++)
        addClaim(new Claim(this, new Location(overworld, min, 0, min), new Location(overworld, max, maxHeight, max)));

        // South West (--)
        addClaim(new Claim(this, new Location(overworld, -max, maxHeight, -max), new Location(overworld, -min, 0, -min)));

        // North West (-+)
        addClaim(new Claim(this, new Location(overworld, -max, 0, min), new Location(overworld, -min, maxHeight, max)));

        // South East (+-)
        addClaim(new Claim(this, new Location(overworld, min, 0, -max), new Location(overworld, max, maxHeight, -min)));
    }

    public EndPortalFaction(Map<String, Object> map) {
        super(map);
    }

    public EndPortalFaction(Document object){
        super(object);
    }

    public static UUID getUUID() {
        return FACTION_UUID;
    }
}
