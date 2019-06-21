package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.manager.SearchCallback;
import technology.brk.util.command.CommandArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactionShowArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionShowArgument(HCFactions plugin) {
        super("show", "Get details about a faction.", new String[]{"i", "info", "who"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " [playerName|factionName]";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Faction playerFaction = null;
        Faction namedFaction = null;

        if (args.length < 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Usage").replace("{usage}", getUsage(label)));
                return true;
            }

            try {
                namedFaction = plugin.getFactionManager().getPlayerFaction((Player) sender);
            } catch (NoFactionFoundException e) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Global-NotInFaction"));
                return true;
            }

            if (namedFaction == null) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Global-NotInFaction"));
                return true;
            }

            namedFaction.sendInformation(sender);
        } else {
            try{
                namedFaction = plugin.getFactionManager().getFaction(args[1]);
                namedFaction.sendInformation(sender);
            } catch (NoFactionFoundException ignored){}

            Faction finalNamedFaction = namedFaction;
            plugin.getFactionManager().advancedSearch(args[1], PlayerFaction.class, new SearchCallback<PlayerFaction>() {
                @Override
                public void onSuccess(PlayerFaction faction) {
                    if(finalNamedFaction != null && finalNamedFaction.equals(faction)){
                        return;
                    }
                    faction.sendInformation(sender);
                }

                @Override
                public void onFail(FailReason reason) {
                    if(finalNamedFaction == null){
                        sender.sendMessage(plugin.getMessages().getString("commands.error.faction_not_found", args[1]));
                    }
                }
            }, true);

        }

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
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player.canSee(target) && !results.contains(target.getName())) {
                results.add(target.getName());
            }
        }

        return results;
    }
}
