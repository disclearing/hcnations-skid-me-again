package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.ClaimableFaction;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.manager.SearchCallback;
import technology.brk.util.command.CommandArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Faction argument used to check {@link Claim}s made by {@link Faction}s.
 */
//TODO: Rewrite
public class FactionClaimsArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionClaimsArgument(HCFactions plugin) {
        super("claims", "View all claims for a faction.");
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " [factionName]";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PlayerFaction selfFaction = null;
        try{
            selfFaction = sender instanceof Player ? plugin.getFactionManager().getPlayerFaction((Player) sender) : null;
        }catch(NoFactionFoundException ignored){}
        ClaimableFaction targetFaction;

        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Usage").replace("{usage}", getUsage(label)));
                return true;
            }

            if (selfFaction == null) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Global-NotInFaction"));
                return true;
            }

            targetFaction = selfFaction;
        } else {
            PlayerFaction finalSelfFaction = selfFaction;
            plugin.getFactionManager().advancedSearch(args[1], ClaimableFaction.class, new SearchCallback<ClaimableFaction>() {
                @Override
                public void onSuccess(ClaimableFaction faction) {
                    handle(sender, faction, finalSelfFaction);
                }

                @Override
                public void onFail(FailReason reason) {
                    sender.sendMessage(plugin.getMessages().getString("commands.error.faction_not_found", args[1]));
                }
            });
            return true;
        }

        handle(sender, targetFaction, selfFaction);
        return true;
    }

    private void handle(CommandSender sender, ClaimableFaction targetFaction, PlayerFaction selfFaction){
        Collection<Claim> claims = targetFaction.getClaims();

        if (claims.isEmpty()) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Claims-FactionClaimedNothing")
                    .replace("{factionName}", targetFaction.getFormattedName(sender)));
            return;
        }

        if (sender instanceof Player && !sender.isOp() && (targetFaction instanceof PlayerFaction && ((PlayerFaction) targetFaction).getHome() == null)) {
            if (selfFaction != targetFaction) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Claims-CannotViewNoHome")
                        .replace("{factionName}", targetFaction.getFormattedName(sender)));
                return;
            }
        }

        sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Claims-ClaimListHeader")
                .replace("{factionName}", targetFaction.getFormattedName(sender))
                .replace("{claimsAmount}", String.valueOf(claims.size())));

        for (Claim claim : claims) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Claims-ClaimListItem")
                    .replace("{claimName}", claim.getFormattedName()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        } else if (args[1].isEmpty()) {
            return null;
        } else {
            Player player = ((Player) sender);
            List<String> results = new ArrayList<>(plugin.getFactionManager().getFactionNameMap().keySet());
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (player.canSee(target) && !results.contains(target.getName())) {
                    results.add(target.getName());
                }
            }

            return results;
        }
    }
}
