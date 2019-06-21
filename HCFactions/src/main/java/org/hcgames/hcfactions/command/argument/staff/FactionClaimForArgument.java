package org.hcgames.hcfactions.command.argument.staff;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.faction.ClaimableFaction;
import org.hcgames.hcfactions.manager.SearchCallback;
import technology.brk.util.command.CommandArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used to claim land for other {@link ClaimableFaction}s.
 */
public class FactionClaimForArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionClaimForArgument(HCFactions plugin) {
        super("claimfor", "Claims land for another faction.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return plugin.getMessages().getString("commands.staff.claimfor.usage", label, getName());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessages().getString("commands.error.player_only"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getMessages().getString("commands.error.usage", getUsage(label)));
            return true;
        }

        plugin.getFactionManager().advancedSearch(args[1], ClaimableFaction.class, new SearchCallback<ClaimableFaction>() {

            @Override
            public void onSuccess(ClaimableFaction faction) {
                Player player = (Player) sender;
                WorldEditPlugin worldEditPlugin = plugin.getWorldEdit();

                if (worldEditPlugin == null) {
                    sender.sendMessage(plugin.getMessages().getString("commands.claimfor.worldedit_required"));
                    return;
                }

                Selection selection = worldEditPlugin.getSelection(player);

                if (selection == null) {
                    sender.sendMessage(plugin.getMessages().getString("commands.claimfor.worldedit_selection_required"));
                    return;
                }

                if (faction.addClaim(new Claim(faction, selection.getMinimumPoint(), selection.getMaximumPoint()), sender)) {
                    sender.sendMessage(plugin.getMessages().getString("commands.claimfor.claimed", faction.getName()));
                }
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
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args[1].isEmpty()) {
            return null;
        }

        Player player = (Player) sender;
        List<String> results = new ArrayList<>(plugin.getFactionManager().getFactionNameMap().keySet());
        for (Player target : plugin.getServer().getOnlinePlayers()) {
            if (player.canSee(target) && !results.contains(target.getName())) {
                results.add(target.getName());
            }
        }

        return results;
    }
}
