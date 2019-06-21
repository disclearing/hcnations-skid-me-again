package org.hcgames.hcfactions.command.argument.staff;

import com.doctordark.hcf.HCF;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.manager.SearchCallback;
import technology.brk.util.command.CommandArgument;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FactionBanArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionBanArgument(HCFactions plugin) {
        super("ban", "Bans every member in this faction.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return plugin.getMessages().getString("commands.staff.ban.usage", label, getName());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getMessages().getString("commands.error.usage", getUsage(label)));
            return true;
        }

        plugin.getFactionManager().advancedSearch(args[1], PlayerFaction.class, new SearchCallback<PlayerFaction>() {

            @Override
            public void onSuccess(PlayerFaction faction){
                String extraArgs = HCFactions.SPACE_JOINER.join(Arrays.copyOfRange(args, 2, args.length));
                ConsoleCommandSender console = plugin.getServer().getConsoleSender();

                for (UUID uuid : faction.getMembers().keySet()) {
                    String commandLine = "ban " + uuid.toString() + " " + extraArgs;
                    sender.sendMessage(plugin.getMessages().getString("commands.staff.ban.executing", commandLine));
                    console.getServer().dispatchCommand(sender, commandLine);
                }

                sender.sendMessage(plugin.getMessages().getString("commands.staff.ban.executed", faction.getName()));
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
