package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.ChatChannel;
import org.hcgames.hcfactions.structure.FactionMember;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FactionChatArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionChatArgument(HCFactions plugin) {
        super("chat", "Toggle faction chat only mode on or off.", new String[]{"c"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " [fac|public|ally] [message]";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is only executable by players.");
            return true;
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction;
        try {
            playerFaction = plugin.getFactionManager().getPlayerFaction(player.getUniqueId());
        } catch (NoFactionFoundException e) {
            sender.sendMessage(ChatColor.RED + "You are not in a faction.");
            return true;
        }

        FactionMember member = playerFaction.getMember(player.getUniqueId());
        ChatChannel currentChannel = member.getChatChannel();
        ChatChannel parsed;

        if(args.length >= 2){
            parsed = ChatChannel.parse(args[1], null);

            if(parsed != null && parsed == ChatChannel.OFFICER && member.getRole() == Role.MEMBER){
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Chat-OfficerOnly"));
                return true;
            }
        }else{
            parsed = currentChannel.getRotation();

            if(parsed == ChatChannel.OFFICER && member.getRole() == Role.MEMBER){
                parsed = currentChannel.getRotation();
            }
        }

        if (parsed == null && currentChannel != ChatChannel.PUBLIC) {
            Collection<Player> recipients = playerFaction.getOnlinePlayers();
            if (currentChannel == ChatChannel.ALLIANCE) {
                for (PlayerFaction ally : playerFaction.getAlliedFactions()) {
                    recipients.addAll(ally.getOnlinePlayers());
                }
            }

            String format = String.format(currentChannel.getRawFormat(player), "", HCF.SPACE_JOINER.join(Arrays.copyOfRange(args, 1, args.length)));
            for (Player recipient : recipients) {
                recipient.sendMessage(format);
            }

            // spawn radius, border, allies, minigames,
            return true;
        }

        ChatChannel newChannel = parsed == null ? currentChannel.getRotation() : parsed;

        if(newChannel == ChatChannel.OFFICER && member.getRole() == Role.MEMBER){
            newChannel = newChannel.getRotation();
        }

        member.setChatChannel(newChannel);
        sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Chat-SwitchedMode")
                .replace("{newMode}", newChannel.getDisplayName().toLowerCase()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        ChatChannel[] values = ChatChannel.values();
        List<String> results = new ArrayList<>(values.length);
        for (ChatChannel type : values) {
            results.add(type.getName());
        }

        return results;
    }
}
