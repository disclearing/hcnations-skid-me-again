package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.FactionMember;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

import java.util.Optional;

public class FactionKickArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionKickArgument(HCFactions plugin) {
        super("kick", "Kick a player from the faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"kickmember", "kickplayer"};
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Kick-PlayerOnlyCMD"));
            //sender.sendMessage(ChatColor.RED + "Only players can kick from a faction.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Usage").replace("{usage}", getUsage(label)));
            return true;
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = null;
        try {
            playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        } catch (NoFactionFoundException e) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Global-NotInFaction"));
            return true;
        }

        if (playerFaction.isRaidable() && !HCF.getPlugin().getConfiguration().isKitMap() && !HCF.getPlugin().getEotwHandler().isEndOfTheWorld()) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Kick-Raidable"));
            //sender.sendMessage(ChatColor.RED + "You cannot kick players whilst your faction is raidable.");
            return true;
        }

        FactionMember targetMember = playerFaction.findMember(args[1]);

        if (targetMember == null) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Kick-NoMemberNamed")
                    .replace("{name}", args[1]));
            //sender.sendMessage(ChatColor.RED + "Your faction does not have a member named '" + args[1] + "'.");
            return true;
        }

        Role selfRole = playerFaction.getMember(player.getUniqueId()).getRole();

        if (selfRole == Role.MEMBER) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Kick-OfficerRequired"));
            //sender.sendMessage(ChatColor.RED + "You must be a faction officer to kick members.");
            return true;
        }

        Role targetRole = targetMember.getRole();

        if (targetRole == Role.LEADER) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Kick-CannotKickLeader"));
            //sender.sendMessage(ChatColor.RED + "You cannot kick the faction leader.");
            return true;
        }

        if ((targetRole == Role.CAPTAIN || targetRole == Role.COLEADER) && selfRole == Role.CAPTAIN) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Kick-CoLeaderRequired"));
            //sender.sendMessage(ChatColor.RED + "You must be a faction leader to kick captains.");
            return true;
        }

        if(targetRole == Role.COLEADER && selfRole == Role.COLEADER){
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Kick-LeaderRequired"));
            return true;
        }

        Optional<Player> onlineTarget = targetMember.toOnlinePlayer();
        if (playerFaction.removeMember(sender, onlineTarget.orElse(null), targetMember.getUniqueId(), true, true)) {
            if (onlineTarget.isPresent()) {
                onlineTarget.get().sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Kick-Kicked")
                        .replace("{sender}", sender.getName()));
                //onlineTarget.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "You were kicked from the faction by " + sender.getName() + '.');
            }

            playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Kick-KickedBroadcast")
                    .replace("{player}", targetMember.getCachedName())
                    .replace("{sender}", playerFaction.getMember(player).getRole().getAstrix() + sender.getName()));
            //playerFaction.broadcast(plugin.getConfiguration().getRelationColourEnemy() + targetMember.getName() + ChatColor.YELLOW + " has been kicked by " +
            //        plugin.getConfiguration().getRelationColourTeammate() + playerFaction.getMember(player).getRole().getAstrix() + sender.getName() + ChatColor.YELLOW + '.');
        }

        return true;
    }

    /*@Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null) {
            return Collections.emptyList();
        }

        Role memberRole = playerFaction.getMember(player.getUniqueId()).getRole();
        if (memberRole == Role.MEMBER) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();
        for (UUID entry : playerFaction.getMembers().keySet()) {
            Role targetRole = playerFaction.getMember(entry).getRole();
            if (targetRole == Role.LEADER || (targetRole == Role.CAPTAIN && memberRole != Role.LEADER)) {
                continue;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(entry);
            String targetName = target.getName();
            if (targetName != null && !results.contains(targetName)) {
                results.add(targetName);
            }
        }

        return results;
    }*/
}
