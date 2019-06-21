package com.doctordark.hcf.deathban.lives.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.deathban.Deathban;
import com.google.common.base.Strings;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import technology.brk.util.DateTimeFormats;
import technology.brk.util.command.CommandArgument;

/**
 * An {@link CommandArgument} used to check the {@link Deathban} of a {@link Player}.
 */
public class LivesCheckDeathbanArgument extends CommandArgument{

    private final HCF plugin;

    public LivesCheckDeathbanArgument(HCF plugin){
        super("checkdeathban", "Check the deathban cause of player");
        this.plugin = plugin;
        this.permission = "hcf.command.lives.argument." + getName();
    }

    @Override
    public String getUsage(String label){
        return plugin.getMessages().getString("Commands.Lives.Subcommand.CheckDeathBan.Usage")
                .replace("{commandLabel}", label)
                .replace("{subCommandLabel}", getName());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length < 2){
            sender.sendMessage(plugin.getMessages().getString("Commands.Lives.Usage")
                    .replace("{commandUsage}", getUsage(label)));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]); //TODO: breaking

        if(!target.hasPlayedBefore() && !target.isOnline()){
            sender.sendMessage(plugin.getMessages().getString("Error-Messages.InvalidPlayer")
                    .replace("{player}", args[1]));
            return true;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Deathban deathban = plugin.getUserManager().getUser(target.getUniqueId()).getDeathban();

            if(deathban == null || !deathban.isActive()){
                sender.sendMessage(plugin.getMessages().getString("Commands.Lives.Subcommand.CheckDeathBan.NotDeathBanned" + (target.getName().equals(sender.getName()) ? "" : "-Other"))
                        .replace("{target}", target.getName()));
                return;
            }

            sender.sendMessage(plugin.getMessages().getString("Commands.Lives.Subcommand.CheckDeathBan.Output.Header")
                    .replace("{target}", target.getName()));
            sender.sendMessage(plugin.getMessages().getString("Commands.Lives.Subcommand.CheckDeathBan.Output.Time")
                    .replace("{deathBanTime}", DateTimeFormats.HR_MIN.format(deathban.getCreationMillis())));
            sender.sendMessage(plugin.getMessages().getString("Commands.Lives.Subcommand.CheckDeathBan.Output.Duration")
                    .replace("{deathBanDuration}", DurationFormatUtils.formatDurationWords(deathban.getInitialDuration(), true, true)));

            Location location = deathban.getDeathPoint();
            if(location != null){
                sender.sendMessage(plugin.getMessages().getString("Commands.Lives.Subcommand.CheckDeathBan.Output.Location")
                        .replace("{deathBanLocationX}", String.valueOf(location.getBlockX()))
                        .replace("{deathBanLocationY}", String.valueOf(location.getBlockY()))
                        .replace("{deathBanLocationZ}", String.valueOf(location.getBlockZ()))
                        .replace("{deathBanLocationWorld}", location.getWorld().getName()));
            }

            sender.sendMessage(plugin.getMessages().getString("Commands.Lives.Subcommand.CheckDeathBan.Output.Reason")
                    .replace("{deathBanReason}", Strings.nullToEmpty(deathban.getReason())));
        });
        return true;
    }
//
//    @Override
//    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
//        if(args.length != 2){
//            return Collections.emptyList();
//        }
//
//        List<String> results = new ArrayList<>();
//        for(FactionUser factionUser : plugin.getUserManager().getUsers().values()){
//            Deathban deathban = factionUser.getDeathban();
//            if(deathban != null && deathban.isActive()){
//                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(factionUser.getUserUUID());
//                String name = offlinePlayer.getName();
//                if(name != null){
//                    results.add(name);
//                }
//            }
//        }
//
//        return results;
//    }
}
