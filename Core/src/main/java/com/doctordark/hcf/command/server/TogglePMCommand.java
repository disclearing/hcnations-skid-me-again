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

package com.doctordark.hcf.command.server;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.listener.TogglePMListener;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Sam on 29/08/2016.
 */
@RequiredArgsConstructor
public class TogglePMCommand implements CommandExecutor{

    private final HCF plugin;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){


        if(!(sender instanceof Player)){
            sender.sendMessage("This command is only valid through players.");
            return true;
        }

        Player player = (Player) sender;
        if(cmd.getName().equalsIgnoreCase("togglepm")){
            if(!TogglePMListener.pmToggled.contains(player.getUniqueId())){
                TogglePMListener.pmToggled.add(player.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "You have toggled PMs.");
            }else{
                TogglePMListener.pmToggled.remove(player.getUniqueId());
                sender.sendMessage(ChatColor.RED + "You have toggled PMs.");
            }
        }
        return true;
    }
}
