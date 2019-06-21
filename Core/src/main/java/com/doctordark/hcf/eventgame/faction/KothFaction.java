package com.doctordark.hcf.eventgame.faction;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.CaptureZone;
import com.doctordark.hcf.eventgame.EventType;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.util.Names;
import technology.brk.util.BukkitUtils;
import technology.brk.util.mongo.Mongoable;

import java.util.List;
import java.util.Map;

/**
 * Represents a 'King of the Hill' faction.
 */
public class KothFaction extends CapturableFaction implements ConfigurationSerializable, Mongoable{

    private CaptureZone captureZone;

    public KothFaction(String name){
        super(name);
        if(name.equalsIgnoreCase("palace")){
            dtrLossMultiplier = 0.75;
        }
    }

    public KothFaction(Map<String, Object> map){
        super(map);
        this.captureZone = (CaptureZone) map.get("captureZone");
        if(getName().equalsIgnoreCase("palace")){
            dtrLossMultiplier = 0.75;
        }
    }

    public KothFaction(Document document){
        super(document);

        if(document.containsKey("captureZone")){
            captureZone = new CaptureZone(document.get("captureZone", Document.class));
        }
    }

    @Override
    public Map<String, Object> serialize(){
        Map<String, Object> map = super.serialize();
        map.put("captureZone", captureZone);
        return map;
    }

    public Document toDocument(){
        Document document = super.toDocument();
        if(captureZone != null){
            document.put("captureZone", captureZone.toDocument());
        }
        return document;
    }

    @Override
    public List<CaptureZone> getCaptureZones(){
        return captureZone == null ? ImmutableList.of() : ImmutableList.of(captureZone);
    }

    @Override
    public EventType getEventType(){
        return EventType.KOTH;
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

        if(captureZone != null){
            long remainingCaptureMillis = captureZone.getRemainingCaptureMillis();
            long defaultCaptureMillis = captureZone.getDefaultCaptureMillis();
            if(remainingCaptureMillis > 0L && remainingCaptureMillis != defaultCaptureMillis){
                sender.sendMessage(ChatColor.YELLOW + "  Remaining Time: " + ChatColor.RED + DurationFormatUtils.formatDurationWords(remainingCaptureMillis, true, true));
            }

            sender.sendMessage(ChatColor.YELLOW + "  Capture Delay: " + ChatColor.RED + captureZone.getDefaultCaptureWords());
            if(captureZone.getCappingPlayer() != null && sender.hasPermission("hcf.koth.checkcapper")){
                Player capping = captureZone.getCappingPlayer();
                PlayerFaction playerFaction = null;
                try{
                    playerFaction = HCF.getPlugin().getFactions().getFactionManager().getPlayerFaction(capping);
                }catch(NoFactionFoundException e){
                }
                String factionTag = "[" + (playerFaction == null ? "*" : playerFaction.getName()) + "]";
                sender.sendMessage(ChatColor.YELLOW + "  Current Capper: " + ChatColor.RED + capping.getName() + ChatColor.GOLD + factionTag);
            }
        }

        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    /**
     * Gets the {@link CaptureZone} of this {@link KothFaction}.
     *
     * @return the {@link CaptureZone} of this {@link KothFaction}
     */
    public CaptureZone getCaptureZone(){
        return captureZone;
    }

    /**
     * Sets the {@link CaptureZone} for this {@link KothFaction}.
     *
     * @param captureZone the {@link CaptureZone} to set
     */
    public void setCaptureZone(CaptureZone captureZone){
        this.captureZone = captureZone;
    }
}
