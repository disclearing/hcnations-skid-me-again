package com.doctordark.hcf.deathban.lives;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.deathban.lives.argument.LivesCheckArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesCheckDeathbanArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesClearDeathbansArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesGiveArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesHelpArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesReviveArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesSetArgument;
import com.doctordark.hcf.deathban.lives.argument.LivesSetDeathbanTimeArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import technology.brk.util.command.ArgumentExecutor;
import technology.brk.util.command.CommandArgument;

/**
 * Handles the execution and tab completion of the lives command.
 */
public class LivesExecutor extends ArgumentExecutor{

    private final CommandArgument helpArgument;

    public LivesExecutor(HCF plugin){
        super("lives");

        addArgument(new LivesCheckArgument(plugin));
        addArgument(new LivesCheckDeathbanArgument(plugin));
        addArgument(new LivesClearDeathbansArgument(plugin));
        addArgument(new LivesGiveArgument(plugin));
        addArgument(new LivesReviveArgument(plugin));
        addArgument(new LivesSetArgument(plugin));
        addArgument(new LivesSetDeathbanTimeArgument(plugin));
        addArgument(helpArgument = new LivesHelpArgument(this));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length < 1){
            helpArgument.onCommand(sender, command, label, args);
            return true;
        }

        CommandArgument argument = getArgument(args[0]);
        if(argument != null){
            String permission = argument.getPermission();
            if(permission == null || sender.hasPermission(permission)){
                argument.onCommand(sender, command, label, args);
                return true;
            }
        }else{
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Lives.Unknown-Subcommand")
                    .replace("{subCommand}", args[0])
                    .replace("{commandLabel}", command.getName()));
            return true;
        }

        helpArgument.onCommand(sender, command, label, args);
        return true;
    }
}
