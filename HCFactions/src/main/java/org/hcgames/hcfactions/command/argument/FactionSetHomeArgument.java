package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.FactionMember;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

public class FactionSetHomeArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionSetHomeArgument(HCFactions plugin) {
        super("sethome", "Sets the faction home location.");
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

        if (plugin.getConfiguration().getMaxHeightFactionHome() != -1 && player.getLocation().getY() > plugin.getConfiguration().getMaxHeightFactionHome()) {
            //sender.sendMessage(ChatColor.RED + "You can not set your faction home above y " + plugin.getConfiguration().getMaxHeightFactionHome() + ".");
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-SetHome-MaxHeight")
                    .replace("{maxHeight}", String.valueOf(plugin.getConfiguration().getMaxHeightFactionHome())));
            return true;
        }

        PlayerFaction playerFaction;
        try {
            playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        } catch (NoFactionFoundException e) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Global-NotInFaction"));
            return true;
        }

        FactionMember factionMember = playerFaction.getMember(player);

        if (factionMember.getRole() == Role.MEMBER) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-SetHome-OfficerRequired"));
            //sender.sendMessage(ChatColor.RED + "You must be a faction officer to set the home.");
            return true;
        }

        Location location = player.getLocation();

        boolean insideTerritory = false;
        for (Claim claim : playerFaction.getClaims()) {
            if (claim.contains(location)) {
                insideTerritory = true;
                break;
            }
        }

        if (!insideTerritory) {
            player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-SetHome-NotInsideHomeTerritory"));
            //player.sendMessage(ChatColor.RED + "You may only set your home in your territory.");
            return true;
        }

        playerFaction.setHome(location);
        playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-SetHome-Broadcast")
                .replace("{player}", factionMember.getRole().getAstrix() + sender.getName()));
        //playerFaction.broadcast(plugin.getConfiguration().getRelationColourTeammate() + factionMember.getRole().getAstrix() +
        //        sender.getName() + ChatColor.YELLOW + " has updated the faction home.");

        return true;
    }
}
