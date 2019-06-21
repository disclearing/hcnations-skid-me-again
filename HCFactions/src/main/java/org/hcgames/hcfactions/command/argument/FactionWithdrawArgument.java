package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.economy.EconomyManager;
import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.FactionMember;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.JavaUtils;
import technology.brk.util.command.CommandArgument;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FactionWithdrawArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionWithdrawArgument(HCFactions plugin) {
        super("withdraw", "Withdraws money from the faction balance.", new String[]{"w"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <all|amount>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can update the faction balance.");
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
        FactionMember factionMember = playerFaction.getMember(uuid);

        if (factionMember.getRole() == Role.MEMBER) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Withdraw-OfficerRequired"));
            //sender.sendMessage(ChatColor.RED + "You must be a faction officer to withdraw money.");
            return true;
        }

        int factionBalance = playerFaction.getBalance();
        Integer amount;

        if (args[1].equalsIgnoreCase("all")) {
            amount = factionBalance;
        } else {
            if ((amount = (JavaUtils.tryParseInt(args[1]))) == null) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Deposit-InvalidNumber")
                        .replace("{amount}", args[1]));
                return true;
            }
        }

        if (amount <= 0) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Withdraw-MustBePositive"));
            //sender.sendMessage(ChatColor.RED + "Amount must be positive.");
            return true;
        }

        if (amount > factionBalance) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Withdraw-MustBePositive")
                    .replace("{requiredAmount}", EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount))
                    .replace("{currentBalance}", EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(factionBalance)));
            //sender.sendMessage(ChatColor.RED + "Your faction need at least " + EconomyManager.ECONOMY_SYMBOL +
            //        JavaUtils.format(amount) + " to do this, whilst it only has " + EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(factionBalance) + '.');

            return true;
        }

        HCF.getPlugin().getEconomyManager().addBalance(uuid, amount);
        playerFaction.setBalance(factionBalance - amount);
        playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Withdraw-Broadcast")
                .replace("{player}", factionMember.getRole().getAstrix() + sender.getName())
                .replace("{amount}", EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount)));
        //playerFaction.broadcast(plugin.getConfiguration().getRelationColourTeammate() + factionMember.getRole().getAstrix() + sender.getName() + ChatColor.YELLOW + " has withdrew " +
        //        ChatColor.BOLD + EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount) + ChatColor.YELLOW + " from the faction balance.");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2 ? COMPLETIONS : Collections.emptyList();
    }

    private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("all");
}
