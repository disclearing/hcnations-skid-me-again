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

package com.doctordark.hcf.eventgame.faction;

import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.claim.ClaimHandler;
import org.hcgames.hcfactions.util.Names;
import technology.brk.util.BukkitUtils;
import technology.brk.util.GenericUtils;
import technology.brk.util.cuboid.Cuboid;
import technology.brk.util.mongo.Mongoable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FuryFaction extends CapturableFaction implements ConfigurationSerializable, Mongoable{
    //Totally didn't steal this from ConquestFaction!

    private final EnumMap<FuryZone, CaptureZone> captureZones = new EnumMap<>(FuryZone.class);
    private final Map<FuryZone, String> captureClaimAreas = new HashMap<>();

    public FuryFaction(String name){
        super(name);
        dtrLossMultiplier = 0.75;
    }

    public FuryFaction(Map<String, Object> map){
        super(map);
        dtrLossMultiplier = 0.75;

        Object object;
        if((object = map.get("overworld")) instanceof CaptureZone){
            captureZones.put(FuryZone.OVERWORLD, (CaptureZone) object);
        }

        if((object = map.get("nether")) instanceof CaptureZone){
            captureZones.put(FuryZone.NETHER, (CaptureZone) object);
        }

        if((object = map.get("end")) instanceof CaptureZone){
            captureZones.put(FuryZone.END, (CaptureZone) object);
        }

        for(Map.Entry<String, String> captureClaimArea : GenericUtils.castMap(map.get("captureClaimAreas"), String.class, String.class).entrySet()){
            captureClaimAreas.put(FuryZone.getByName(captureClaimArea.getKey()), captureClaimArea.getValue());
        }
    }

    public FuryFaction(Document document){
        super(document);
        dtrLossMultiplier = 0.75;

        if(document.containsKey("captureZones")){
            Document captureZones = (Document) document.get("captureZones");
            for(Map.Entry<String, Object> entry : captureZones.entrySet()){
                this.captureZones.put(FuryZone.getByName(entry.getKey()), new CaptureZone((Document) entry.getValue()));
            }
        }

        if(document.containsKey("captureClaimAreas")){
            Document captureClaimAreas = (Document) document.get("captureClaimAreas");
            for(Map.Entry<String, Object> entry : captureClaimAreas.entrySet()){
                this.captureClaimAreas.put(FuryZone.getByName(entry.getKey()), (String) entry.getValue());
            }
        }
    }

    @Override
    public Map<String, Object> serialize(){
        Map<String, Object> map = super.serialize();
        for(Map.Entry<FuryZone, CaptureZone> entry : captureZones.entrySet()){
            map.put(entry.getKey().name().toLowerCase(), entry.getValue());
        }

        Map<String, String> captureClaimAreas = new HashMap<>();
        for(Map.Entry<FuryZone, String> entry : this.captureClaimAreas.entrySet()){
            captureClaimAreas.put(entry.getKey().getName(), entry.getValue());
        }
        map.put("captureClaimAreas", captureClaimAreas);
        return map;
    }

    @Override
    public Document toDocument(){
        Document document = super.toDocument();

        Document captureZones = new Document();
        for(Map.Entry<FuryZone, CaptureZone> entry : this.captureZones.entrySet()){
            captureZones.put(entry.getKey().name().toLowerCase(), entry.getValue().toDocument());
        }
        document.put("captureZones", captureZones);

        Document captureClaimAreas = new Document();
        for(Map.Entry<FuryZone, String> entry : this.captureClaimAreas.entrySet()){
            captureClaimAreas.put(entry.getKey().getName(), entry.getValue());
        }
        document.put("captureClaimAreas", captureClaimAreas);

        return document;
    }

    @Override
    public EventType getEventType(){
        return EventType.FURY;
    }

    @Override
    public void sendInformation(CommandSender sender){
        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(getFormattedName(sender));

        for(Claim claim : getClaims()){
            Location location = claim.getCenter();
            sender.sendMessage(ChatColor.YELLOW + "  Location: " + ChatColor.RED +
                    '(' + Names.getEnvironmentName(location.getWorld().getEnvironment()) + ", " + location.getBlockX() + " | " + location.getBlockZ() + ')');
        }

        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    public void setZone(FuryZone furyZone, CaptureZone captureZone){
        switch(furyZone){
            case OVERWORLD:
                captureZones.put(FuryZone.OVERWORLD, captureZone);
                break;
            case NETHER:
                captureZones.put(FuryZone.NETHER, captureZone);
                break;
            case END:
                captureZones.put(FuryZone.END, captureZone);
                break;
            default:
                throw new AssertionError("Unsupported operation");
        }
    }

    public CaptureZone getOverworld(){
        return captureZones.get(FuryZone.OVERWORLD);
    }

    public CaptureZone getNether(){
        return captureZones.get(FuryZone.NETHER);
    }

    public CaptureZone getEnd(){
        return captureZones.get(FuryZone.END);
    }

    public Collection<FuryZone> getConquestZones(){
        return ImmutableSet.copyOf(captureZones.keySet());
    }

    @Override
    public List<CaptureZone> getCaptureZones(){
        return ImmutableList.copyOf(captureZones.values());
    }

    public void setClaim(Cuboid cuboid, CommandSender sender, FuryZone zone){
        if(captureClaimAreas.containsKey(zone)){
            String name = captureClaimAreas.get(zone);
            for(Claim claim : getClaims()){
                if(claim.getName().equals(name)){
                    removeClaim(claim, sender);
                    break;
                }
            }
        }

        Location min = cuboid.getMinimumPoint();
        min.setY(ClaimHandler.MIN_CLAIM_HEIGHT);

        Location max = cuboid.getMaximumPoint();
        max.setY(ClaimHandler.MAX_CLAIM_HEIGHT);

        Claim claim = new Claim(this, min, max);
        captureClaimAreas.put(zone, claim.getName());

        addClaim(claim, sender);
    }

    public enum FuryZone{

        OVERWORLD(World.Environment.NORMAL, ChatColor.BLUE, "Overworld"),
        NETHER(World.Environment.NETHER, ChatColor.RED, "Nether"),
        END(World.Environment.THE_END, ChatColor.GRAY, "End");

        private static final Map<String, FuryZone> BY_NAME;

        static{
            ImmutableMap.Builder<String, FuryZone> builder = ImmutableMap.builder();
            for(FuryZone zone : values()){
                builder.put(zone.name().toUpperCase(), zone);
            }

            BY_NAME = builder.build();
        }

        private final String name;
        private final ChatColor color;
        private final World.Environment environment;

        FuryZone(World.Environment environment, ChatColor color, String name){
            this.environment = environment;
            this.color = color;
            this.name = name;
        }

        public static FuryZone getByName(String name){
            return BY_NAME.get(name.toUpperCase());
        }

        public static Collection<String> getNames(){
            return new ArrayList<>(BY_NAME.keySet());
        }

        public ChatColor getColor(){
            return color;
        }

        public String getName(){
            return name;
        }

        public String getDisplayName(){
            return color.toString() + name;
        }

        public World.Environment getEnvironment(){
            return environment;
        }
    }
}
