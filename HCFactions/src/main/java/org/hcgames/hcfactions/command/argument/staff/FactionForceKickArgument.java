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

public class FactionForceKickArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionForceKickArgument(HCFactions plugin) {
        super("forcekick", "Forcefully kick a player from their faction.");
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

        plugin.getFactionManager().advancedSearch(args[0], PlayerFaction.class, new SearchCallback<PlayerFaction>() {
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
                    sender.sendMessage(ChatColor.RED + "You cannot forcefully kick faction leaders. Use /f forceremove instead.");
                    return;
                }

                if (faction.removeMember(sender, null, member.getUniqueId(), true, true)) {
                    faction.broadcast(ChatColor.GOLD.toString() + ChatColor.BOLD + member.getCachedName() + " has been forcefully kicked by " + sender.getName() + '.');
                }
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
        return args.length == 2 ? null : Collections.<String>emptyList();
    }
}
