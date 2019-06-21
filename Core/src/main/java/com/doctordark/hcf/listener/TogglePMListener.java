package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.UUID;


/**
 * Created by Sam on 29/08/2016.
 */
@RequiredArgsConstructor //TODO: Move to Essentials where it belongs
public class TogglePMListener implements Listener{

    private static final ImmutableList<String> aliases = ImmutableList.of("msg", "m", "w", "t", "pm", "emsg", "epm", "tell", "etell", "whisper", "ewhisper");
    private static final String BYPASS_PERMISSION = "hcf.command.togglepm.bypass";
    public static ArrayList<UUID> pmToggled = new ArrayList<>();
    private final HCF plugin;

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        if(pmToggled.contains(player.getUniqueId())){
            pmToggled.remove(player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event){
        String[] args = event.getMessage().split(" ");
        Player player = event.getPlayer();

        if(args.length > 1){
            String command = args[0].substring(1).toLowerCase();
            Player target;

            if(aliases.contains(command) && ((target = plugin.getServer().getPlayer(args[1])) != null && pmToggled.contains(target.getUniqueId()) && !player.hasPermission(BYPASS_PERMISSION))){
                player.sendMessage(ChatColor.RED + "You cannot message this player as they have PMs toggled.");
                event.setCancelled(true);
            }
        }
    }
}
