/*
 *   COPYRIGHT NOTICE
 *
 *   Copyright (C) 2016, SystemUpdate, <admin@systemupdate.io>.
 *
 *   All rights reserved.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS. IN
 *   NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *   DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 *   OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 *   OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *   Except as contained in this notice, the name of a copyright holder shall not
 *   be used in advertising or otherwise to promote the sale, use or other dealings
 *   in this Software without prior written authorization of the copyright holder.
 */

package com.doctordark.hcf.scoreboard.api;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public abstract class Board{

    protected final Scoreboard scoreboard;
    protected final Player player;

    private final Objective objective;
    private final BukkitTask task;

    private final List<Team> staticTeams;
    private Set<String> currentLines;

    public Board(Plugin plugin, Player player){
        Scoreboard localScoreboard = player.getScoreboard();
        if(localScoreboard.equals(plugin.getServer().getScoreboardManager().getMainScoreboard())){
            player.setScoreboard(localScoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard());
        }

        this.scoreboard = localScoreboard;
        this.player = player;

        Objective finalObjective = scoreboard.getObjective("board");
        if(finalObjective != null){
            finalObjective.unregister();
        }

        finalObjective = scoreboard.registerNewObjective("board", "dummy");
        this.objective = finalObjective;

        staticTeams = new ArrayList<>(15);
        currentLines = new HashSet<>();

        for(int i = 0; 15 > i; i++){
            String teamName = "board-" + i;

            Team team = scoreboard.getTeam(teamName);
            if(team != null){
                team.unregister();
            }

            team = scoreboard.registerNewTeam(teamName);
            team.setDisplayName(new String(new char[]{ChatColor.COLOR_CHAR, ChatColor.values()[i].getChar(), ChatColor.COLOR_CHAR, ChatColor.RESET.getChar()}));
            team.addEntry(team.getDisplayName());

            staticTeams.add(team);
        }

        task = new BukkitRunnable(){

            @Override
            public void run(){
                List<String> lines = provide(player);
                Set<String> newLines = new HashSet<>(lines.size());
                int position = lines.size();

                for(int i = position; i > 0; i--){
                    SidebarEntry entry = new SidebarEntry(lines.get(i - 1));
                    String identifier;
                    Team team;

                    if(entry.getName() != null){
                        team = scoreboard.getOrRegisterNewTeam(entry.getName());
                        team.addEntry(team.getName());
                        identifier = team.getName();
                        newLines.add(identifier);

                        if(!team.getPrefix().equals(entry.getPrefix())){
                            team.setPrefix(entry.getPrefix());
                        }

                        if(!team.getSuffix().equals(entry.getSuffix())){
                            team.setSuffix(entry.getSuffix());
                        }

                    }else{
                        team = scoreboard.getTeam("board-" + i);
                        identifier = team.getDisplayName();
                        newLines.add(team.getName());

                        if(!team.getPrefix().equals(entry.getPrefix())){
                            team.setPrefix(entry.getPrefix());
                        }

                        if(entry.getSuffix() != null){
                            String colouredSuffix = ChatColor.getLastColors(team.getPrefix()) + entry.getSuffix();
                            if(!colouredSuffix.equals(team.getSuffix())){
                                team.setSuffix(colouredSuffix);
                            }
                        }

                    }

                    team.setDisplayName(identifier);
                    objective.getScore(identifier).setScore(i);
                }

                currentLines.removeAll(newLines);
                currentLines.forEach(name -> {
                    Team team = scoreboard.getTeam(name);
                    if(team != null){
                        scoreboard.resetScores(team.getDisplayName());

                        if(!staticTeams.contains(team)){
                            team.unregister();
                        }
                    }
                });

                currentLines = newLines;
            }

        }.runTaskTimerAsynchronously(plugin, 2L, 2L);
    }

    public abstract List<String> provide(Player player);

    void setTitle(String title){
        objective.setDisplayName(title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    void cancelTask(){
        task.cancel();

        staticTeams.forEach(Team::unregister);
        objective.unregister();
    }

}
