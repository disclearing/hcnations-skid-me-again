package com.doctordark.hcf.economy;

import com.doctordark.hcf.HCF;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import technology.brk.util.JavaUtils;

import java.util.Collections;
import java.util.List;

/**
 * Command used to pay other {@link Player}s some money.
 */
public class PayCommand implements CommandExecutor, TabCompleter{

    private final HCF plugin;

    public PayCommand(HCF plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length < 2){
            sender.sendMessage(plugin.getMessagesOld().getString("Commands-Pay-Usage")
                    .replace("{commandLabel}", label));
            return true;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Double amount = JavaUtils.tryParseDouble(args[1]);

            if(amount == null){
                sender.sendMessage(plugin.getMessagesOld().getString("Commands-Pay-InvalidNumber")
                        .replace("{enteredNumber}", String.valueOf(args[1])));
                return;
            }

            if(amount <= 0){
                sender.sendMessage(plugin.getMessagesOld().getString("Commands-Pay-NegativeNumber"));
                return;
            }

            // Calculate the senders balance here.
            Player senderPlayer = sender instanceof Player ? (Player) sender : null;
            double senderBalance = senderPlayer != null ? plugin.getEconomyManager().getBalance(senderPlayer.getUniqueId()) : 1024;

            if(senderBalance < amount){
                sender.sendMessage(plugin.getMessagesOld().getString("Commands-Pay-NotEnoughFunds")
                        .replace("{payAmount}", String.valueOf(EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount)))
                        .replace("{playerBalance}", String.valueOf(EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(senderBalance))));

                return;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]); //TODO: breaking

            if(sender.equals(target)){
                sender.sendMessage(plugin.getMessagesOld().getString("Commands-Pay-CannotSendToSelf"));
                return;
            }

            Player targetPlayer = target.getPlayer();

            if(!target.hasPlayedBefore() && targetPlayer == null){
                sender.sendMessage(plugin.getMessagesOld().getString("Commands-Pay-UnknownPlayer")
                        .replace("{enteredPlayer}", args[0]));
                return;
            }

            if(targetPlayer == null) return; // won't happen, IntelliJ compiler won't ignore

            // Make the money transactions.
            if(senderPlayer != null) plugin.getEconomyManager().subtractBalance(senderPlayer.getUniqueId(), amount);
            plugin.getEconomyManager().addBalance(targetPlayer.getUniqueId(), amount);

            targetPlayer.sendMessage(plugin.getMessagesOld().getString("Commands-Pay-MoneyReceived")
                    .replace("{sender}", sender.getName())
                    .replace("{amount}", String.valueOf(EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount))));
            sender.sendMessage(plugin.getMessagesOld().getString("Commands-Pay-MoneyPaid")
                    .replace("{amount}", String.valueOf(EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount)))
                    .replace("{receiver}", target.getName()));
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        return args.length == 1 ? null : Collections.emptyList();
    }
}
