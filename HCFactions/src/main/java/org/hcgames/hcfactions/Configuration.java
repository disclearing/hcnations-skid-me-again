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

package org.hcgames.hcfactions;

import lombok.AccessLevel;
import lombok.Getter;
import net.techcable.techutils.collect.CaseInsensitveStringSet;
import net.techcable.techutils.config.AnnotationConfig;
import net.techcable.techutils.config.Setting;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
public class Configuration extends AnnotationConfig{

    @Setting("factions.nameMinCharacters")
    private int factionNameMinCharacters = 3;

    @Setting("factions.nameMaxCharacters")
    private int factionNameMaxCharacters = 16;

    @Setting("factions.maxMembers")
    private int factionMaxMembers = 25;

    @Setting("factions.maxClaims")
    private int factionMaxClaims = 8;

    @Setting("factions.maxAllies")
    private int factionMaxAllies = 1;

    @Setting("factions.dtr.regenFreeze.baseMinutes")
    private int factionDtrRegenFreezeBaseMinutes = 40;
    private long factionDtrRegenFreezeBaseMilliseconds;

    @Getter(AccessLevel.NONE)
    @Setting("factions.dtr.regenFreeze.minutesPerMember")
    private int factionDtrRegenFreezeMinutesPerMember = 2;
    private long factionDtrRegenFreezeMillisecondsPerMember;

    @Setting("factions.dtr.minimum")
    private int factionMinimumDtr = -50;

    @Setting("factions.dtr.maximum")
    private float factionMaximumDtr = 6.0F;

    @Setting("factions.dtr.millisecondsBetweenUpdates")
    private int factionDtrUpdateMillis = 45000; // 45 seconds
    private String factionDtrUpdateTimeWords;

    @Setting("factions.dtr.incrementBetweenUpdates")
    private float factionDtrUpdateIncrement = 0.1F;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.warzone")
    private String relationColourWarzoneName = "LIGHT_PURPLE";
    private ChatColor relationColourWarzone = ChatColor.LIGHT_PURPLE;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.wilderness")
    private String relationColourWildernessName = "DARK_GREEN";
    private ChatColor relationColourWilderness = ChatColor.DARK_GREEN;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.teammate")
    private String relationColourTeammateName = "GREEN";
    private ChatColor relationColourTeammate = ChatColor.GREEN;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.ally")
    private String relationColourAllyName = "GOLD";
    private ChatColor relationColourAlly = ChatColor.GOLD;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.enemy")
    private String relationColourEnemyName = "RED";
    private ChatColor relationColourEnemy = ChatColor.RED;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.road")
    private String relationColourRoadName = "YELLOW";
    private ChatColor relationColourRoad = ChatColor.YELLOW;

    @Getter(AccessLevel.NONE)
    @Setting("factions.relationColours.safezone")
    private String relationColourSafezoneName = "AQUA";
    private ChatColor relationColourSafezone = ChatColor.AQUA;

    @Getter(AccessLevel.NONE)
    @Setting("factions.antirotation.delay")
    private int antiRotationDelayHours = 6;
    private long antiRotationDelayMillis = 0;

    @Setting("factions.antirotation.enabled")
    private boolean antiRotationEnabled = false;

    @Setting("factions.endportal.enabled")
    private boolean factionEndPortalEnabled = true;

    @Setting("factions.endportal.radius")
    private int endPortalRadius = 20;

    @Setting("factions.endportal.center")
    private int endPortalCenter = 1000;

    @Setting("factions.spawn.radiusOverworld")
    private int spawnRadiusOverworld = 800;

    @Setting("factions.spawn.radiusNether")
    private int spawnRadiusNether = 800;

    @Setting("factions.spawn.radiusEnd")
    private int spawnRadiusEnd = 800;

    @Setting("factions.roads.widthLeft")
    private int roadWidthLeft = 7;

    @Setting("factions.roads.widthRight")
    private int roadWidthRight = 7;

    @Setting("factions.roads.length")
    private int roadLength = 4000;

    @Setting("factions.warzone.radiusOverworld")
    private int warzoneRadiusOverworld = 800;

    @Setting("factions.warzone.radiusNether")
    private int warzoneRadiusNether = 800;

    @Setting("factions.roads.allowClaimsBesides")
    private boolean allowClaimsBesidesRoads = true;

    @Setting("disallowedFactionNames")
    private List<String> factionDisallowedNamesList = new ArrayList<>();
    private CaseInsensitveStringSet factionDisallowedNames = new CaseInsensitveStringSet();

    @Setting("factions.home.teleportDelay.NORMAL")
    private int factionHomeTeleportDelayOverworldSeconds;
    private long factionHomeTeleportDelayOverworldMillis;

    @Setting("factions.home.teleportDelay.NETHER")
    private int factionHomeTeleportDelayNetherSeconds;
    private long factionHomeTeleportDelayNetherMillis;

    @Setting("factions.home.teleportDelay.THE_END")
    private int factionHomeTeleportDelayEndSeconds;
    private long factionHomeTeleportDelayEndMillis;

    @Setting("factions.home.allowTeleportingInEnemyTerritory")
    private boolean allowTeleportingInEnemyTerritory = true;

    @Setting("factions.home.maxHeight")
    private int maxHeightFactionHome = -1;

    @Setting("subclaimSigns.private")
    private boolean subclaimSignPrivate = false;

    @Setting("subclaimSigns.captain")
    private boolean subclaimSignCaptain = false;

    @Setting("subclaimSigns.leader")
    private boolean subclaimSignLeader = false;

    @Setting("subclaimSigns.hopperCheck")
    private boolean subclaimHopperCheck = false;

    @Setting("preventAllyDamage")
    private boolean preventAllyAttackDamage = true;

    @Setting("messageDebug")
    private boolean messageDebug = false;

    @Setting("data.mongo.address")
    private String mongoAddress = "127.0.0.1";

    @Setting("data.mongo.port")
    private int mongoPort = 27017;

    @Setting("data.mongo.username")
    private String mongoUsername = "mongo";

    @Setting("data.mongo.password")
    private String mongoPassword = "secure";

    @Setting("data.mongo.database")
    private String mongoDatabase = "hardcore";

    @Setting("data.useMongo")
    private boolean useMongo = false;

    void load(HCFactions plugin){
        relationColourWarzone = ChatColor.valueOf(relationColourWarzoneName.replace(" ", "_").toUpperCase());
        relationColourWilderness = ChatColor.valueOf(relationColourWildernessName.replace(" ", "_").toUpperCase());
        relationColourTeammate = ChatColor.valueOf(relationColourTeammateName.replace(" ", "_").toUpperCase());
        relationColourAlly = ChatColor.valueOf(relationColourAllyName.replace(" ", "_").toUpperCase());
        relationColourEnemy = ChatColor.valueOf(relationColourEnemyName.replace(" ", "_").toUpperCase());
        relationColourRoad = ChatColor.valueOf(relationColourRoadName.replace(" ", "_").toUpperCase());
        relationColourSafezone = ChatColor.valueOf(relationColourSafezoneName.replace(" ", "_").toUpperCase());
        antiRotationDelayMillis = TimeUnit.HOURS.toMillis(antiRotationDelayHours);
        factionHomeTeleportDelayOverworldMillis = TimeUnit.SECONDS.toMillis(factionHomeTeleportDelayOverworldSeconds);
        factionHomeTeleportDelayNetherMillis = TimeUnit.SECONDS.toMillis(factionHomeTeleportDelayNetherSeconds);
        factionHomeTeleportDelayEndMillis = TimeUnit.SECONDS.toMillis(factionHomeTeleportDelayEndSeconds);
        factionDisallowedNames.addAll(factionDisallowedNamesList);
    }

}
