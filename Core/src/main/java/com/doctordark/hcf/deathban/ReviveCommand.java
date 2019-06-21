package com.doctordark.hcf.deathban;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.user.FactionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class ReviveCommand implements CommandExecutor{

    private final HCF plugin;

    public ReviveCommand(HCF plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length < 1){
            sender.sendMessage(plugin.getMessages().getString("Commands.Revive.Usage")
                    .replace("{commandLabel}", label));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]); //TODO: breaking

        if(!target.hasPlayedBefore() && !target.isOnline()){
            sender.sendMessage(plugin.getMessages().getString("Error-Messages.InvalidPlayer")
                    .replace("{player}", args[0]));
            return true;
        }

        UUID targetUUID = target.getUniqueId();
        FactionUser factionTarget = HCF.getPlugin().getUserManager().getUser(targetUUID);
        Deathban deathban = factionTarget.getDeathban();

        if(deathban == null || !deathban.isActive()){
            sender.sendMessage(plugin.getMessages().getString("Commands.Revive.Usage")
                    .replace("{commandLabel}", label));
            sender.sendMessage(ChatColor.RED + target.getName() + " is not death-banned.");
            return true;
        }

        factionTarget.removeDeathban();

        sender.sendMessage(plugin.getMessages().getString("Commands.Revive.Revived")
                .replace("{player}", sender.getName())
                .replace("{target}", target.getName()));

        Command.broadcastCommandMessage(sender, plugin.getMessages().getString("Broadcast.Player-Revived")
                .replace("{player}", sender.getName())
                .replace("{target}", target.getName()), false);

        /*if (sender instanceof PluginMessageRecipient){
            //NOTE: This server needs at least 1 player online.
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Message");
            out.writeUTF(args[1]);

            String serverDisplayName = ChatColor.GREEN + "HCF"; //TODO: Non hard-coded server display name.
            out.writeUTF(ChatColor.GOLD + sender.getName() + " has just revived you from " + serverDisplayName + ChatColor.GOLD + '.');
            ((PluginMessageRecipient) sender).sendPluginMessage(plugin, "RedisBungee", out.toByteArray());
        }*/
        return false;
    }
//
//    @Override
//    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
//        if(args.length != 1){
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
//        return BukkitUtils.getCompletions(args, results);
//    }
}
