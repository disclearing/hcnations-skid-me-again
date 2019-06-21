package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import com.google.common.collect.ImmutableMap;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import technology.brk.util.JavaUtils;
import technology.brk.util.command.CommandArgument;

import java.util.UUID;

/**
 * Faction argument used to create a new {@link Faction}.
 */
public class FactionCreateArgument extends CommandArgument {

    private final static ImmutableMap<String, UUID> RESTRICTED_NAMES = ImmutableMap.<String, UUID>builder().put("test", UUID.fromString("42e61ded-4f50-46c7-82a8-723bdcda4991")).put("yaml", UUID.fromString("ea50290c-8225-4222-8664-32f2f5070974")).build();

    private final HCFactions plugin;

    public FactionCreateArgument(HCFactions plugin) {
        super("create", "Create a faction.", new String[]{"make", "define"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <factionName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-ConsoleOnly"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Usage").replace("{usage}", getUsage(label)));
            return true;
        }

        String name = args[1];

        if (plugin.getConfiguration().getFactionDisallowedNames().contains(name)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Create-BlockedName").replace("{factionName}", name));
            return true;
        }

        String nameLower = name.toLowerCase();
        if(RESTRICTED_NAMES.containsKey(nameLower) && !((Player) sender).getUniqueId().equals(RESTRICTED_NAMES.get(nameLower))){
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Create-BlockedName").replace("{factionName}", name));
            return true;
        }

        int value = plugin.getConfiguration().getFactionNameMinCharacters();

        if (name.length() < value) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Create-MinimumChars")
                    .replace("{minChars}", String.valueOf(value)));
            return true;
        }

        value = plugin.getConfiguration().getFactionNameMaxCharacters();

        if (name.length() > value) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Create-MaximumChars")
                    .replace("{maxChars}", String.valueOf(value)));
            return true;
        }

        if (!JavaUtils.isAlphanumeric(name)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Create-MustBeAlphanumeric"));
            return true;
        }

        try {
            if(plugin.getFactionManager().getFaction(name) != null){
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Create-NameAlreadyExists")
                        .replace("{factionName}", name));
                return true;
            }
        } catch (NoFactionFoundException e) {}

        try {
            if (plugin.getFactionManager().getPlayerFaction((Player) sender) != null) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Create-AlreadyInFaction"));
                return true;
            }
        } catch (NoFactionFoundException e) {}

        plugin.getFactionManager().createFaction(new PlayerFaction(name), sender);
        return true;
    }
}
