package com.doctordark.hcf.deathban.lives.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.deathban.Deathban;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import technology.brk.util.JavaUtils;
import technology.brk.util.command.CommandArgument;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An {@link CommandArgument} used to set the base {@link Deathban} time, not including multipliers, etc.
 */
public class LivesSetDeathbanTimeArgument extends CommandArgument{

    private final HCF plugin;

    public LivesSetDeathbanTimeArgument(HCF plugin){
        super("setdeathbantime", "Sets the base deathban time");
        this.permission = "hcf.command.lives.argument." + getName();
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label){
        return '/' + label + ' ' + getName() + " <time>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length < 2){
            sender.sendMessage(plugin.getMessages().getString("Commands.Lives.Usage")
                    .replace("{commandUsage}", getUsage(label)));
            return true;
        }

        long duration = JavaUtils.parse(args[1]);

        if(duration == -1){
            sender.sendMessage(ChatColor.RED + "Invalid duration, use the correct format: 10m 1s");
            return true;
        }

        plugin.getConfiguration().setDeathbanBaseDurationMinutes((int) TimeUnit.MILLISECONDS.toMinutes(duration));
        Command.broadcastCommandMessage(sender, ChatColor.YELLOW + "Base death-ban time set to " +
                DurationFormatUtils.formatDurationWords(duration, true, true) + " (not including multipliers, etc).");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        return Collections.emptyList();
    }
}
