package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import com.google.common.base.Joiner;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import technology.brk.util.command.CommandArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Faction argument used to check invites for {@link Faction}s.
 */
public class FactionInvitesArgument extends CommandArgument {

    private static final Joiner JOINER = Joiner.on(ChatColor.WHITE + ", " + ChatColor.GRAY);

    private final HCFactions plugin;

    public FactionInvitesArgument(HCFactions plugin) {
        super("invites", "View faction invitations.");
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can have faction invites.");
            return true;
        }

        List<String> receivedInvites = new ArrayList<>();
        for (Faction faction : plugin.getFactionManager().getFactions()) {
            if (faction instanceof PlayerFaction) {
                PlayerFaction targetPlayerFaction = (PlayerFaction) faction;
                if (targetPlayerFaction.getInvitedPlayerNames().contains(sender.getName())) {
                    receivedInvites.add(targetPlayerFaction.getFormattedName(sender));
                }
            }
        }

        try {
            PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction((Player) sender);
            Set<String> sentInvites = playerFaction.getInvitedPlayerNames();
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Invites-SentBy")
                    .replace("{factionName}", playerFaction.getFormattedName(sender))
                    .replace("{inviteCount}", String.valueOf(sentInvites.size()))
                    .replace("{invites}", (sentInvites.isEmpty() ? HCF.getPlugin().getMessagesOld().getString("Commands-Factions-SentByNoInvites-SentBy") : JOINER.join(sentInvites))));
        } catch (NoFactionFoundException e) {}


        sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Invites-Requested")
                .replace("{inviteCount}", String.valueOf(receivedInvites.size()))
                .replace("{invites}", (receivedInvites.isEmpty() ? HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Invites-RequestedNoInvites") : JOINER.join(receivedInvites))));
        //sender.sendMessage(ChatColor.AQUA + "Requested (" + receivedInvites.size() + ')' + ChatColor.DARK_AQUA + ": " +
        //        ChatColor.GRAY + (receivedInvites.isEmpty() ? "No factions have invited you." : JOINER.join(receivedInvites) + '.'));

        return true;
    }
}
