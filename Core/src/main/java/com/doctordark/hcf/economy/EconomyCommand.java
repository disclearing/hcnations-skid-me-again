package com.doctordark.hcf.economy;

import com.doctordark.hcf.HCF;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import technology.brk.base.BaseConstants;
import technology.brk.util.BukkitUtils;
import technology.brk.util.JavaUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Command used to check a players' balance.
 */

public class EconomyCommand implements CommandExecutor, TabCompleter{

    private static final ImmutableList<String> COMPLETIONS_SECOND = ImmutableList.of("add", "set", "take");
    private final HCF plugin;

    public EconomyCommand(HCF plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args){
        OfflinePlayer target;
        if(args.length > 0 && sender.hasPermission(command.getPermission() + ".staff")){
            target = BukkitUtils.offlinePlayerWithNameOrUUID(args[0]);
        }else if(sender instanceof Player){
            target = (Player) sender;
        }else{
            sender.sendMessage(plugin.getMessagesOld().getString("Commands-Economy-Bal-Usage")
                    .replace("{commandLabel}", label));
            return true;
        }

        if(!target.hasPlayedBefore() && !target.isOnline()){
            sender.sendMessage(String.format(BaseConstants.PLAYER_WITH_NAME_OR_UUID_NOT_FOUND, args[0]));
            return true;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID uuid = target.getUniqueId();
            double balance = plugin.getEconomyManager().getBalance(uuid);

            if(args.length < 2){
                sender.sendMessage(plugin.getMessagesOld().getString("Commands-Economy-Bal-ViewBalance")
                        .replace("{balanceFormat}",
                                (sender.equals(target) ?
                                        plugin.getMessagesOld().getString("Commands-Economy-Bal-YourBalanceFormat")
                                        : plugin.getMessagesOld().getString("Commands-Economy-Bal-BalanceOfFormat")))
                        .replace("{player}", target.getName())
                        .replace("{balance}", EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(balance)));

                return;
            }

            if((args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("add")) && sender.hasPermission("hcf.economy.give")){
                if(args.length < 3){
                    sender.sendMessage(plugin.getMessagesOld().getString("Commands-Economy-Give-Usage")
                            .replace("{commandLabel}", label)
                            .replace("{targetName}", target.getName())
                            .replace("{commandArgument}", args[1]));
                    return;
                }

                Double amount = JavaUtils.tryParseDouble(args[2]);

                if(amount == null){
                    sender.sendMessage(plugin.getMessagesOld().getString("Commands-Invalid-Number")
                            .replace("{number}", args[2]));
                    return;
                }

                double newBalance = plugin.getEconomyManager().addBalance(uuid, amount);
                sender.sendMessage(plugin.getMessagesOld().getString("Commands-Economy-Give-Success")
                        .replace("{amount}", EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount))
                        .replace("{targetPlayer}", target.getName())
                        .replace("{targetBalance}", EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(newBalance)));

                return;
            }

            if((args[1].equalsIgnoreCase("take") || args[1].equalsIgnoreCase("negate") || args[1].equalsIgnoreCase("minus") || args[1].equalsIgnoreCase("subtract")) && sender.hasPermission("hcf.economy.take")){
                if(args.length < 3){
                    sender.sendMessage(plugin.getMessagesOld().getString("Commands-Economy-Take-Usage")
                            .replace("{commandLabel}", label)
                            .replace("{targetName}", target.getName())
                            .replace("{commandArgument}", args[1]));
                    return;
                }

                Double amount = JavaUtils.tryParseDouble(args[2]);

                if(amount == null){
                    sender.sendMessage(plugin.getMessagesOld().getString("Commands-Invalid-Number")
                            .replace("{number}", args[2]));
                    return;
                }

                double newBalance = plugin.getEconomyManager().subtractBalance(uuid, amount);

                sender.sendMessage(plugin.getMessagesOld().getString("Commands-Economy-Take-Success")
                        .replace("{amount}", String.valueOf(EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(amount)))
                        .replace("{targetPlayer}", target.getName())
                        .replace("{targetBalance}", String.valueOf(EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(newBalance))));

                return;
            }

            if(args[1].equalsIgnoreCase("set") && sender.hasPermission("hcf.economy.set")){
                if(args.length < 3){
                    sender.sendMessage(plugin.getMessagesOld().getString(""));
                    sender.sendMessage(plugin.getMessagesOld().getString("Commands-Economy-Set-Usage")
                            .replace("{commandLabel}", label)
                            .replace("{targetName}", target.getName())
                            .replace("{commandArgument}", args[1]));
                    sender.sendMessage(ChatColor.RED + "Usage: /" + label + ' ' + target.getName() + ' ' + args[1] + " <amount>");
                    return;
                }

                Double amount = JavaUtils.tryParseDouble(args[2]);

                if(amount == null){
                    sender.sendMessage(plugin.getMessagesOld().getString("Commands-Invalid-Number")
                            .replace("{number}", args[2]));
                    return;
                }

                double newBalance = plugin.getEconomyManager().setBalance(uuid, amount);
                sender.sendMessage(plugin.getMessagesOld().getString("Commands-Economy-Set-Success")
                        .replace("{amount}", EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(newBalance))
                        .replace("{targetPlayer}", target.getName()));
                return;
            }

            sender.sendMessage(plugin.getMessagesOld().getString("Commands-Economy-Bal-ViewBalance")
                    .replace("{balanceFormat}",
                            (sender.equals(target) ?
                                    plugin.getMessagesOld().getString("Commands-Economy-Bal-YourBalanceFormat")
                                    : plugin.getMessagesOld().getString("Commands-Economy-Bal-BalanceOfFormat")))
                    .replace("{player}", target.getName())
                    .replace("{balance}", EconomyManager.ECONOMY_SYMBOL + JavaUtils.format(balance)));
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        switch(args.length){
            case 1:
                List<String> results = Lists.newArrayList("top");
                if(sender.hasPermission(command.getPermission() + ".staff")){
                    Player senderPlayer = sender instanceof Player ? (Player) sender : null;
                    for(Player player : Bukkit.getOnlinePlayers()){
                        if(senderPlayer == null || senderPlayer.canSee(player)){
                            results.add(player.getName());
                        }
                    }
                }

                return BukkitUtils.getCompletions(args, results);
            case 2:
                if(!args[0].equals("top") && sender.hasPermission(command.getPermission() + ".staff")){
                    return BukkitUtils.getCompletions(args, COMPLETIONS_SECOND);
                }
            default:
                return Collections.emptyList();
        }
    }
}
