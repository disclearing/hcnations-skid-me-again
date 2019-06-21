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
import com.doctordark.hcf.user.FactionUser;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.Role;
import technology.brk.util.command.CommandArgument;

public class FactionReviveArgument extends CommandArgument{

    private final HCFactions plugin;

    public FactionReviveArgument(HCFactions plugin){
        super("revive", "Revive a player with faction lives");
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String commandLabel){
        return HCF.getPlugin().getMessages().getString("Commands.Factions.Revive.Usage");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(!(sender instanceof Player)){
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Error-Messages.PlayerOnly"));
            return true;
        }

        Player player = (Player) sender;

        if(args.length < 2){
            sender.sendMessage(getUsage(commandLabel));
            return true;
        }

        PlayerFaction faction;
        try {
            faction = plugin.getFactionManager().getPlayerFaction(player.getUniqueId());
        } catch (NoFactionFoundException e) {
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Error-Messages.NotInFaction"));
            //invalid faction
            return true;
        }

        if(faction.getMember(player).getRole() == Role.MEMBER){
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Factions.Revive.Officer-Required"));
            //officer required
            return true;
        }

        if(faction.getLives() <= 0){
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Factions.Revive.Not-Enough").replace("{player}", args[1]));
            //faction doesn't have enough lives
            return true;
        }

        OfflinePlayer deadPlayer = plugin.getServer().getOfflinePlayer(args[1]);

        if(deadPlayer == null){
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Error-Messages.InvalidPlayer").replace("{player}", args[1]));
            //player not found
            return true;
        }

        FactionUser user;
        if(!HCF.getPlugin().getUserManager().userExists(deadPlayer.getUniqueId()) || ((user = HCF.getPlugin().getUserManager().getUser(deadPlayer.getUniqueId())) == null) || user.getDeathban() == null){
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Factions.Revive.Not-Deathbanned"));
            //not deathbanned
            return true;
        }

        faction.setLives(faction.getLives() - 1);
        user.removeDeathban();
        faction.broadcast(HCF.getPlugin().getMessages().getString("Commands.Factions.Revive.Broadcast").replace("{player}",
                player.getName()).replace("{victim}", deadPlayer.getName()));
        //broadcast
        return false;
    }
}
