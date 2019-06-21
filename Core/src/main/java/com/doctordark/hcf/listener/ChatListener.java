package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.ChatChannel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener implements Listener{

    private static final Pattern
            FACTION_TAG_REPLACER = Pattern.compile("\\{FACTION}", Pattern.LITERAL),
            DISPLAY_NAME_REPLACER = Pattern.compile("\\{DISPLAYNAME}", Pattern.LITERAL),
            MESSAGE_REPLACER = Pattern.compile("\\{MESSAGE}", Pattern.LITERAL);

    private final HCF plugin;
    private Essentials essentials;

    public ChatListener(HCF plugin){
        this.plugin = plugin;

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        Plugin essentialsPlugin = pluginManager.getPlugin("Essentials");
        if(essentialsPlugin instanceof Essentials && essentialsPlugin.isEnabled()){
            this.essentials = (Essentials) essentialsPlugin;
        }
    }

    /**
     * Checks if a message should be posted in {@link ChatChannel#PUBLIC}.
     *
     * @param input the message to check
     * @return true if the message should be posted in {@link ChatChannel#PUBLIC}
     */
    public static boolean isGlobalChannel(String input){
        int length = input.length();

        if(length > 1 && input.startsWith("!")){
            for(int i = 1; i < length; i++){
                char character = input.charAt(i);

                // Ignore whitespace to prevent blank messages
                if(Character.isWhitespace(character)){
                    continue;
                }

                // Player is faking a command
                return character != '/';
            }
        }

        return false;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event){
        String message = event.getMessage();
        Player player = event.getPlayer();

        PlayerFaction playerFaction = plugin.getFactions().getFactionManager().hasFaction(player) ? plugin.getFactions().getFactionManager().getPlayerFaction(player) : null;
        String displayName = player.getDisplayName();
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        String defaultFormat = this.getChatFormat(player, playerFaction, console);

        // Handle the custom messaging here.
        event.setFormat(defaultFormat);
        event.setCancelled(true);
        console.sendMessage(String.format(defaultFormat, displayName, message));
        for(Player recipient : event.getRecipients()){
            recipient.sendMessage(String.format(this.getChatFormat(player, playerFaction, recipient), displayName, message));
        }
    }

    private String getChatFormat(Player player, PlayerFaction playerFaction, CommandSender viewer){
        String factionTag = (playerFaction == null ? ChatColor.RED.toString() + '*' : playerFaction.getFormattedName(viewer));
        String result;

        if(this.essentials != null){
            User user = this.essentials.getUser(player);
            result = this.essentials.getSettings().getChatFormat(user.getGroup());

            result = FACTION_TAG_REPLACER.matcher(result).replaceAll(Matcher.quoteReplacement(factionTag));
            result = DISPLAY_NAME_REPLACER.matcher(result).replaceAll(Matcher.quoteReplacement(user.getDisplayName()));
            result = MESSAGE_REPLACER.matcher(result).replaceAll(Matcher.quoteReplacement("%2$s"));
        }else{
            result = ChatColor.GOLD + "[" + factionTag + ChatColor.GOLD + "] %1$s" + ChatColor.GRAY + ": " + ChatColor.WHITE + "%2$s";
        }

        return result;
    }
}
