package com.doctordark.hcf.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Copyright SystemUpdate (https://systemupdate.io) to present.
 * Please see included licence file for licensing terms.
 * File created on 17/03/2016.
 */
public class SyntaxBlocker implements Listener{

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event){
        String[] withArguments = event.getMessage().split(" ");
        String command = withArguments[0].substring(1);

        if(command.contains(":") && !event.getPlayer().hasPermission("hcf.bypass.syntaxblockee")){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThis syntax is blocked."));
        }
    }
}
