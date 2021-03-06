package com.doctordark.hcf.deathban.lives.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import technology.brk.util.JavaUtils;
import technology.brk.util.command.CommandArgument;

import java.util.Collections;
import java.util.List;

/**
 * An {@link CommandArgument} used to give lives to {@link Player}s.
 */
public class LivesGiveArgument extends CommandArgument{

    private final HCF plugin;

    public LivesGiveArgument(HCF plugin){
        super("give", "Give lives to a player");
        this.plugin = plugin;
        this.aliases = new String[]{"transfer", "send", "pay", "add"};
        this.permission = "hcf.command.lives.argument." + getName();
    }

    @Override
    public String getUsage(String label){
        return '/' + label + ' ' + getName() + " <playerName> <amount>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length < 3){
            sender.sendMessage(plugin.getMessages().getString("Commands.Lives.Usage")
                    .replace("{commandUsage}", getUsage(label)));
            return true;
        }

        Integer amount = JavaUtils.tryParseInt(args[2]);

        if(amount == null){
            sender.sendMessage(plugin.getMessages().getString("Error-Messages.InvalidNumber")
                    .replace("{player}", args[2]));
            return true;
        }

        if(amount <= 0){
            sender.sendMessage(ChatColor.RED + "The amount of lives must be positive.");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]); //TODO: breaking

        if(!target.hasPlayedBefore() && !target.isOnline()){
            sender.sendMessage(plugin.getMessages().getString("Error-Messages.InvalidPlayer")
                    .replace("{player}", args[1]));
            return true;
        }

        Player onlineTarget = target.getPlayer();

        if(sender instanceof Player){
            Player player = (Player) sender;

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                int ownedLives = plugin.getDeathbanManager().getLives(player.getUniqueId());

                if(amount > ownedLives){
                    sender.sendMessage(ChatColor.RED + "You tried to give " + target.getName() + ' ' +
                            amount + " lives, but you only have " + ownedLives + '.');
                    return;
                }

                plugin.getDeathbanManager().takeLives(player.getUniqueId(), amount);

                plugin.getDeathbanManager().addLives(target.getUniqueId(), amount);
                sender.sendMessage(ChatColor.YELLOW + "You have sent " + ChatColor.GOLD + target.getName() +
                        ChatColor.YELLOW + ' ' + amount + ' ' + (amount > 1 ? "lives" : "life") + ChatColor.YELLOW + '.');
                if(onlineTarget != null){
                    onlineTarget.sendMessage(ChatColor.GOLD + sender.getName() + ChatColor.YELLOW + " has sent you " +
                            ChatColor.GOLD + amount + ' ' + (amount > 1 ? "lives" : "life") + ChatColor.YELLOW + '.');
                }
            });
        }else{
            plugin.getDeathbanManager().addLives(target.getUniqueId(), amount);
            sender.sendMessage(ChatColor.YELLOW + "You have sent " + ChatColor.GOLD + target.getName() +
                    ChatColor.YELLOW + ' ' + amount + ' ' + (amount > 1 ? "lives" : "life") + ChatColor.YELLOW + '.');
            if(onlineTarget != null){
                onlineTarget.sendMessage(ChatColor.GOLD + sender.getName() + ChatColor.YELLOW + " has sent you " +
                        ChatColor.GOLD + amount + ' ' + (amount > 1 ? "lives" : "life") + ChatColor.YELLOW + '.');
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        return args.length == 2 ? null : Collections.<String>emptyList();
    }
}
