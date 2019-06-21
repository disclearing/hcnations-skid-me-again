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

public class FactionForceDemoteArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionForceDemoteArgument(HCFactions plugin) {
        super("forcedemote", "Forces the demotion status of a player.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();
    }

    @Override
    public String getUsage(String label){
        return plugin.getMessages().getString("commands.staff.forcedemote.usage", label, getName());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            plugin.getMessages().getString("commands.error.usage", getUsage(label));
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
                    sender.sendMessage(plugin.getMessages().getString("commands.error.member_not_found", args[1]));
                    return;
                }

                if (member.getRole() == Role.LEADER) {
                    sender.sendMessage(plugin.getMessages().getString("command.staff.forcedemote.leader_demote", member.getCachedName()));
                    return;
                }

                if(member.getRole() == Role.LEADER){
                    sender.sendMessage(plugin.getMessages().getString("command.staff.forcedemote.user_demote", member.getCachedName()));
                    return;
                }

                Role newRole;

                if(member.getRole() == Role.COLEADER){
                    newRole = Role.CAPTAIN;
                }else if(member.getRole() == Role.CAPTAIN){
                    newRole = Role.MEMBER;
                }else{
                    //Should never happen
                    newRole = Role.MEMBER;
                }

                member.setRole(newRole);
                faction.broadcast(plugin.getMessages().getString("commands.staff.forcedemote.demote_broadcast", member.getCachedName(), newRole.getName()));
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
