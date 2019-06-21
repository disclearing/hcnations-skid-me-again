package com.doctordark.hcf.deathban.lives.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.deathban.Deathban;
import com.doctordark.hcf.user.FactionUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import technology.brk.util.command.CommandArgument;

/**
 * An {@link CommandArgument} used to clear all {@link Deathban}s.
 */
public class LivesClearDeathbansArgument extends CommandArgument{

    private final HCF plugin;

    public LivesClearDeathbansArgument(HCF plugin){
        super("cleardeathbans", "Clears the global deathbans");
        this.plugin = plugin;
        this.aliases = new String[]{"resetdeathbans"};
        this.permission = "hcf.command.lives.argument." + getName();
    }

    @Override
    public String getUsage(String label){
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getUserManager().getUsers().values().forEach(FactionUser::removeDeathban);

            sender.sendMessage(plugin.getMessages().getString("Commands.Lives.Subcommand.ClearDeathBans"));
            Command.broadcastCommandMessage(sender, plugin.getMessages().getString("Broadcast.Deathbans-Cleared"), false);
        });
        return true;
    }
}
