package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.FactionMember;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

/**
 * Faction argument used to demote players to members in {@link Faction}s.
 */
public class FactionDemoteArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionDemoteArgument(HCFactions plugin) {
        super("demote", "Demotes a player to a member.", new String[]{"uncaptain", "delcaptain", "delofficer"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName>";
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

        //if (playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER) {
        //    sender.sendMessage(plugin.getMessagesOld().getString("Commands-Factions-Demote-OfficerRequired"));
        //    return true;
        //}

        FactionMember targetMember = playerFaction.findMember(args[1]);

        if (targetMember == null) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Demote-PlayerNotInFaction"));
            return true;
        }


        Role currentRole = playerFaction.getMember(player.getUniqueId()).getRole();
        Role targetRole = targetMember.getRole();


        if(currentRole == Role.MEMBER || currentRole == Role.CAPTAIN && targetRole == Role.CAPTAIN){
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Demote-LeaderRequired"));
            return true;
        }

        if(targetRole == Role.MEMBER){
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Demote-LowestRank").replace("{player}", targetMember.getCachedName()));
            return true;
        }

        if(targetRole == Role.LEADER){
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Comamnds-Factions-Demote-LeaderDemote"));
            return true;
        }

        if(!(currentRole == Role.LEADER) && targetRole == Role.COLEADER){
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Demote-LeaderRequired"));
            return true;
        }

        if(!(currentRole == Role.COLEADER || currentRole == Role.LEADER) && targetRole != Role.CAPTAIN){
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Demote-CoLeaderRequired"));
            return true;
        }

        targetMember.setRole(targetRole == Role.COLEADER ? Role.CAPTAIN : Role.MEMBER);
        playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Demote-Success").replace("{player}",
                targetMember.getCachedName()).replace("{newRole}", targetMember.getRole().getName())
                .replace("{oldRole}", targetRole.getName()));
        return true;
    }

    // FIXME: 29/09/2016
    /*@Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null || (playerFaction.getMember(player.getUniqueId()).getRole() != Role.LEADER)) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();
        Collection<UUID> keySet = playerFaction.getMembers().keySet();
        for (UUID entry : keySet) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(entry);
            String targetName = target.getName();
            if (targetName != null && playerFaction.getMember(target.getUniqueId()).getRole() == Role.CAPTAIN) {
                results.add(targetName);
            }
        }

        return results;
    }*/
}
