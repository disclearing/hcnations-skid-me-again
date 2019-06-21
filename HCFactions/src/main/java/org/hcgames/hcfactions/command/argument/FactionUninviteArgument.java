package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.FactionMember;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

import java.util.Set;

public class FactionUninviteArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionUninviteArgument(HCFactions plugin) {
        super("uninvite", "Revoke an invitation to a player.", new String[]{"deinvite", "deinv", "uninv", "revoke"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <all|playerName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can un-invite from a faction.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Usage").replace("{usage}", getUsage(label)));
            return true;
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction;
        try {
            playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        } catch (NoFactionFoundException e) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Global-NotInFaction"));
            return true;
        }
        FactionMember factionMember = playerFaction.getMember(player);

        if (factionMember.getRole() == Role.MEMBER) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Uninvite-OfficerRequired"));
            //sender.sendMessage(ChatColor.RED + "You must be a faction officer to un-invite players.");
            return true;
        }

        Set<String> invitedPlayerNames = playerFaction.getInvitedPlayerNames();

        if (args[1].equalsIgnoreCase("all")) {
            invitedPlayerNames.clear();
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Uninvite-ClearedAll"));
            //sender.sendMessage(ChatColor.YELLOW + "You have cleared all pending invitations.");
            return true;
        }

        if (!invitedPlayerNames.remove(args[1])) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Uninvite-NoPendingInvites")
                    .replace("{name}", args[1]));
            //sender.sendMessage(ChatColor.RED + "There is not a pending invitation for " + args[1] + '.');
            return true;
        }

        playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Uninvite-Broadcast")
                .replace("{player}", factionMember.getRole().getAstrix() + sender.getName())
                .replace("{name}", args[1]));
        //playerFaction.broadcast(ChatColor.YELLOW + factionMember.getRole().getAstrix() + sender.getName() + " has uninvited " +
        //        plugin.getConfiguration().getRelationColourEnemy() + args[1] + ChatColor.YELLOW + " from the faction.");

        return true;
    }

    /*@Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null || playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>(COMPLETIONS);
        results.addAll(playerFaction.getInvitedPlayerNames());
        return results;
    }

    private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("all");*/
}
