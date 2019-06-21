package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

import java.util.UUID;

public class FactionLeaveArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionLeaveArgument(HCFactions plugin) {
        super("leave", "Leave your current faction.");
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can leave faction.");
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

        UUID uuid = player.getUniqueId();
        if (playerFaction.getMember(uuid).getRole() == Role.LEADER) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Leave-CannotLeaveAsLeader")
                    .replace("{commandLabel}", label));
            //sender.sendMessage(ChatColor.RED + "You cannot leave factions as a leader. Either use " + ChatColor.GOLD + '/' + label + " disband" + ChatColor.RED + " or " +
            //       ChatColor.GOLD + '/' + label + " leader" + ChatColor.RED + '.');

            return true;
        }

        if (playerFaction.removeMember(player, player, player.getUniqueId(), false, false)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Leave-LeaveSuccess"));
            //sender.sendMessage(ChatColor.YELLOW + "Successfully left the faction.");
            playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Leave-LeaveBroadcast")
                    .replace("{sender}", sender.getName()));
            //playerFaction.broadcast(Relation.ENEMY.toChatColour() + sender.getName() + ChatColor.YELLOW + " has left the faction.");
        }

        return true;
    }
}
