package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.JavaUtils;
import technology.brk.util.command.CommandArgument;

import java.util.concurrent.TimeUnit;

public class FactionRenameArgument extends CommandArgument {

    private static final long FACTION_RENAME_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(15L);
    private static final String FACTION_RENAME_DELAY_WORDS = DurationFormatUtils.formatDurationWords(FACTION_RENAME_DELAY_MILLIS, true, true);

    private final HCFactions plugin;

    public FactionRenameArgument(HCFactions plugin) {
        super("rename", "Change the name of your faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"changename", "setname"};
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <newFactionName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can create faction.");
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

        Role role = playerFaction.getMember(player.getUniqueId()).getRole();
        if (!(role == Role.LEADER || role == Role.COLEADER)) {
            //sender.sendMessage(ChatColor.RED + "You must be a faction leader to edit the name.");
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Rename-CoLeaderRequired"));
            return true;
        }

        String newName = args[1];

        if (plugin.getConfiguration().getFactionDisallowedNames().contains(newName)) {
            //sender.sendMessage(ChatColor.RED + "'" + newName + "' is a blocked faction name.");
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Rename-BlockedName")
                    .replace("{factionName}", newName));
            return true;
        }

        int value = plugin.getConfiguration().getFactionNameMinCharacters();

        if (newName.length() < value) {
            //sender.sendMessage(ChatColor.RED + "Faction names must have at least " + value + " characters.");
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Rename-MinimumChars")
                    .replace("{minChars}", String.valueOf(value)));
            return true;
        }

        value = plugin.getConfiguration().getFactionNameMaxCharacters();

        if (newName.length() > value) {
            //sender.sendMessage(ChatColor.RED + "Faction names cannot be longer than " + value + " characters.");
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Rename-MaximumChars")
                    .replace("{maxChars}", String.valueOf(value)));
            return true;
        }

        if (!JavaUtils.isAlphanumeric(newName)) {
            //sender.sendMessage(ChatColor.RED + "Faction names may only be alphanumeric.");
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Rename-MustBeAlphanumeric"));
            return true;
        }

        try {
            if (plugin.getFactionManager().getFaction(newName) != null) {
                //sender.sendMessage(ChatColor.RED + "Faction " + newName + ChatColor.RED + " already exists.");
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Rename-NameAlreadyExists")
                        .replace("{factionNewName}", newName));
                return true;
            }
        } catch (NoFactionFoundException e) {}

        long difference = (playerFaction.getLastRenameMillis() - System.currentTimeMillis()) + FACTION_RENAME_DELAY_MILLIS;

        if (!player.isOp() && difference > 0L) {
            player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Rename-RenameDelay")
                    .replace("{factionRenameDelay}", FACTION_RENAME_DELAY_WORDS)
                    .replace("{factionRenameTimeLeft}", DurationFormatUtils.formatDurationWords(difference, true, true)));
            //player.sendMessage(ChatColor.RED + "There is a faction rename delay of " + FACTION_RENAME_DELAY_WORDS + ". Therefore you need to wait another " +
            //        DurationFormatUtils.formatDurationWords(difference, true, true) + " to rename your faction.");

            return true;
        }

        playerFaction.setName(args[1], sender);
        return true;
    }
}
