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
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import technology.brk.util.command.CommandArgument;
import technology.brk.util.uuid.UUIDHandler;

import java.io.Console;
import java.util.List;
import java.util.UUID;

public class FactionPastFactionsArgument extends CommandArgument{

    private final HCFactions plugin;

    public FactionPastFactionsArgument(HCFactions plugin) {
        super("pastfactions", "See past factions of a user");
        this.plugin = plugin;
    }


    @Override
    public String getUsage(String s){
        return "/f " + s + " pastfactions [name]";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(args.length < 2){
            if(!(sender instanceof Player)){
                sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(commandLabel));
                return true;
            }

            List<String> pastFactions  = HCF.getPlugin().getUserManager().getUser(((Player)sender).getUniqueId()).getPastFactions();
            sender.sendMessage(plugin.getMessages().getString("commands.pastfactions.own", pastFactions.isEmpty() ? "None" : HCFactions.COMMA_JOINER.join(pastFactions)));
        }else{
            Player player = plugin.getServer().getPlayer(args[1]);

            if(player == null){
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    UUID user = UUIDHandler.getUUID(args[1]);

                    if(user == null || !HCF.getPlugin().getUserManager().userExists(user)){
                        sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Pay-UnknownPlayer").replace("{player}", args[1]));
                        return;
                    }

                    List<String> pastFactions = HCF.getPlugin().getUserManager().getUser(user).getPastFactions();
                    sender.sendMessage(plugin.getMessages().getString("commands.pastfactions.other", args[1], pastFactions.isEmpty() ? "None" : HCFactions.COMMA_JOINER.join(pastFactions)));
                });
            }else{
                List<String> pastFactions = HCF.getPlugin().getUserManager().getUser(player.getUniqueId()).getPastFactions();
                sender.sendMessage(plugin.getMessages().getString("commands.pastfactions.other", player.getName(), pastFactions.isEmpty() ? "None" : HCFactions.COMMA_JOINER.join(pastFactions)));
            }
        }

        return true;
    }
}
