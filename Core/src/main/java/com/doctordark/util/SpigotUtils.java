package com.doctordark.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Deprecated
public class SpigotUtils{

    @Deprecated
    public static void broadcastMessage(Function<CommandSender, String> function){
        List<CommandSender> recipients = new ArrayList<>(Bukkit.getOnlinePlayers());
        recipients.add(Bukkit.getConsoleSender());
        for(CommandSender recipient : recipients){
            recipient.sendMessage(function.apply(recipient));
        }
    }

}
