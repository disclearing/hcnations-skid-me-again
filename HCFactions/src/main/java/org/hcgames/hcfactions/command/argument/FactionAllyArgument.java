package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.event.playerfaction.FactionRelationCreateEvent;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.manager.SearchCallback;
import org.hcgames.hcfactions.structure.FactionRelation;
import org.hcgames.hcfactions.structure.Relation;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

import java.util.Collection;
import java.util.UUID;

/**
 * Faction argument used to request or accept ally {@link Relation} invitations from a {@link Faction}.
 */
public class FactionAllyArgument extends CommandArgument {

    private static final Relation RELATION = Relation.ALLY;

    private final HCFactions plugin;

    public FactionAllyArgument(HCFactions plugin) {
        super("ally", "Make an ally pact with other factions.", new String[]{"alliance"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <factionName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-ConsoleOnly"));
            return true;
        }

        if (plugin.getConfiguration().getFactionMaxAllies() <= 0) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Ally-AlliesDisabled"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Usage").replace("{usage}", getUsage(label)));
            return true;
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction;
        try {
            playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        } catch (NoFactionFoundException e) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Global-NotInFaction"));
            return true;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Ally-OfficerRequired"));
            return true;
        }

        plugin.getFactionManager().advancedSearch(args[1], PlayerFaction.class, new SearchCallback<PlayerFaction>() {
            @Override
            public void onSuccess(PlayerFaction faction) {
                if (playerFaction == faction) {
                    sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Ally-RequestingOwnFaction").replace("{relationName}", RELATION.getDisplayName()));
                    return;
                }

                Collection<UUID> allied = playerFaction.getAllied();

                if (allied.size() >= plugin.getConfiguration().getFactionMaxAllies()) {
                    sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Ally-OwnFactionLimitReached").replace("{allyLimit}", String.valueOf(plugin.getConfiguration().getFactionMaxAllies())));
                    return;
                }

                if (faction.getAllied().size() >= plugin.getConfiguration().getFactionMaxAllies()) {
                    sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Ally-OtherFactionLimitReached")
                            .replace("{allyLimit}", String.valueOf(plugin.getConfiguration().getFactionMaxAllies()))
                            .replace("{otherFactionName}", faction.getFormattedName(sender)));

                    return;
                }

                if (allied.contains(faction.getUniqueID())) {
                    sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Ally-RequestingOwnFaction")
                            .replace("{relationName}", RELATION.getDisplayName())
                            .replace("{otherFactionName}", faction.getFormattedName(playerFaction)));

                    return;
                }

                // Their faction has already requested us, lets' accept.
                if (faction.getRequestedRelations().remove(playerFaction.getUniqueID()) != null) {
                    FactionRelationCreateEvent event = new FactionRelationCreateEvent(playerFaction, faction, RELATION);
                    Bukkit.getPluginManager().callEvent(event);

                    faction.getRelations().put(playerFaction.getUniqueID(), new FactionRelation(RELATION, playerFaction.getUniqueID()));
                    faction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Ally-NowAllied")
                            .replace("{relationName}", RELATION.getDisplayName())
                            .replace("{otherFactionName}", playerFaction.getFormattedName(faction)));

                    playerFaction.getRelations().put(faction.getUniqueID(), new FactionRelation(RELATION, faction.getUniqueID()));
                    playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Ally-NowAllied")
                            .replace("{relationName}", RELATION.getDisplayName())
                            .replace("{otherFactionName}", faction.getFormattedName(playerFaction)));

                    return;
                }

                if (playerFaction.getRequestedRelations().putIfAbsent(faction.getUniqueID(), RELATION) != null) {
                    sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Ally-AlreadyRequested")
                            .replace("{relationName}", RELATION.getDisplayName())
                            .replace("{otherFactionName}", faction.getFormattedName(playerFaction)));

                    return;
                }

                // Handle the request.

                playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Ally-RequestSent")
                        .replace("{relationName}", RELATION.getDisplayName())
                        .replace("{otherFactionName}", faction.getFormattedName(playerFaction)));

                faction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Ally-RequestReceived")
                        .replace("{relationName}", RELATION.getDisplayName())
                        .replace("{otherFactionName}", playerFaction.getFormattedName(faction)));
            }

            @Override
            public void onFail(FailReason reason) {
                sender.sendMessage(plugin.getMessages().getString("commands.error.faction_not_found", args[1]));
            }
        });

        return true;
    }

    /*@Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction;
        try {
            playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        } catch (NoFactionFoundException e) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.equals(player) && player.canSee(target) && !results.contains(target.getName())) {
                Faction targetFaction;
                try{
                    Faction targetFaction = plugin.getFactionManager().getPlayerFaction(target);
                }catch(NoFactionFoundException e){}

                if (targetFaction != null && playerFaction != targetFaction) {
                    if (playerFaction.getRequestedRelations().get(targetFaction.getUniqueID()) != RELATION && playerFaction.getRelations().get(targetFaction.getUniqueID()) != RELATION) {
                        results.add(targetFaction.getName());
                    }
                }
            }
        }

        return results;
    }*/
}
