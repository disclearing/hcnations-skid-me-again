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

package com.doctordark.hcf.command.player;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.listener.OreCountListener;
import com.doctordark.hcf.user.FactionUser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import technology.brk.util.uuid.UUIDHandler;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class OresCommand implements CommandExecutor{

    private final HCF plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(args.length == 1){
            String username = args[0];

            if(UUIDHandler.isLoaded(username)){
                printOres(sender, username, UUIDHandler.getUUID(username));
            }else{
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> printOres(sender, username, UUIDHandler.getUUID(username)));
            }
        }else if(sender instanceof Player){
            printOres(sender, sender.getName(), ((Player) sender).getUniqueId());
        }else{
            sender.sendMessage(ChatColor.RED + "Usage: /ores <username>");
        }
        return true;
    }

    private void printOres(CommandSender sender, String username, UUID userUUID){
        if(userUUID == null || !plugin.getUserManager().userExists(userUUID)){
            sender.sendMessage(plugin.getMessages().getString("Error-Messages.InvalidPlayer").replace("{player}", username));
            return;
        }

        FactionUser user = plugin.getServer().isPrimaryThread() ? plugin.getUserManager().getUser(userUUID) : plugin.getUserManager().getUserAsync(userUUID);
        sender.sendMessage(plugin.getMessages().getString("Commands.Ores." + (sender.getName().equalsIgnoreCase(username)
                ? "Own" : "Other")).replace("{player}", username));

        for(Map.Entry<Material, ChatColor> entry : OreCountListener.ORES.entries()){
            if(entry.getKey().equals(Material.GLOWING_REDSTONE_ORE)){
                continue;
            }

            sender.sendMessage(entry.getValue() + WordUtils.capitalizeFully(entry.getKey().name().replace("_", " ")) +
                    ':' + ChatColor.RED + ' ' + ChatColor.WHITE + user.getOreCount(entry.getKey()));
        }
    }
}
