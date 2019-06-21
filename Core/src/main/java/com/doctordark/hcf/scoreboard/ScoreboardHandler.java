package com.doctordark.hcf.scoreboard;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.scoreboard.api.BoardHolder;
import com.google.common.collect.Iterables;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.hcgames.hcfactions.event.playerfaction.FactionRelationCreateEvent;
import org.hcgames.hcfactions.event.playerfaction.FactionRelationRemoveEvent;
import org.hcgames.hcfactions.event.playerfaction.PlayerFactionFocusEvent;
import org.hcgames.hcfactions.event.playerfaction.PlayerFactionUnfocusEvent;
import org.hcgames.hcfactions.event.playerfaction.PlayerJoinedFactionEvent;
import org.hcgames.hcfactions.event.playerfaction.PlayerLeftFactionEvent;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ScoreboardHandler extends BoardHolder<PlayerBoard> implements Listener{

    private final HCF plugin;

    public ScoreboardHandler(HCF plugin){
        super(plugin, player -> new PlayerBoard(plugin, player));
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Give all online players a scoreboard.
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        for(Player player : players){
            getBoard(player).addUpdates(players);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        // Update this player for every other online player.
        for(PlayerBoard board : getBoards()){
            board.addUpdate(player);
        }

        getBoard(player).addUpdates(plugin.getServer().getOnlinePlayers());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoinedFaction(PlayerJoinedFactionEvent event){
        Optional<Player> optional = event.getPlayer();
        if(optional.isPresent()){
            Player player = optional.get();

            Collection<Player> players = event.getFaction().getOnlinePlayers();
            getBoard(player).addUpdates(players);
            for(Player target : players){
                getBoard(target).addUpdate(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLeftFaction(PlayerLeftFactionEvent event){
        Optional<Player> optional = event.getPlayer();
        if(optional.isPresent()){
            Player player = optional.get();

            Collection<Player> players = event.getFaction().getOnlinePlayers();
            getBoard(player).addUpdates(players);
            for(Player target : players){
                getBoard(target).addUpdate(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionAllyCreate(FactionRelationCreateEvent event){
        Iterable<Player> updates = Iterables.concat(
                event.getFaction().getOnlinePlayers(),
                event.getTargetFaction().getOnlinePlayers()
        );

        for(PlayerBoard board : getBoards()){
            board.addUpdates(updates);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionAllyRemove(FactionRelationRemoveEvent event){
        Iterable<Player> updates = Iterables.concat(
                event.getFaction().getOnlinePlayers(),
                event.getTargetFaction().getOnlinePlayers()
        );

        for(PlayerBoard board : getBoards()){
            board.addUpdates(updates);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerFactionFocus(PlayerFactionFocusEvent event){
        List<Player> updates;

        if(event.getTarget().isFactionTarget()){
            try{
                PlayerFaction target = plugin.getFactions().getFactionManager().getFaction(event.getTarget().getTarget(), PlayerFaction.class);
                updates = target.getOnlinePlayers();
            }catch(NoFactionFoundException e){
                return;
            }
        }else{
            Player target = plugin.getServer().getPlayer(event.getTarget().getTarget());
            if(target == null){
                return;
            }

            updates = Collections.singletonList(target);
        }

        for(Player onlineMember : event.getFaction().getOnlinePlayers()){
            getBoard(onlineMember).addUpdates(updates);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerFactionUnfocus(PlayerFactionUnfocusEvent event){
        List<Player> updates;

        if(event.getTarget().isFactionTarget()){
            try{
                PlayerFaction target = plugin.getFactions().getFactionManager().getFaction(event.getTarget().getTarget(), PlayerFaction.class);
                updates = target.getOnlinePlayers();
            }catch(NoFactionFoundException e){
                return;
            }
        }else{
            Player target = plugin.getServer().getPlayer(event.getTarget().getTarget());
            if(target == null){
                return;
            }

            updates = Collections.singletonList(target);
        }

        for(Player onlineMember : event.getFaction().getOnlinePlayers()){
            getBoard(onlineMember).addUpdates(updates);
        }
    }

    @Override
    public String getTitle(){
        return null;
    }
}
