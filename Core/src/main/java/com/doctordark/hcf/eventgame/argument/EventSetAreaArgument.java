package com.doctordark.hcf.eventgame.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.faction.EventFaction;
import com.doctordark.hcf.eventgame.faction.FuryFaction;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import technology.brk.util.command.CommandArgument;
import technology.brk.util.cuboid.Cuboid;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An {@link CommandArgument} used for setting the area of an {@link EventFaction}.
 */
public class EventSetAreaArgument extends CommandArgument{

    private static final int MIN_EVENT_CLAIM_AREA = 8;

    private final HCF plugin;

    public EventSetAreaArgument(HCF plugin){
        super("setarea", "Sets the area of an event");
        this.plugin = plugin;
        this.aliases = new String[]{"setclaim", "setclaimarea", "setland"};
        this.permission = "hcf.command.event.argument." + getName();
    }

    @Override
    public String getUsage(String label){
        return '/' + label + ' ' + getName() + " <kothName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players can set event claim areas");
            return true;
        }

        if(args.length < 2){
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        WorldEditPlugin worldEditPlugin = plugin.getWorldEdit();

        if(worldEditPlugin == null){
            sender.sendMessage(ChatColor.RED + "WorldEdit must be installed to set event claim areas.");
            return true;
        }

        Player player = (Player) sender;
        Selection selection = worldEditPlugin.getSelection(player);

        if(selection == null){
            sender.sendMessage(ChatColor.RED + "You must make a WorldEdit selection to do this.");
            return true;
        }

        if(selection.getWidth() < MIN_EVENT_CLAIM_AREA || selection.getLength() < MIN_EVENT_CLAIM_AREA){
            sender.sendMessage(ChatColor.RED + "Event claim areas must be at least " + MIN_EVENT_CLAIM_AREA + 'x' + MIN_EVENT_CLAIM_AREA + '.');
            return true;
        }

        Faction faction;
        try{
            faction = plugin.getFactions().getFactionManager().getFaction(args[1]);
        }catch(NoFactionFoundException e){
            sender.sendMessage(ChatColor.RED + "There is not an event faction named '" + args[1] + "'.");
            return true;
        }

        if(!(faction instanceof EventFaction)){
            sender.sendMessage(ChatColor.RED + "There is not an event faction named '" + args[1] + "'.");
            return true;
        }

        if(faction instanceof FuryFaction){
            if(args.length < 3){
                sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label) + " <overworld|nether|end>");
                return true;
            }

            FuryFaction.FuryZone furyZone = FuryFaction.FuryZone.getByName(args[2]);

            if(furyZone == null){
                sender.sendMessage(ChatColor.RED + "Invalid capture zone");
                return true;
            }

            FuryFaction furyFaction = (FuryFaction) faction;
            furyFaction.setClaim(new Cuboid(selection.getMinimumPoint(), selection.getMaximumPoint()), player, furyZone);

            sender.sendMessage(ChatColor.YELLOW + "Updated the claim for event " + faction.getName() + ChatColor.YELLOW + '(' + furyZone.getDisplayName() + ChatColor.YELLOW + ").");
            return true;
        }

        ((EventFaction) faction).setClaim(new Cuboid(selection.getMinimumPoint(), selection.getMaximumPoint()), player);

        sender.sendMessage(ChatColor.YELLOW + "Updated the claim for event " + faction.getName() + ChatColor.YELLOW + '.');
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        if(args.length != 2){
            return Collections.emptyList();
        }

        return plugin.getFactions().getFactionManager().getFactions().stream().filter(faction -> faction instanceof EventFaction).map(Faction::getName).collect(Collectors.toList());
    }
}
