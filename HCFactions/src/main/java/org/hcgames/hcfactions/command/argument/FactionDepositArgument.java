package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.economy.EconomyManager;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import technology.brk.util.JavaUtils;
import technology.brk.util.command.CommandArgument;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FactionDepositArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionDepositArgument(HCFactions plugin) {
        super("deposit", "Deposits money to the faction balance.", new String[]{"d"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <all|amount>";
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

        UUID uuid = player.getUniqueId();
        Integer playerBalance = ((Double)HCF.getPlugin().getEconomyManager().getBalance(uuid)).intValue();

        Integer amount;
        if (args[1].equalsIgnoreCase("all")) {
            amount = playerBalance;
        } else {
            if ((amount = (JavaUtils.tryParseInt(args[1]))) == null) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Deposit-InvalidNumber")
                        .replace("{amount}", args[1]));
                return true;
            }
        }

        if (amount <= 0) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Deposit-AmountNotPositive"));
            return true;
        }

        if (playerBalance < amount) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Deposit-NotEnoughFunds")
                    .replace("{requiredAmount}", EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount))
                    .replace("{currentAmount}",  EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(playerBalance)));

            return true;
        }

        HCF.getPlugin().getEconomyManager().subtractBalance(uuid, amount);

        playerFaction.setBalance(playerFaction.getBalance() + amount);
        playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Deposit-BroadcastDeposit")
                .replace("{player}", playerFaction.getMember(player).getRole().getAstrix() + sender.getName())
                .replace("{amount}", EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount)));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return args.length == 2 ? COMPLETIONS : Collections.<String>emptyList();
    }

    private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("all");
}
