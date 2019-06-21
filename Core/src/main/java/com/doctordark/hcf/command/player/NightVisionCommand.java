/*
 * Copyright (C) 2016 SystemUpdate (https://systemupdate.io) All Rights Reserved
 */

package com.doctordark.hcf.command.player;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.user.FactionUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class NightVisionCommand implements CommandExecutor, TabCompleter{

    private PotionEffect NIGHT_VISION_EFFECT = new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true);
    private HCF plugin;

    public NightVisionCommand(HCF plugin){
        this.plugin = plugin;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(!(sender instanceof Player)){
            sender.sendMessage(plugin.getMessages().getString("Error-Messages.PlayerOnly"));
            return true;
        }

        Player player = (Player) sender;
        FactionUser user = plugin.getUserManager().getUser(player.getUniqueId());

        if(user == null){
            return false;
        }

        boolean state;
        if(state = user.hasNightVisionEnabled()){
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }else{
            player.addPotionEffect(NIGHT_VISION_EFFECT);
        }

        user.setNightVisionEnabled(!state);
        sender.sendMessage(plugin.getMessages().getString("Commands.NightVision." + (!state ? "Disabled" : "Enabled")));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        return Collections.emptyList();
    }
}
