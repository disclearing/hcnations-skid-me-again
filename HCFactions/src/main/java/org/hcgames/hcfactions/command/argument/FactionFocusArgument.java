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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.focus.FocusTarget;
import technology.brk.util.command.CommandArgument;

public class FactionFocusArgument extends CommandArgument{

    private final HCFactions plugin;

    public FactionFocusArgument(HCFactions plugin){
        super("focus", "Focus on a player or argument", new String[]{"unfocus"});
        plugin.getCommand("focus").setExecutor(new FocusForwarder());
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String s) {
        return HCF.getPlugin().getMessages().getString("Commands.Factions.Focus.Usage");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(!(sender instanceof Player)){
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Error-Messages.PlayerOnly"));
            //player only
            return true;
        }

        Player player = (Player) sender;

        if(args.length < 2){
            sender.sendMessage(getUsage(commandLabel));
            return true;
        }

        PlayerFaction faction;
        try{
            faction = plugin.getFactionManager().getPlayerFaction(player);
        }catch(NoFactionFoundException e){
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Error-Messages.NotInFaction"));
            //not in faction
            return true;
        }

        String name = args[1];

        Player targetPlayer = plugin.getServer().getPlayer(name);
        Faction targetFaction;

        if(targetPlayer == null){
            try{
                targetFaction = plugin.getFactionManager().getFaction(name);
            }catch(NoFactionFoundException e){
                sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Factions.Focus.Error.NotFound").replace("{name}", name));
                //player or faction not found
                return true;
            }

            if(!(targetFaction instanceof PlayerFaction)){
                sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Factions.Focus.Error.NotPlayerFaction"));
               //faction isn't player faction
                return true;
            }

            handleFactionFocus(sender, faction, (PlayerFaction) targetFaction);
            return true;
        }

        handleFactionFocus(sender, faction, targetPlayer);
        return true;
    }

    private void handleFactionFocus(CommandSender sender, PlayerFaction current, PlayerFaction target){
        if(current.isFocused(target.getUniqueID())){
            plugin.getFactionManager().getFocusHandler().unfocus(current.fmk(target.getUniqueID()));
            current.broadcast(HCF.getPlugin().getMessages().getString("Commands.Factions.Focus.UnFocus.Faction")
                    .replace("{player}", sender.getName()).replace("{focusedFaction}", target.getName()));
            return;
        }

        plugin.getFactionManager().getFocusHandler().focus(current, new FocusTarget(plugin, current, target));
        current.broadcast(HCF.getPlugin().getMessages().getString("Commands.Factions.Focus.Focus.Faction")
                .replace("{player}", sender.getName()).replace("{focusedFaction}", target.getName()));
        //focused
    }

    private void handleFactionFocus(CommandSender sender, PlayerFaction current, Player target){
        if(current.isFocused(target.getUniqueId())){
            plugin.getFactionManager().getFocusHandler().unfocus(current.fmk(target.getUniqueId()));
            current.broadcast(HCF.getPlugin().getMessages().getString("Commands.Factions.Focus.UnFocus.Player")
                    .replace("{player}", sender.getName()).replace("{focusedPlayer}", target.getName()));
            //already focused
            return;
        }

        plugin.getFactionManager().getFocusHandler().focus(current, new FocusTarget(plugin, current, target));
        current.broadcast(HCF.getPlugin().getMessages().getString("Commands.Factions.Focus.Focus.Player")
                .replace("{player}", sender.getName()).replace("{focusedPlayer}", target.getName()));
    }

    private class FocusForwarder implements CommandExecutor{

        @Override
        public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
            if(!(sender instanceof Player)){
                sender.sendMessage(HCF.getPlugin().getMessages().getString("Error-Messages.PlayerOnly"));
                //player only
                return true;
            }

            plugin.getServer().dispatchCommand(sender, "f focus " + (args.length > 0 ? args[0] : ""));
            return true;
        }
    }
}
