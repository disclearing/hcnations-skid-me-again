package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.event.playerfaction.FactionRelationRemoveEvent;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.manager.SearchCallback;
import org.hcgames.hcfactions.structure.Relation;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

import java.util.Collection;
import java.util.Collections;

public class FactionUnallyArgument extends CommandArgument {

    private Relation relation = Relation.ALLY;
    private final HCFactions plugin;

    public FactionUnallyArgument(HCFactions plugin) {
        super("unally", "Remove an ally pact with other factions.");
        this.plugin = plugin;
        this.aliases = new String[]{"unalliance", "neutral"};
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <all|factionName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-ConsoleOnly"));
            return true;
        }

        if (plugin.getConfiguration().getFactionMaxAllies() <= 0) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Unally-DisabledOnMap"));
            //sender.sendMessage(ChatColor.RED + "Allies are disabled this map.");
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
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Unally-OfficerRequired"));
            //sender.sendMessage(ChatColor.RED + "You must be a faction officer to edit relations.");
            return true;
        }

        if (args[1].equalsIgnoreCase("all")) {
            Collection<PlayerFaction> allies = playerFaction.getAlliedFactions();
            if (allies.isEmpty()) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Unally-NoAllies"));
                //sender.sendMessage(ChatColor.RED + "Your faction has no allies.");
                return true;
            }

            handle(sender, playerFaction, allies);
        } else {
            plugin.getFactionManager().advancedSearch(args[1], PlayerFaction.class, new SearchCallback<PlayerFaction>() {
                @Override
                public void onSuccess(PlayerFaction faction) {
                    handle(sender, playerFaction, Collections.singleton(faction));
                }

                @Override
                public void onFail(FailReason reason) {
                    sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Global-UnknownFaction").replace("{factionName}", args[1]));
                }
            });
        }

        return true;
    }

    private void handle(CommandSender sender, PlayerFaction faction, Collection<PlayerFaction> targets){
        for (PlayerFaction targetFaction : targets) {
            if (faction.getRelations().remove(targetFaction.getUniqueID()) == null || targetFaction.getRelations().remove(faction.getUniqueID()) == null) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Unally-NotAllied")
                        .replace("{allyDisplayName}", relation.getDisplayName())
                        .replace("{otherFactionName}", targetFaction.getFormattedName(faction)));
                //sender.sendMessage(ChatColor.RED + "Your faction is not " + relation.getDisplayName() + ChatColor.RED + " with " + targetFaction.getDisplayName(playerFaction) + ChatColor.RED + '.');
                return;
            }

            FactionRelationRemoveEvent event = new FactionRelationRemoveEvent(faction, targetFaction, Relation.ALLY);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Unally-CouldNotDrop")
                        .replace("{allyDisplayName}", relation.getDisplayName())
                        .replace("{otherFactionName}", targetFaction.getFormattedName(faction)));
                //sender.sendMessage(ChatColor.RED + "Could not drop " + relation.getDisplayName() + " with " + targetFaction.getDisplayName(playerFaction) + ChatColor.RED + ".");
                return;
            }

            // Inform the affected factions.
            faction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Unally-PlayerFactionBroadcast")
                    .replace("{allyDisplayName}", relation.getDisplayName())
                    .replace("{otherFactionName}", targetFaction.getFormattedName(faction)));
            //playerFaction.broadcast(ChatColor.YELLOW + "Your faction has dropped its " + relation.getDisplayName() + ChatColor.YELLOW + " with " +
            //        targetFaction.getDisplayName(playerFaction) + ChatColor.YELLOW + '.');

            targetFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Unally-OtherFactionBroadcast")
                    .replace("{allyDisplayName}", relation.getDisplayName())
                    .replace("{factionName}", faction.getFormattedName(targetFaction)));
            //targetFaction.broadcast(ChatColor.YELLOW + playerFaction.getDisplayName(targetFaction) + ChatColor.YELLOW + " has dropped their " + relation.getDisplayName() +
            //        ChatColor.YELLOW + " with your faction.");
        }
    }

    /*@Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            return Collections.emptyList();
        }

        return Lists.newArrayList(Iterables.concat(COMPLETIONS, playerFaction.getAlliedFactions().stream().map(Faction::getName).collect(Collectors.toList())));
    }

    private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("all");*/
}
