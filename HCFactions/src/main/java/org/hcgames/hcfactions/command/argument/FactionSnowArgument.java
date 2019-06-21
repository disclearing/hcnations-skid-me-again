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

package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import io.brkmc.breakspigot.BreakConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.ClaimableFaction;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

public class FactionSnowArgument extends CommandArgument{

    private final HCFactions plugin;

    public FactionSnowArgument(HCFactions plugin){
        super("snow", "Toggle snow fall in your faction");
        this.permission = "hcf.command.faction.argument." + getName();
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String s){
        return "/f " + s + " snow";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(!BreakConfig.christmasMap){
            sender.sendMessage(ChatColor.RED + "Requires christmas map break config option.");
            return true;
        }

        ClaimableFaction faction;
        boolean own = false;

        if(args.length < 2){
            if(!(sender instanceof Player)){
                sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(commandLabel) + " [factionName]");
                return true;
            }

            Player player = (Player) sender;
            if(!plugin.getFactionManager().hasFaction(player)){
                sender.sendMessage(ChatColor.RED + "You are not in a faction.");
                return true;
            }

            PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFaction(player);
            if(playerFaction.getMember(player).getRole() == Role.MEMBER){
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Kick-OfficerRequired"));
                return true;
            }

            faction = playerFaction;
            own = true;
        }else if(sender instanceof ConsoleCommandSender || sender.hasPermission(this.permission + ".staff")){
            Faction found;

            try{
                found = plugin.getFactionManager().getFaction(args[1]);
            }catch(NoFactionFoundException e){
                sender.sendMessage(ChatColor.RED + "No faction found by name " + args[1]);
                return true;
            }

            if(!(found instanceof ClaimableFaction)){
                sender.sendMessage(ChatColor.RED + "You cannot toggle snow for that faction.");
                return true;
            }

            faction = (ClaimableFaction) found;
        }else{
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        boolean newState = !faction.isSnowfall();
        faction.setSnowfall(newState);
        sender.sendMessage(plugin.getMessages().getString("commands.snow." + (own ? "own" : "other"), newState ? "enabled" : "disabled", faction.getName()));
        return true;
    }
}
