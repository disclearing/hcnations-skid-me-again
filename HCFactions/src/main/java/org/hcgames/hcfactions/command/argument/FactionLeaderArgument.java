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

import java.util.UUID;

public class FactionLeaderArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionLeaderArgument(HCFactions plugin) {
        super("leader", "Sets the new leader for your faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"setleader", "newleader"};
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Leader-PlayerOnlyCMD"));
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

        UUID uuid = player.getUniqueId();
        FactionMember selfMember = playerFaction.getMember(uuid);
        Role selfRole = selfMember.getRole();

        if (selfRole != Role.LEADER) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Leader-LeaderRequired"));
            //sender.sendMessage(ChatColor.RED + "You must be the current faction leader to transfer the faction.");
            return true;
        }

        FactionMember targetMember = playerFaction.findMember(args[1]);

        if (targetMember == null) {
            //sender.sendMessage(ChatColor.RED + "Player '" + args[1] + "' is not in your faction.");
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Leader-PlayerNotInFaction")
                    .replace("{name}", args[1]));
            return true;
        }

        if (targetMember.getUniqueId().equals(uuid)) {
            //sender.sendMessage(ChatColor.RED + "You are already the faction leader.");
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Leader-AlreadyLeader"));
            return true;
        }

        targetMember.setRole(Role.LEADER);
        selfMember.setRole(Role.CAPTAIN);

        playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Leader-LeaderTransferBroadcast")
                .replace("{previousLeaderName}", selfMember.getRole().getAstrix() + selfMember.getCachedName())
                .replace("{newLeaderName}", targetMember.getRole().getAstrix() + targetMember.getCachedName()));

        //ChatColor colour = plugin.getConfiguration().getRelationColourTeammate();
        //playerFaction.broadcast(colour + selfMember.getRole().getAstrix() + selfMember.getName() + ChatColor.YELLOW +
        //        " has transferred the faction to " + colour + targetMember.getRole().getAstrix() + targetMember.getName() + ChatColor.YELLOW + '.');

        return true;
    }

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
        Map<UUID, FactionMember> members = playerFaction.getMembers();
        for (Map.Entry<UUID, FactionMember> entry : members.entrySet()) {
            if (entry.getValue().getRole() != Role.LEADER) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(entry.getKey());
                String targetName = target.getName();
                if (targetName != null && !results.contains(targetName)) {
                    results.add(targetName);
                }
            }
        }

        return results;
    }*/
}
