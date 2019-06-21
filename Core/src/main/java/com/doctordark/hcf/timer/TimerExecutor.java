package com.doctordark.hcf.timer;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.timer.argument.TimerCheckArgument;
import com.doctordark.hcf.timer.argument.TimerSetArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import technology.brk.util.command.ArgumentExecutor;
import technology.brk.util.command.CommandArgument;

/**
 * Handles the execution and tab completion of the timer command.
 */
public class TimerExecutor extends ArgumentExecutor{

    public TimerExecutor(HCF plugin){
        super("timer");

        addArgument(new TimerCheckArgument(plugin));
        addArgument(new TimerSetArgument(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length >= 1){
            CommandArgument argument = getArgument(args[0]);
            if(argument == null){
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Unknown-Subcommand")
                        .replace("{subCommand}", args[0])
                        .replace("{commandLabel}", command.getName()));
                return true;
            }else{
                return super.onCommand(sender, command, label, args);
            }
        }

        return super.onCommand(sender, command, label, args);
    }
}
