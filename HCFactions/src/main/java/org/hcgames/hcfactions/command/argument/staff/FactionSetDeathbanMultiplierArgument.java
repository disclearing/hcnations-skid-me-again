package org.hcgames.hcfactions.command.argument.staff;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.manager.SearchCallback;
import technology.brk.util.JavaUtils;
import technology.brk.util.command.CommandArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactionSetDeathbanMultiplierArgument extends CommandArgument {

    private static final double MIN_MULTIPLIER = 0.0;
    private static final double MAX_MULTIPLIER = 5.0;

    private final HCFactions plugin;

    public FactionSetDeathbanMultiplierArgument(HCFactions plugin) {
        super("setdeathbanmultiplier", "Sets the deathban multiplier of a faction.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName|factionName> <newMultiplier>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        plugin.getFactionManager().advancedSearch(args[1], Faction.class, new SearchCallback<Faction>() {
            @Override
            public void onSuccess(Faction faction) {
                Double multiplier = JavaUtils.tryParseDouble(args[2]);

                if (multiplier == null) {
                    sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a valid number.");
                    return;
                }

                if (multiplier < MIN_MULTIPLIER) {
                    sender.sendMessage(ChatColor.RED + "Deathban multipliers may not be less than " + MIN_MULTIPLIER + '.');
                    return;
                }

                if (multiplier > MAX_MULTIPLIER) {
                    sender.sendMessage(ChatColor.RED + "Deathban multipliers may not be more than " + MAX_MULTIPLIER + '.');
                    return;
                }

                double previousMultiplier = faction.getDeathbanMultiplier();
                faction.setDeathbanMultiplier(multiplier);

                Command.broadcastCommandMessage(sender, ChatColor.YELLOW + "Set deathban multiplier of " + faction.getName() + " from " + previousMultiplier + " to " + multiplier + '.');
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
        if (args.length != 2) {
            return Collections.emptyList();
        } else if (args[1].isEmpty()) {
            return null;
        } else {
            List<String> results = new ArrayList<>(plugin.getFactionManager().getFactionNameMap().keySet());
            Player senderPlayer = sender instanceof Player ? ((Player) sender) : null;
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Make sure the player can see.
                if (senderPlayer == null || senderPlayer.canSee(player)) {
                    results.add(player.getName());
                }
            }

            return results;
        }
    }
}
