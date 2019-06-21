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

import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.ClaimableFaction;
import org.hcgames.hcfactions.faction.Faction;
import technology.brk.util.cuboid.Cuboid;
import technology.brk.util.cuboid.NamedCuboid;
import technology.brk.util.mongo.Mongoable;

import java.util.Map;
import java.util.SplittableRandom;
import java.util.UUID;

public class Claim extends NamedCuboid implements Cloneable, ConfigurationSerializable, Mongoable {

    private static final SplittableRandom RANDOM = new SplittableRandom();

    private final UUID claimUniqueID;
    private final UUID factionUUID;

    public Claim(Faction faction, Location location1, Location location2) {
        super(location1, location2);
        this.name = generateName();
        this.factionUUID = faction.getUniqueID();
        this.claimUniqueID = UUID.randomUUID();
    }

    public Claim(Faction faction, World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        super(world, x1, y1, z1, x2, y2, z2);
        this.name = generateName();
        this.factionUUID = faction.getUniqueID();
        this.claimUniqueID = UUID.randomUUID();
    }

    public Claim(Faction faction, Cuboid cuboid) {
        super(cuboid);
        this.name = generateName();
        this.factionUUID = faction.getUniqueID();
        this.claimUniqueID = UUID.randomUUID();
    }

    public Claim(Map<String, Object> map) {
        super(map);

        this.name = (String) map.get("name");
        this.claimUniqueID = UUID.fromString((String) map.get("claimUUID"));
        this.factionUUID = UUID.fromString((String) map.get("factionUUID"));
    }

    public Claim(Document object){
        super(object);

        name = object.getString("name");
        claimUniqueID = UUID.fromString(object.getString("claimUUID"));
        factionUUID = UUID.fromString(object.getString("factionUUID"));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("name", name);
        map.put("claimUUID", claimUniqueID.toString());
        map.put("factionUUID", factionUUID.toString());
        return map;
    }

    @Override
    public Document toDocument(){
        Document document = super.toDocument();
        document.put("name", name);
        document.put("claimUUID", claimUniqueID.toString());
        document.put("factionUUID", factionUUID.toString());
        return document;
    }

    public Claim(Faction faction, Location location) {
        super(location, location);
        this.name = generateName();
        this.factionUUID = faction.getUniqueID();
        this.claimUniqueID = UUID.randomUUID();
    }

    private String generateName() {
        return String.valueOf(RANDOM.nextInt(899) + 100);
    }

    public UUID getClaimUniqueID() {
        return claimUniqueID;
    }

    private ClaimableFaction faction;
    private boolean loaded = false;

    public ClaimableFaction getFaction() throws NoFactionFoundException{
        if (!this.loaded) {
            Faction faction = JavaPlugin.getPlugin(HCFactions.class).getFactionManager().getFaction(factionUUID);

            if (faction instanceof ClaimableFaction) {
                this.faction = (ClaimableFaction) faction;
            }

            this.loaded = true;
        }

        return this.faction;
    }

    /**
     * Gets the formatted name for this {@link Claim}.
     *
     * @return the {@link Claim} formatted name
     */
    public String getFormattedName() {
        return getName() + ": (" + worldName + ", " + x1 + ", " + y1 + ", " + z1 + ") - (" + worldName + ", " + x2 + ", " + y2 + ", " + z2 + ')';
    }

    @Override
    public Claim clone() {
        return (Claim) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Claim blocks = (Claim) o;

        if (loaded != blocks.loaded) return false;
        if (claimUniqueID != null ? !claimUniqueID.equals(blocks.claimUniqueID) : blocks.claimUniqueID != null) return false;
        if (factionUUID != null ? !factionUUID.equals(blocks.factionUUID) : blocks.factionUUID != null) return false;
        return !(faction != null ? !faction.equals(blocks.faction) : blocks.faction != null);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (claimUniqueID != null ? claimUniqueID.hashCode() : 0);
        result = 31 * result + (factionUUID != null ? factionUUID.hashCode() : 0);
        result = 31 * result + (faction != null ? faction.hashCode() : 0);
        result = 31 * result + (loaded ? 1 : 0);
        return result;
    }
}
