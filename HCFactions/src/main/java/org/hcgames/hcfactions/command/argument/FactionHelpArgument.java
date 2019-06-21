package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.command.FactionExecutor;
import org.hcgames.hcfactions.faction.Faction;
import technology.brk.util.JavaUtils;
import technology.brk.util.command.CommandArgument;

/**
 * Faction argument used to show help on how to use {@link Faction}s.
 */
public class FactionHelpArgument extends CommandArgument {

    private static final int HELP_PER_PAGE = 10;

    private HCFactions plugin;

    private ImmutableMultimap<Integer, String> pages;
    private final FactionExecutor executor;

    public FactionHelpArgument(FactionExecutor executor, HCFactions plugin) {
        super("help", "View help on how to use factions.");
        this.executor = executor;
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            showPage(sender, label, 1);
            return true;
        }

        Integer page = JavaUtils.tryParseInt(args[1]);

        if (page == null) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Invalid-Number")
                    .replace("{number}", args[1]));
            return true;
        }

        showPage(sender, label, page);
        return true;
    }

    private void showPage(CommandSender sender, String label, int pageNumber) {
        // Create the multimap.
        if (pages == null) {
            boolean isPlayer = sender instanceof Player;
            int val = 1;
            int count = 0;
            Multimap<Integer, String> pages = ArrayListMultimap.create();
            for (CommandArgument argument : executor.getArguments()) {
                if (argument == this) continue;

                // Check the permission and if the player can access.
                String permission = argument.getPermission();
                if (permission != null && !sender.hasPermission(permission)) continue;
                if (argument.isPlayerOnly() && !isPlayer) continue;

                count++;
                pages.get(val).add(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Help-MenuEntry")
                        .replace("{commandLabel}", label)
                        .replace("{commandArgument}", argument.getName())
                        .replace("{commandDescription}", argument.getDescription()));
                if (count % HELP_PER_PAGE == 0) {
                    val++;
                }
            }

            // Finally assign it.
            this.pages = ImmutableMultimap.copyOf(pages);
        }

        int totalPageCount = (pages.size() / HELP_PER_PAGE) + 1;

        if (pageNumber < 1) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Help-PageLessThanOne"));
            return;
        }

        if (pageNumber > totalPageCount) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Help-NoMorePages")
                    .replace("{totalPageCount}", String.valueOf(totalPageCount)));
            return;
        }

        sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Help-Header")
                .replace("{currentPageNumber}", String.valueOf(pageNumber))
                .replace("{totalPageCount}", String.valueOf(totalPageCount)));

        for (String message : pages.get(pageNumber)) {
            sender.sendMessage("  " + message);
        }

        sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Help-Footer")
                .replace("{currentPageNumber}", String.valueOf(pageNumber))
                .replace("{totalPageCount}", String.valueOf(totalPageCount))
                .replace("{commandLabel}", label));
    }
}
