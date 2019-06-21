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

package com.doctordark.hcf.eventgame.sotw;

import com.doctordark.hcf.HCF;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import technology.brk.util.DurationFormatter;
import technology.brk.util.JavaUtils;

import java.util.List;

@RequiredArgsConstructor
public class SOTWCommand implements CommandExecutor, TabCompleter{

    private static final ImmutableList<String> TAB_COMPLETIONS = ImmutableList.of("toggle", "settime", "end");
    private final HCF plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(args.length < 1 || !TAB_COMPLETIONS.contains(args[0].toLowerCase())){
            sender.sendMessage(ChatColor.RED + "Usage: /sotw <toggle | settime | end>");
            return true;
        }

        if(args[0].equalsIgnoreCase("toggle")){
            if(!plugin.getSOTWManager().isActive()){
                sender.sendMessage(ChatColor.RED + "SOTW is not active.");
                return true;
            }

            plugin.getSOTWManager().setPaused(!plugin.getSOTWManager().isPaused());

            if(plugin.getSOTWManager().isPaused()){
                sender.sendMessage(ChatColor.GREEN + "SOTW is now paused.");
            }else{
                sender.sendMessage(ChatColor.GREEN + "SOTW is no longer paused.");
            }

            return true;
        }

        if(args[0].equalsIgnoreCase("settime")){
            if(args.length < 1){
                sender.sendMessage(ChatColor.RED + "Usage: /sotw settime <time>");
                return true;
            }

            long time = JavaUtils.parse(args[1]);

            if(time == -1){
                sender.sendMessage(ChatColor.RED + "Invalid Time: " + args[1]);
                return true;
            }

            plugin.getSOTWManager().setDuration(time);
            sender.sendMessage(ChatColor.GREEN + "SOTW time set to " + DurationFormatter.getRemaining(time, true, true));
            return true;
        }

        if(args[0].equalsIgnoreCase("end")){
            if(!plugin.getSOTWManager().isActive()){
                sender.sendMessage(ChatColor.RED + "SOTW is not active.");
                return true;
            }

            plugin.getSOTWManager().end(false);
            sender.sendMessage(ChatColor.GREEN + "Forcefully ended SOTW.");
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(args.length == 0){
            return TAB_COMPLETIONS;
        }

        return null;
    }
}
