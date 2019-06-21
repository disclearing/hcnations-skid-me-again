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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.faction.ClaimableFaction;
import technology.brk.util.BukkitUtils;
import technology.brk.util.mongo.Mongoable;

import java.util.Map;
import java.util.UUID;

public abstract class RoadFaction extends ClaimableFaction implements ConfigurationSerializable, Mongoable, SystemFaction {

    // The difference the roads will end from the border.
    //public static final int ROAD_EDGE_DIFF = 0;

    // The minimum and maximum heights for roads.
    //public static final int ROAD_MIN_HEIGHT = 0; //50 'this allowed people to claim below the roads, temp disabled;
    //public static final int ROAD_MAX_HEIGHT = 256; //80 'this allowed people to claim above the roads, temp disabled;

    public RoadFaction(String name, UUID uuid) {
        super(name, uuid);
    }

    public static class NorthRoadFaction extends RoadFaction implements ConfigurationSerializable, Mongoable, SystemFaction {

        private final static UUID FACTION_UUID = UUID.fromString("ab32b857-1f3a-4742-b0e0-b193a992813a");

        public NorthRoadFaction() {
            super("NorthRoad", FACTION_UUID);
            displayName = "North Road";

            /*int roadLength = plugin.getConfiguration().getRoadLength();
            for (World world : plugin.getServer().getWorlds()) {
                World.Environment environment = world.getEnvironment();
                if (environment != World.Environment.THE_END) {

                    int offset = 0;
                    switch (environment){
                        case NORMAL:
                            offset = plugin.getConfiguration().getSpawnRadiusOverworld();
                            break;
                        case NETHER:
                            offset = plugin.getConfiguration().getSpawnRadiusNether();
                            break;
                        default:
                            break;
                    }
                    offset = offset + 1;

                    addClaim(new Claim(this,
                            new Location(world, -plugin.getConfiguration().getRoadWidthLeft(), ROAD_MIN_HEIGHT, -offset),
                            new Location(world, plugin.getConfiguration().getRoadWidthRight(), ROAD_MAX_HEIGHT, -roadLength + ROAD_EDGE_DIFF)));
                }
            }*/
        }


        public NorthRoadFaction(Map<String, Object> map) {
            super(map);
        }

        public NorthRoadFaction(Document object){
            super(object);
        }

        public static UUID getUUID() {
            return FACTION_UUID;
        }
    }

    public static class EastRoadFaction extends RoadFaction implements ConfigurationSerializable, Mongoable, SystemFaction{

        private final static UUID FACTION_UUID = UUID.fromString("401dc34a-0111-4f25-9c4c-526d77650e21");

        public EastRoadFaction() {
            super("EastRoad", FACTION_UUID);
            displayName = "East Road";

            /*int roadLength = plugin.getConfiguration().getRoadLength();
            for (World world : plugin.getServer().getWorlds()) {
                World.Environment environment = world.getEnvironment();
                if (environment != World.Environment.THE_END) {

                    int offset = 0;
                    switch (environment){
                        case NORMAL:
                            offset = plugin.getConfiguration().getSpawnRadiusOverworld();
                            break;
                        case NETHER:
                            offset = plugin.getConfiguration().getSpawnRadiusNether();
                            break;
                        default:
                            break;
                    }
                    offset = offset + 1;

                    addClaim(new Claim(this,
                            new Location(world, -plugin.getConfiguration().getRoadWidthLeft(), ROAD_MIN_HEIGHT, -offset),
                            new Location(world, plugin.getConfiguration().getRoadWidthRight(), ROAD_MAX_HEIGHT, -roadLength + ROAD_EDGE_DIFF)));
                }
            }*/
        }

        public EastRoadFaction(Map<String, Object> map) {
            super(map);
        }

        public EastRoadFaction(Document object){
            super(object);
        }

        public static UUID getUUID() {
            return FACTION_UUID;
        }
    }

    public static class SouthRoadFaction extends RoadFaction implements ConfigurationSerializable, Mongoable, SystemFaction{

        private final static UUID FACTION_UUID = UUID.fromString("42e61ded-4f50-46c7-82a8-723bdcda4991");

        public SouthRoadFaction() {
            super("SouthRoad", FACTION_UUID);
            displayName = "South Road";

            /*int roadLength = plugin.getConfiguration().getRoadLength();
            for (World world : plugin.getServer().getWorlds()) {
                World.Environment environment = world.getEnvironment();
                if (environment != World.Environment.THE_END) {

                    int offset = 0;
                    switch (environment){
                        case NORMAL:
                            offset = plugin.getConfiguration().getSpawnRadiusOverworld();
                            break;
                        case NETHER:
                            offset = plugin.getConfiguration().getSpawnRadiusNether();
                            break;
                        default:
                            break;
                    }
                    offset = offset + 1;

                    addClaim(new Claim(this,
                            new Location(world, -plugin.getConfiguration().getRoadWidthLeft(), ROAD_MIN_HEIGHT, -offset),
                            new Location(world, plugin.getConfiguration().getRoadWidthRight(), ROAD_MAX_HEIGHT, -roadLength + ROAD_EDGE_DIFF)));
                }
            }*/
        }

        public SouthRoadFaction(Map<String, Object> map) {
            super(map);
        }

        public SouthRoadFaction(Document object){
            super(object);
        }

        public static UUID getUUID() {
            return FACTION_UUID;
        }
    }

    public static class WestRoadFaction extends RoadFaction implements ConfigurationSerializable, Mongoable {

        private final static UUID FACTION_UUID = UUID.fromString("ea50290c-8225-4222-8664-32f2f5070974");

        public WestRoadFaction() {
            super("WestRoad", FACTION_UUID);
            displayName = "West Road";

            /*int roadLength = plugin.getConfiguration().getRoadLength();
            for (World world : plugin.getServer().getWorlds()) {
                World.Environment environment = world.getEnvironment();
                if (environment != World.Environment.THE_END) {

                    int offset = 0;
                    switch (environment){
                        case NORMAL:
                            offset = plugin.getConfiguration().getSpawnRadiusOverworld();
                            break;
                        case NETHER:
                            offset = plugin.getConfiguration().getSpawnRadiusNether();
                            break;
                        default:
                            break;
                    }
                    offset = offset + 1;

                    addClaim(new Claim(this,
                            new Location(world, -plugin.getConfiguration().getRoadWidthLeft(), ROAD_MIN_HEIGHT, -offset),
                            new Location(world, plugin.getConfiguration().getRoadWidthRight(), ROAD_MAX_HEIGHT, -roadLength + ROAD_EDGE_DIFF)));
                }
            }*/
        }

        public WestRoadFaction(Map<String, Object> map) {
            super(map);
        }

        public WestRoadFaction(Document object){
            super(object);
        }

        public static UUID getUUID() {
            return FACTION_UUID;
        }
    }

    public RoadFaction(Map<String, Object> map) {
        super(map);
    }

    public RoadFaction(Document object){
        super(object);
    }

    @Override
    public String getFormattedName(CommandSender sender) {
        return JavaPlugin.getPlugin(HCFactions.class).getConfiguration().getRelationColourRoad() + getDisplayName();
    }

    @Override
    public void sendInformation(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(' ' + getFormattedName(sender));
        sender.sendMessage(ChatColor.YELLOW + "  Location: " + ChatColor.RED + "None");
        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }
}
