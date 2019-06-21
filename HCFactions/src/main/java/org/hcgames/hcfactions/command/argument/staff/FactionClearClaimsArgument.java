package org.hcgames.hcfactions.command.argument.staff;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.faction.ClaimableFaction;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.manager.SearchCallback;
import technology.brk.util.command.CommandArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Faction argument used to set the DTR Regeneration cooldown of {@link Faction}s.
 */
public class FactionClearClaimsArgument extends CommandArgument {

    private final ConversationFactory factory;
    private final HCFactions plugin;

    public FactionClearClaimsArgument(final HCFactions plugin) {
        super("clearclaims", "Clears the claims of a faction.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();

        this.factory = new ConversationFactory(plugin).
                withFirstPrompt(new ClaimClearAllPrompt(plugin)).
                withEscapeSequence("/no").
                withTimeout(10).
                withModality(false).
                withLocalEcho(true);
    }

    @Override
    public String getUsage(String label) {
        return plugin.getMessages().getString("commands.staff.clearclaims.usage", label, getName());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessages().getString("command.error.usage", getUsage(label)));
            return true;
        }

        if (args[1].equalsIgnoreCase("all")) {
            if (!(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(plugin.getMessages().getString("commands.error.console_only"));
                return true;
            }

            Conversable conversable = (Conversable) sender;
            conversable.beginConversation(factory.buildConversation(conversable));
            return true;
        }

        plugin.getFactionManager().advancedSearch(args[1], ClaimableFaction.class, new SearchCallback<ClaimableFaction>() {
            @Override
            public void onSuccess(ClaimableFaction claimableFaction) {
                claimableFaction.removeClaims(claimableFaction.getClaims(), sender);
                if (claimableFaction instanceof PlayerFaction) {
                    ((PlayerFaction) claimableFaction).broadcast(plugin.getMessages().getString("commands.staff.clearclaims.cleared_faction_broadcast", sender.getName()));
                }
                sender.sendMessage(plugin.getMessages().getString("commands.staff.clearclaims.cleared", claimableFaction.getName()));
            }

            @Override
            public void onFail(FailReason reason) {
                sender.sendMessage(plugin.getMessages().getString("commands.error.faction_not_found", args[1]));
            }
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        } else if (args[1].isEmpty()) {
            return null;
        } else {
            Player player = (Player) sender;
            List<String> results = new ArrayList<>(plugin.getFactionManager().getFactionNameMap().keySet());
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (player.canSee(target) && !results.contains(target.getName())) {
                    results.add(target.getName());
                }
            }

            return results;
        }
    }

    private static class ClaimClearAllPrompt extends StringPrompt {

        private final HCFactions plugin;

        public ClaimClearAllPrompt(HCFactions plugin) {
            this.plugin = plugin;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return plugin.getMessages().getString("commands.staff.clearclaims.console_prompt.prompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String string) {
            switch (string.toLowerCase()) {
                case "yes": {
                    for (Faction faction : plugin.getFactionManager().getFactions()) {
                        if (faction instanceof ClaimableFaction) {
                            ClaimableFaction claimableFaction = (ClaimableFaction) faction;
                            claimableFaction.removeClaims(claimableFaction.getClaims(), plugin.getServer().getConsoleSender());
                        }
                    }

                    Conversable conversable = context.getForWhom();
                    plugin.getServer().broadcastMessage(plugin.getMessages().getString("commands.staff.clearclaims.console_prompt.cleared",
                            (conversable instanceof CommandSender ? " by " + ((CommandSender) conversable).getName() : "")));

                    return Prompt.END_OF_CONVERSATION;
                }
                case "no": {
                    context.getForWhom().sendRawMessage(plugin.getMessages().getString("commands.staff.clearclaims.console_prompt.cancelled"));
                    return Prompt.END_OF_CONVERSATION;
                }
                default: {
                    context.getForWhom().sendRawMessage(plugin.getMessages().getString("commands.staff.clearclaims.console_prompt.cancelled_unknown"));
                    return Prompt.END_OF_CONVERSATION;
                }
            }
        }
    }
}
