package com.doctordark.hcf.eventgame.conquest;

import com.doctordark.hcf.HCF;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import technology.brk.util.command.ArgumentExecutor;
import technology.brk.util.command.CommandArgument;

public class ConquestExecutor extends ArgumentExecutor{

    public ConquestExecutor(HCF plugin){
        super("conquest");
        addArgument(new ConquestSetpointsArgument(plugin));
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
