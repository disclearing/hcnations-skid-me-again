package com.doctordark.hcf.scoreboard;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.pvpclass.archer.ArcherClass;
import com.doctordark.hcf.scoreboard.api.Board;
import com.doctordark.hcf.user.FactionUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.hcgames.hcfactions.faction.PlayerFaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayerBoard extends Board{

    private final HCF plugin;

    private final Team archerTags;
    private final Team members;
    private final Team neutrals;
    private final Team allies;
    private final Team focus;

    PlayerBoard(HCF plugin, Player player){
        super(plugin, player);
        this.plugin = plugin;

        members = scoreboard.getOrRegisterNewTeam("members");
        members.setPrefix(plugin.getFactions().getConfiguration().getRelationColourTeammate().toString());
        members.setCanSeeFriendlyInvisibles(false);

        neutrals = scoreboard.getOrRegisterNewTeam("neutrals");
        neutrals.setPrefix(plugin.getFactions().getConfiguration().getRelationColourEnemy().toString());

        allies = scoreboard.getOrRegisterNewTeam("allies");
        allies.setPrefix(plugin.getFactions().getConfiguration().getRelationColourAlly().toString());

        archerTags = scoreboard.getOrRegisterNewTeam("archerTags");
        archerTags.setPrefix(ChatColor.DARK_RED.toString());

        focus = scoreboard.getOrRegisterNewTeam("focus");
        focus.setPrefix(ChatColor.LIGHT_PURPLE.toString());
    }

    @Override
    public List<String> provide(Player player){
        List<String> lines = new ArrayList<>();

        if(plugin.getConfiguration().isKitMap()){
            FactionUser user = plugin.getUserManager().getUser(player.getUniqueId());
            lines.addAll(plugin.getMessages().getStringList("scoreboard.stats", user.getKills(), user.getDeaths()));
        }

        if(player.hasPermission("hcf.scoreboard.staff")){
            if(plugin.getStaff().getChatManager().isChatLocked()){
                lines.add(plugin.getMessages().getString("scoreboard.staff.chat.locked"));
            }else if(plugin.getStaff().getSlowChatManager().isEnabled()){
                lines.add(plugin.getMessages().getString("scoreboard.staff.chat.slowed").replace("{time}", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(plugin.getStaff().getSlowChatManager().getDelay()))));
            }

            if(player.hasPermission("hcf.scoreboard.vanish")){
                lines.add(plugin.getMessages().getString("scoreboard.staff.vanish." + String.valueOf(plugin.getStaff().getVanishManager().isVanished(player)).toLowerCase()));
            }
        }

        plugin.getSOTWManager().provideScoreboard(lines);
        plugin.getEotwHandler().provideScoreboard(lines);
        plugin.getTimerManager().provideScoreboard(player, lines);
        plugin.getPvpClassManager().provideScoreboard(player, lines);
        plugin.getTimerManager().getEventTimer().provideScoreboard(lines);

        if(!lines.isEmpty()){
            lines.add(0, plugin.getMessages().getString("scoreboard.format.header"));
            lines.add(plugin.getMessages().getString("scoreboard.format.footer"));
        }

        return lines;
    }

    public void addUpdate(Player target){
        this.addUpdates(Collections.singleton(target));
    }

    public void addUpdates(Iterable<? extends Player> updates){
        new BukkitRunnable(){
            @Override
            public void run(){
                // Lazy load - don't lookup this in every iteration
                PlayerFaction playerFaction = null;
                boolean firstExecute = false;
                //

                for(Player update : updates){
                    if(player.equals(update)){
                        if(!members.hasPlayer(update)){
                            members.addPlayer(update);
                        }
                        continue;
                    }

                    if(!firstExecute){
                        if(plugin.getFactions().getFactionManager().hasFaction(player)){
                            playerFaction = plugin.getFactions().getFactionManager().getPlayerFaction(player);
                        }
                        firstExecute = true;
                    }

                    if(playerFaction != null && playerFaction.isFocused(update.getUniqueId())){
                        focus.addPlayer(update);
                        continue;
                    }

                    PlayerFaction targetFaction = plugin.getFactions().getFactionManager().hasFaction(update) ? plugin.getFactions().getFactionManager().getPlayerFaction(update) : null;

                    if(playerFaction != null && targetFaction != null){
                        if(playerFaction == targetFaction){
                            members.addPlayer(update);
                            continue;
                        }else if(playerFaction.getAllied().contains(targetFaction.getUniqueID())){
                            allies.addPlayer(update);
                            continue;
                        }else if(playerFaction.isFocused(targetFaction.getUniqueID())){
                            focus.addPlayer(update);
                            continue;
                        }
                    }

                    if(((ArcherClass) plugin.getPvpClassManager().getClass("archer")).isMarked(update.getUniqueId())){
                        archerTags.addPlayer(update);
                        continue;
                    }

                    neutrals.addPlayer(update);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

}
