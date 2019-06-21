package org.hcgames.hcfactions.command.argument.staff;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.manager.SearchCallback;
import org.hcgames.hcfactions.structure.FactionMember;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FactionForceLeaderArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionForceLeaderArgument(HCFactions plugin) {
        super("forceleader", "Forces the leader of a faction.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        plugin.getFactionManager().advancedSearch(args[1], PlayerFaction.class, new SearchCallback<PlayerFaction>() {

            @Override
            public void onSuccess(PlayerFaction faction) {
                FactionMember member = null;

                for(FactionMember search : faction.getMembers().values()){
                    if(search.getCachedName().equalsIgnoreCase(args[1])){
                        member = search;
                        break;
                    }
                }

                if (member == null) {
                    sender.sendMessage(ChatColor.RED + "Faction containing member with IGN or UUID " + args[1] + " not found.");
                    return;
                }

                if (member.getRole() == Role.LEADER) {
                    sender.sendMessage(ChatColor.RED + member.getCachedName() + " is already the leader of " + faction.getFormattedName(sender) + ChatColor.RED + '.');
                    return;
                }

                Optional<FactionMember> leader = faction.getLeader();
                String oldLeaderName = leader.isPresent() ? "none" : leader.get().getCachedName();
                String newLeaderName = member.getCachedName();

                // Demote the previous leader, promoting the new.
                if (leader.isPresent()) {
                    leader.get().setRole(Role.CAPTAIN);
                }

                member.setRole(Role.LEADER);
                faction.broadcast(ChatColor.YELLOW + sender.getName() + " has forcefully set the leader to " + newLeaderName + '.');

                sender.sendMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + "Leader of " + faction.getName() + " was forcefully set from " + oldLeaderName + " to " + newLeaderName + '.');
            }

            @Override
            public void onFail(FailReason reason) {
                sender.sendMessage(plugin.getMessages().getString("commands.error.faction_not_found", args[1]));
            }
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2 ? null : Collections.emptyList();
    }
}
