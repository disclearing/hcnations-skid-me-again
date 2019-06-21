package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

public class FactionDisbandArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionDisbandArgument(HCFactions plugin) {
        super("disband", "Disband your faction.");
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-ConsoleOnly"));
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

        if (playerFaction.isRaidable() && !HCF.getPlugin().getConfiguration().isKitMap() && !HCF.getPlugin().getEotwHandler().isEndOfTheWorld()) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Disband-Raidable"));
            return true;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Disband-LeaderRequired"));
            return true;
        }

        plugin.getFactionManager().removeFaction(playerFaction, sender);
        return true;
    }
}
