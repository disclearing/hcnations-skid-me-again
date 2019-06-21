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
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.faction.ClaimableFaction;

import java.util.Map;
import java.util.UUID;

public class SpawnFaction extends ClaimableFaction implements ConfigurationSerializable, SystemFaction {

    private final static UUID FACTION_UUID = UUID.fromString("14379e6d-6005-4fe8-9724-32f1f4653ecc");

    public SpawnFaction() {
        super("Spawn", FACTION_UUID);
        this.safezone = true;

        /*int radius;

        for (World world : Bukkit.getWorlds()) {
            switch(world.getEnvironment()){
                case NORMAL:
                    radius = plugin.getConfiguration().getSpawnRadiusOverworld();
                    if(radius > 0){
                        addClaim(new Claim(this, new Location(world, radius, 0, radius), new Location(world, -radius, world.getMaxHeight(), -radius)));
                    }
                    break;
                case NETHER:
                    radius = plugin.getConfiguration().getSpawnRadiusNether();
                    if(radius > 0){
                        addClaim(new Claim(this, new Location(world, radius, 0, radius), new Location(world, -radius, world.getMaxHeight(), -radius)));
                    }
                    break;
                case THE_END:
                    radius = plugin.getConfiguration().getSpawnRadiusEnd();
                    if(radius > 0){
                        addClaim(new Claim(this, new Location(world, radius, 0, radius), new Location(world, -radius, world.getMaxHeight(), -radius)));
                    }
                    break;
            }
        }*/
    }

    public SpawnFaction(Map<String, Object> map) {
        super(map);
    }

    public SpawnFaction(Document document){
        super(document);
    }

    @Override
    public boolean isDeathban() {
        return false;
    }

    public static UUID getUUID() {
        return FACTION_UUID;
    }

    @Override
    public String getFormattedName(CommandSender sender) {
        return JavaPlugin.getPlugin(HCFactions.class).getConfiguration().getRelationColourSafezone() + getName();
    }
}
