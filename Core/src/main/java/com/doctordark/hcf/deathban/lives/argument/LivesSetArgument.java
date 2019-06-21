package com.doctordark.hcf.deathban.lives.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import technology.brk.util.BukkitUtils;
import technology.brk.util.JavaUtils;
import technology.brk.util.command.CommandArgument;

import java.util.Collections;
import java.util.List;

/**
 * An {@link CommandArgument} used to set the lives of {@link Player}s.
 */
public class LivesSetArgument extends CommandArgument{

    private final HCF plugin;

    public LivesSetArgument(HCF plugin){
        super("set", "Set how much lives a player has");
        this.plugin = plugin;
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
            sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a number.");
            return true;
        }

        OfflinePlayer target = BukkitUtils.offlinePlayerWithNameOrUUID(args[1]);

        if(!target.hasPlayedBefore() && !target.isOnline()){
            sender.sendMessage(plugin.getMessages().getString("Error-Messages.InvalidPlayer")
                    .replace("{player}", args[1]));
            return true;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDeathbanManager().setLives(target.getUniqueId(), amount);
            sender.sendMessage(ChatColor.YELLOW + target.getName() + " now has " + ChatColor.GOLD + amount + ChatColor.YELLOW + " lives.");
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        return args.length == 2 ? null : Collections.emptyList();
    }
}
