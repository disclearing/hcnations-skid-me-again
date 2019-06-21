package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.Relation;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

import java.util.Set;

/**
 * Faction argument used to invite players into {@link Faction}s.
 */
public class FactionInviteArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionInviteArgument(HCFactions plugin) {
        super("invite", "Invite a player to the faction.");
        this.plugin = plugin;
        this.aliases = new String[]{"inv", "invitemember", "inviteplayer"};
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <playerName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Invite-PlayersOnlyCMD"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Usage").replace("{usage}", getUsage(label)));
            return true;
        }

        if(args[1].length() > 17){
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Invite-InvalidUsername")
                    .replace("{username}", args[1]));
            return true;
        }

        Player invitee = plugin.getServer().getPlayer(args[1]);

        if(invitee == null){
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Pay-UnknownPlayer").replace("{player}", args[1]));
            return true;
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction;
        try {
            playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        } catch (NoFactionFoundException e) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Global-NotInFaction"));
            return true;
        }

        if (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Invite-OfficerRequired"));
            return true;
        }

        Set<String> invitedPlayerNames = playerFaction.getInvitedPlayerNames();
        String name = args[1];

        if (playerFaction.findMember(name) != null) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Invite-AlreadyInFaction")
                    .replace("{player}", name));
            return true;
        }

        if (!HCF.getPlugin().getConfiguration().isKitMap() && !HCF.getPlugin().getEotwHandler().isEndOfTheWorld() && playerFaction.isRaidable()) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Invite-NoInviteWhileRaidable"));
            return true;
        }

        if (!invitedPlayerNames.add(name.toLowerCase())) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Invite-AlreadyInvited")
                    .replace("{player}", name));
            return true;
        }

        Player target = Bukkit.getPlayer(name);
        if (target != null) {
            name = target.getName(); // fix casing.

            FancyMessage message = new FancyMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Invite-InviteReceived")
                    .replace("{relationColour}", Relation.ENEMY.toChatColour() + "")
                    .replace("{sender}", sender.getName())
                    .replace("{factionName}", playerFaction.getName()));
            message.command("/" + label + " accept " + playerFaction.getName());
            message.send(target);
            /*net.md_5.bungee.api.ChatColor enemyRelationColor = toBungee(Relation.ENEMY.toChatColour());
            ComponentBuilder builder = new ComponentBuilder(sender.getName()).color(enemyRelationColor);
            builder.append(" has invited you to join ", ComponentBuilder.FormatRetention.NONE).color(net.md_5.bungee.api.ChatColor.YELLOW);
            builder.append(playerFaction.getName()).color(enemyRelationColor).append(". ", ComponentBuilder.FormatRetention.NONE).color(net.md_5.bungee.api.ChatColor.YELLOW);
            builder.append("Click here").color(net.md_5.bungee.api.ChatColor.GREEN).
                    event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + label + " accept " + playerFaction.getName())).
                    event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to join ").color(net.md_5.bungee.api.ChatColor.AQUA).
                            append(playerFaction.getName(), ComponentBuilder.FormatRetention.NONE).color(enemyRelationColor).
                            append(".", ComponentBuilder.FormatRetention.NONE).color(net.md_5.bungee.api.ChatColor.AQUA).create()));
            builder.append(" to accept this invitation.", ComponentBuilder.FormatRetention.NONE).color(net.md_5.bungee.api.ChatColor.YELLOW);
            target.spigot().sendMessage(builder.create());*/

        }

        playerFaction.broadcast(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Invite-InviteBroadcast")
                .replace("{player}", sender.getName()) //I don't get this, surly it should get the relation, not assume?
                .replace("{invitee}", name));
       // playerFaction.broadcast(Relation.MEMBER.toChatColour() + sender.getName() + ChatColor.YELLOW + " has invited " + Relation.ENEMY.toChatColour() + name + ChatColor.YELLOW + " into the faction.");
        return true;
    }

    /*@Override //fixme
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
        if (playerFaction == null || (playerFaction.getMember(player.getUniqueId()).getRole() == Role.MEMBER)) {
            return Collections.emptyList();
        }

        List<String> results = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player.canSee(target) && !results.contains(target.getName())) {
                if (playerFaction != plugin.getFactionManager().getPlayerFaction(target.getUniqueId())) {
                    results.add(target.getName());
                }
            }
        }

        return results;
    }*/
}
