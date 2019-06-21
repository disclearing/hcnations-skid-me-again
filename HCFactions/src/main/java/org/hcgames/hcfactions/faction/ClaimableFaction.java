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
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.event.claim.ClaimChangeEvent;
import org.hcgames.hcfactions.event.claim.FactionClaimChangeEvent;
import org.hcgames.hcfactions.event.claim.FactionClaimChangedEvent;
import org.hcgames.hcfactions.structure.FactionMember;
import org.hcgames.hcfactions.util.Names;
import technology.brk.util.BukkitUtils;
import technology.brk.util.GenericUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClaimableFaction extends Faction{

    private final Collection<Claim> claims = new ArrayList<>();

    @Getter @Setter
    private boolean snowfall;

    public ClaimableFaction(String name, UUID uuid){
        super(name, uuid);
    }

    public ClaimableFaction(String name){
        super(name);
    }

    public ClaimableFaction(Map<String, Object> map) {
        super(map);
        claims.addAll(GenericUtils.createList(map.get("claims"), Claim.class));
        snowfall = (boolean) map.getOrDefault("snowfall", false);
    }

    public ClaimableFaction(Document document) {
        super(document);
        claims.addAll(GenericUtils.createList(document.get("claims"), Document.class).stream().map(Claim::new).collect(Collectors.toList()));
        snowfall = document.getBoolean("snowfall", false);
    }

    @Override
    public Map<String, Object> serialize(){
        Map<String, Object> map = super.serialize();
        map.put("claims", claims);
        map.put("snowfall", snowfall);
        return map;
    }

    @Override
    public Document toDocument(){
        Document document = super.toDocument();
        document.put("claims", claims.stream().map(Claim::toDocument).collect(Collectors.toList()));
        document.put("snowfall", snowfall);
        return document;
    }

    @Override
    public void sendInformation(CommandSender sender) {
        HCFactions plugin = JavaPlugin.getPlugin(HCFactions.class);

        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(plugin.getMessages().getString("factions.show.claimable.top").replace("{factionName}", getFormattedName(sender)));
        for (Claim claim : claims) {
            Location location = claim.getCenter();
            sender.sendMessage(plugin.getMessages().getString("factions.show.claimable.claim_format")
                    .replace("{environment}", Names.getEnvironmentName(location.getWorld().getEnvironment()))
                    .replace("{locX}", String.valueOf(location.getBlockX()))
                    .replace("{locY}", String.valueOf(location.getBlockZ())));
        }
        sender.sendMessage(ChatColor.GOLD + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    public List<Claim> getClaims(){
        return ImmutableList.copyOf(claims);
    }

    public List<Claim> getClaims(World world){
        return ImmutableList.copyOf(claims.stream().filter(claim -> world.equals(claim.getWorld())).collect(Collectors.toList()));
    }

    public boolean addClaim(Claim claim){
        return addClaim(claim, Bukkit.getServer().getConsoleSender());
    }

    public boolean addClaim(Claim claim, CommandSender sender) {
        return addClaims(Collections.singleton(claim), sender);
    }

    public boolean addClaims(Collection<Claim> adding, CommandSender sender) {
        FactionClaimChangeEvent event = new FactionClaimChangeEvent(sender, this, adding, ClaimChangeEvent.ClaimChangeReason.CLAIM);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled() || !claims.addAll(adding)) {
            return false;
        }

        Bukkit.getServer().getPluginManager().callEvent(new FactionClaimChangedEvent(sender, this, adding, ClaimChangeEvent.ClaimChangeReason.CLAIM));
        return true;
    }

    public boolean removeClaim(Claim claim){
        return removeClaim(claim, Bukkit.getServer().getConsoleSender());
    }

    public boolean removeClaim(Claim claim, CommandSender sender) {
        return removeClaims(Collections.singleton(claim), sender);
    }


    public boolean removeClaims(Collection<Claim> toRemove, CommandSender sender) {
        if (sender == null) {
            sender = Bukkit.getConsoleSender();
        }

        if(toRemove.isEmpty() || claims.isEmpty()){
            return false;
        }

        int expected = this.claims.size() - toRemove.size();

        FactionClaimChangeEvent event = new FactionClaimChangeEvent(sender, this, new ArrayList<>(claims), ClaimChangeEvent.ClaimChangeReason.UNCLAIM);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || !this.claims.removeAll(toRemove)) { // we clone the collection so we can show what we removed to the event.
            return false;
        }

        if (expected != this.claims.size()) {
            return false;
        }

        if (this instanceof PlayerFaction) {
            HCFactions plugin = JavaPlugin.getPlugin(HCFactions.class);
            PlayerFaction playerFaction = (PlayerFaction) this;

            Optional<Location> home = playerFaction.getHome();
            Optional<FactionMember> leader = playerFaction.getLeader();
            UUID leaderUUID;

            if(!leader.isPresent()){
                throw new RuntimeException("Leader is not present for faction " + playerFaction);
            }else{
                leaderUUID = leader.get().getUniqueId();
            }

            int refund = 0;
            for (Claim claim : toRemove) {
                refund += plugin.getClaimHandler().calculatePrice(claim, expected, true);
                if (expected > 0) expected--;

                if (home != null && (home.isPresent() && claim.contains(home.get()))){
                    playerFaction.setHome(null);
                    playerFaction.broadcast(plugin.getMessages().getString("factions.claiming.home_unset"));
                    break;
                }
            }

            HCF.getPlugin().getEconomyManager().addBalance(leaderUUID, refund);
            playerFaction.broadcast(plugin.getMessages().getString("factions.claiming.refund_broadcast")
                    .replace("{amount}", String.valueOf(refund)));
        }

        Bukkit.getPluginManager().callEvent(new FactionClaimChangedEvent(sender, this, toRemove, ClaimChangeEvent.ClaimChangeReason.UNCLAIM));
        return true;
    }
}
