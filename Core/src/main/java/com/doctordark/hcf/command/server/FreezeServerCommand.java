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
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static org.bukkit.Bukkit.getServer;

/**
 * Created by Sam on 28/09/2016.
 */
@RequiredArgsConstructor
public class FreezeServerCommand implements CommandExecutor{

    private final HCF plugin;

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(!sender.hasPermission("freezeserver.freeze")){
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        plugin.setServerFrozen(!plugin.isServerFrozen());
        getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + sender.getName() + " has " + (plugin.isServerFrozen() ? "frozen" : "unfrozen") + " the server.");
        return true;
    }


}
