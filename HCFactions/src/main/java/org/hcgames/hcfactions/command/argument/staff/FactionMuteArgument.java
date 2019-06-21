package org.hcgames.hcfactions.command.argument.staff;

import com.doctordark.hcf.HCF;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.manager.SearchCallback;
import technology.brk.util.command.CommandArgument;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FactionMuteArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionMuteArgument(HCFactions plugin) {
        super("mute", "Mutes every member in this faction.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <factionName> <time:(e.g. 1h2s)> <reason>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        plugin.getFactionManager().advancedSearch(args[1], org.hcgames.hcfactions.faction.PlayerFaction.class, new SearchCallback<org.hcgames.hcfactions.faction.PlayerFaction>() {

            @Override
            public void onSuccess(org.hcgames.hcfactions.faction.PlayerFaction faction){
                String extraArgs = HCF.SPACE_JOINER.join(Arrays.copyOfRange(args, 2, args.length));
                ConsoleCommandSender console = Bukkit.getConsoleSender();
                for (UUID uuid : faction.getMembers().keySet()) {
                    String commandLine = "mute " + uuid.toString() + " " + extraArgs;
                    sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Executing " + ChatColor.RED + commandLine);
                    console.getServer().dispatchCommand(sender, commandLine);
                }

                sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Executed mute action on faction " + faction.getName() + ".");
            }

            @Override
            public void onFail(FailReason reason){
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
