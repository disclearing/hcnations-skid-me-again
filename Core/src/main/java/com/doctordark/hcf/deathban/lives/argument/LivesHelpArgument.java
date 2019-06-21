package com.doctordark.hcf.deathban.lives.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.deathban.lives.LivesExecutor;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import technology.brk.util.JavaUtils;
import technology.brk.util.command.CommandArgument;

/**
 * Copyright SystemUpdate (https://systemupdate.io) to present.
 * Please see included licence file for licensing terms.
 * File created on 01/03/2016.
 */
public class LivesHelpArgument extends CommandArgument{

    private static final int HELP_PER_PAGE = 10;
    private final LivesExecutor executor;
    private ImmutableMultimap<Integer, String> pages;

    public LivesHelpArgument(LivesExecutor executor){
        super("help", "Help for the lives command.");
        this.executor = executor;
    }

    @Override
    public String getUsage(String label){
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(args.length < 2){
            showPage(sender, label, 1);
            return true;
        }

        Integer page = JavaUtils.tryParseInt(args[1]);

        if(page == null){
            sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a valid number.");
            return true;
        }

        showPage(sender, label, page);
        return true;
    }

    private void showPage(CommandSender sender, String label, int pageNumber){
        // Create the multimap.
        if(pages == null){
            boolean isPlayer = sender instanceof Player;
            int val = 1;
            int count = 0;
            Multimap<Integer, String> pages = ArrayListMultimap.create();
            for(CommandArgument argument : executor.getArguments()){
                if(argument == this) continue;

                // Check the permission and if the player can access.
                String permission = argument.getPermission();
                if(permission != null && !sender.hasPermission(permission)) continue;
                if(argument.isPlayerOnly() && !isPlayer) continue;

                count++;
                pages.get(val).add(HCF.getPlugin().getMessagesOld().getString("Commands-Lives-Help-MenuEntry")
                        .replace("{commandLabel}", label)
                        .replace("{commandArgument}", argument.getName())
                        .replace("{commandDescription}", argument.getDescription()));
                if(count % HELP_PER_PAGE == 0){
                    val++;
                }
            }

            // Finally assign it.
            this.pages = ImmutableMultimap.copyOf(pages);
        }

        int totalPageCount = (pages.size() / HELP_PER_PAGE) + 1;

        if(pageNumber < 1){
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Lives-Help-PageLessThanOne"));
            return;
        }

        if(pageNumber > totalPageCount){
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Lives-Help-NoMorePages")
                    .replace("{totalPageCount}", String.valueOf(totalPageCount)));
            return;
        }

        sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Lives-Help-Header")
                .replace("{currentPageNumber}", String.valueOf(pageNumber))
                .replace("{totalPageCount}", String.valueOf(totalPageCount)));

        for(String message : pages.get(pageNumber)){
            sender.sendMessage("  " + message);
        }

        sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Lives-Help-Footer")
                .replace("{currentPageNumber}", String.valueOf(pageNumber))
                .replace("{totalPageCount}", String.valueOf(totalPageCount))
                .replace("{commandLabel}", label));
    }
}
