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

package com.doctordark.hcf.invrestore;

import com.doctordark.hcf.HCF;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import technology.brk.util.uuid.UUIDHandler;

import java.util.UUID;

@RequiredArgsConstructor
public class InvCommand implements CommandExecutor{

    private final HCF plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(args.length < 1){
            sender.sendMessage(plugin.getMessages().getString("Commands.Inv.Usage"));
            return true;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID playerUUID = UUIDHandler.getUUID(args[0]);

            if(playerUUID == null){
                sender.sendMessage(plugin.getMessages().getString("Error-Messages.InvalidPlayer").replace("{player}", args[0]));
                return;
            }

            InventoryState state = plugin.getInvManager().getState(playerUUID);
            if(state == null){
                sender.sendMessage(plugin.getMessages().getString("Commands.Inv.No-State").replace("{player}", args[0]));
                return;
            }

            Player player = plugin.getServer().getPlayer(playerUUID);
            if(player != null && player.isOnline()){
                plugin.getInvManager().restore(player, state);

                sender.sendMessage(plugin.getMessages().getString("Commands.Inv.Restored.Staff").replace("{player}", player.getName()));
                player.sendMessage(plugin.getMessages().getString("Commands.Inv.Restored.User").replace("{player}", (sender instanceof ConsoleCommandSender ? "Console" : sender.getName())));

                BukkitCommand.broadcastCommandMessage(sender, "Restored inventory of " + player.getName(), false);
                return;
            }

            boolean newState = !plugin.getInvManager().shouldRestoreOnJoin(playerUUID);
            plugin.getInvManager().setShouldRestoreOnJoin(playerUUID, newState);

            sender.sendMessage(plugin.getMessages().getString("Commands.Inv.Pending." + (newState ? "Enabled" : "Disabled")).replace("{player}", args[0]));
            BukkitCommand.broadcastCommandMessage(sender, "Has " + (newState ? "enabled" : "disabled") + " inventory restore on join for " + args[0], false);

        });
        return true;
    }
}
