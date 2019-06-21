package com.doctordark.hcf.deathban.lives.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import technology.brk.util.command.CommandArgument;

import java.util.Collections;
import java.util.List;

/**
 * An {@link CommandArgument} used to check how many lives a {@link Player} has.
 */
public class LivesCheckArgument extends CommandArgument{

    private final HCF plugin;

    public LivesCheckArgument(HCF plugin){
        super("check", "Check how much lives a player has");
        this.plugin = plugin;
        this.permission = "hcf.command.lives.argument." + getName();
    }

    @Override
    public String getUsage(String label){
        return plugin.getMessages().getString("Commands.Lives.Subcommand.Check.Usage")
                .replace("{commandLabel}", label)
                .replace("{subCommandLabel}", getName());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        OfflinePlayer target;
        if(args.length > 1){
            target = Bukkit.getOfflinePlayer(args[1]); //TODO: breaking
        }else if(sender instanceof Player){
            target = (Player) sender;
        }else{
            sender.sendMessage(plugin.getMessages().getString("Commands.Lives.Usage")
                    .replace("{commandUsage}", getUsage(label)));
            return true;
        }

        if(!target.hasPlayedBefore() && !target.isOnline()){
            sender.sendMessage(plugin.getMessages().getString("Error-Messages.InvalidPlayer")
                    .replace("{player}", args[1]));
            return true;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            int targetLives = plugin.getDeathbanManager().getLives(target.getUniqueId());

            sender.sendMessage(plugin.getMessages().getString("Commands.Lives.Subcommand.Check.Output" +
                    (target.getName().equals(sender.getName()) ? "" : "-Other"))
                    .replace("{target}", target.getName())
                    .replace("{livesAmount}", String.valueOf(targetLives))
                    .replace("{s}", (targetLives == 1 ? "" : "s")));
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        return args.length == 2 ? null : Collections.emptyList();
    }
}
