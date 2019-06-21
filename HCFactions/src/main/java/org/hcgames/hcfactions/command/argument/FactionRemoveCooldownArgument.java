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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import technology.brk.util.command.CommandArgument;

public class FactionRemoveCooldownArgument extends CommandArgument{

    private final HCFactions plugin;

    public FactionRemoveCooldownArgument(HCFactions plugin){
        super("removecooldown", "Removes a faction cool down for a player.", "hcf.command.faction.argument.removecooldown");
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String s){
        return HCF.getPlugin().getMessages().getString("Commands.Factions.RemoveCooldown.Usage");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(args.length < 3){
            sender.sendMessage(getUsage(commandLabel));
            return true;
        }

        Player player = plugin.getServer().getPlayer(args[1]);

        if(player == null){
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Error-Messages.InvalidPlayer").replace("{player}", args[1]));
            return true;
        }

        Faction faction;
        try {
            faction = plugin.getFactionManager().getFaction(args[2]);
        } catch (NoFactionFoundException e) {
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Error-Messages.InvalidFaction").replace("{faction}", args[2]));
            return true;
        }

        if(!(faction instanceof PlayerFaction)){
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Factions.RemoveCooldown.InvalidFactionType").replace("{faction}", faction.getName()));
            return true;
        }

        PlayerFaction pFaction = (PlayerFaction) faction;

        if(!pFaction.hasCooldown(player.getUniqueId())){
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Factions.RemoveCooldown.NotOnCooldown").replace("{player}", player.getName()));
            return true;
        }

        pFaction.removeCooldown(player.getUniqueId());
        sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Factions.RemoveCooldown.CooldownRemoved").replace("{player}", player.getName()).replace("{faction}", faction.getName()));
        return false;
    }
}
