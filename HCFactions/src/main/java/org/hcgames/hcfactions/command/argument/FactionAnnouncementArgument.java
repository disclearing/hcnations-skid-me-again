package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FactionAnnouncementArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionAnnouncementArgument(HCFactions plugin) {
        super("announcement", "Set your faction announcement.", new String[]{"announce", "motd"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <newAnnouncement>";
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
        PlayerFaction playerFaction;

        try {
            playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        } catch (NoFactionFoundException e) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Global-NotInFaction"));
            return true;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Announcement-OfficerRequired"));
            return true;
        }

        Optional<String> oldAnnouncement = playerFaction.getAnnouncement();
        String newAnnouncement;
        if (args[1].equalsIgnoreCase("clear") || args[1].equalsIgnoreCase("none") || args[1].equalsIgnoreCase("remove")) {
            newAnnouncement = null;
        } else {
            newAnnouncement = HCF.SPACE_JOINER.join(Arrays.copyOfRange(args, 1, args.length));
        }

        if (!oldAnnouncement.isPresent() && newAnnouncement == null) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Announcement-AlreadyUnset"));
            return true;
        }

        if (oldAnnouncement.isPresent() && newAnnouncement != null && oldAnnouncement.get().equals(newAnnouncement)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Announcement-SameAnnouncement")
                    .replace("%currentAnnouncement%", newAnnouncement));
            return true;
        }

        playerFaction.setAnnouncement(newAnnouncement);

        if (newAnnouncement == null) {
            playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Announcement-AnnouncementCleared")
                    .replace("{player}", sender.getName()));
            return true;
        }

        playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Announcement-BroadcastSwitched")
                .replace("{player}", player.getName())
                .replace("{oldAnnouncement}", (oldAnnouncement.isPresent() ? oldAnnouncement.get() : "none"))
                .replace("{newAnnouncement}", newAnnouncement));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        } else if (args.length == 2) {
            return CLEAR_LIST;
        } else {
            return Collections.emptyList();
        }
    }

    private static final ImmutableList<String> CLEAR_LIST = ImmutableList.of("clear");
}
