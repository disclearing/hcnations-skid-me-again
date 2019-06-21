package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.manager.SearchCallback;
import org.hcgames.hcfactions.structure.ChatChannel;
import org.hcgames.hcfactions.structure.FactionMember;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Faction argument used to accept invitations from {@link Faction}s.
 */
public class FactionAcceptArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionAcceptArgument(HCFactions plugin) {
        super("accept", "Accept a join request from an existing faction.", new String[]{"join", "a"});
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

        if (args.length < 2) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Usage").replace("{usage}", getUsage(label)));
            return true;
        }

        Player player = (Player) sender;

        try {
            if (plugin.getFactionManager().getPlayerFaction(player) != null) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Accept-InFactionAlready"));
                return true;
            }
        } catch (NoFactionFoundException e) {}

        plugin.getFactionManager().advancedSearch(args[1], PlayerFaction.class, new SearchCallback<PlayerFaction>() {
            @Override
            public void onSuccess(PlayerFaction faction) {
                if (faction.getMembers().size() >= plugin.getConfiguration().getFactionMaxMembers()) {
                    sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Accept-FactionFull")
                            .replace("{factionName}", faction.getFormattedName(sender))
                            .replace("{factionPlayerLimits}", String.valueOf(plugin.getConfiguration().getFactionMaxMembers())));
                    return;
                }

                if (!faction.isOpen() && !faction.getInvitedPlayerNames().contains(player.getName().toLowerCase())) {
                    sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Accept-NotInvited").replace("{factionName}", faction.getFormattedName(sender)));
                    return;
                }

                if (faction.addMember(player, player, player.getUniqueId(), new FactionMember(player, ChatChannel.PUBLIC, Role.MEMBER), false)) {
                    faction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Accept-FactionJoinBroadcast").replace("{player}", sender.getName()));
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
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        return plugin.getFactionManager().getFactions().stream().
                filter(faction -> faction instanceof PlayerFaction && ((PlayerFaction) faction).getInvitedPlayerNames().contains(sender.getName())).
                map(faction -> sender.getName()).collect(Collectors.toList());
    }
}

