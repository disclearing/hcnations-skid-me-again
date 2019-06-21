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

package technology.brk.staff.command;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import technology.brk.staff.Staff;
import technology.brk.staff.player.StaffMember;

import java.util.Collection;
import java.util.Iterator;

@RequiredArgsConstructor @Deprecated
public class CommandsCommand implements CommandExecutor{

    private final Staff plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]){
        if(sender.hasPermission("staff.command.command")){
            if(!(args.length >= 1)){
                sender.sendMessage(plugin.getMessages().getString("Commands-Commands-InvalidUsage"));
                return true;
            }

            Player bukkitPlayer = plugin.getServer().getPlayer(args[0]);
            StaffMember player;

            if(bukkitPlayer == null || ((player = plugin.getPlayerManager().getPlayer(bukkitPlayer)) == null)){
                sender.sendMessage(plugin.getMessages().getString("Commands-Commands-PlayerNotFound")
                        .replace("{player}", args[0]));
                return true;
            }

            Collection<String> commands = player.getCommandsLog().values();

            if(commands.isEmpty()){
                sender.sendMessage(plugin.getMessages().getString("Commands-Commands-NoCommands")
                        .replace("{player}", args[0]));
                return true;
            }

            int pageNumber = 1;

            if(args.length >= 2){
                try{
                    pageNumber = Integer.valueOf(args[1]);
                }catch(NumberFormatException exception){
                    sender.sendMessage(plugin.getMessages().getString("Error-Messages.Invalid-Number")
                            .replace("{number}", args[1]));
                    return true;
                }
            }

            int maxPerPage = plugin.getConfig().getInt("CommandsLog.Max-Per-Page");
            int totalPages = 1;

            Multimap<Integer, String> pages = ArrayListMultimap.create();
            Iterator<String> iter = commands.iterator();

            while (iter.hasNext()){
                pages.get(totalPages).add(iter.next());

                if((pages.get(totalPages).size() >= maxPerPage) && iter.hasNext()){
                    totalPages++;
                }
            }

            if(pageNumber < 1 || pageNumber > totalPages){
                sender.sendMessage(plugin.getMessages().getString("Commands-Commands-NoSuchPage")
                        .replace("{page}", String.valueOf(pageNumber)));
                return true;
            }

            sender.sendMessage(plugin.getMessages().getString("Commands-Commands-Header")
                    .replace("{player}", bukkitPlayer.getName())
                    .replace("{currentPage}", String.valueOf(pageNumber))
                    .replace("{totalPages}", String.valueOf(totalPages)));

            for(String i : pages.get(pageNumber)){
                sender.sendMessage(plugin.getMessages().getString("Commands-Commands-Item")
                        .replace("{command}", i));
            }

            sender.sendMessage(plugin.getMessages().getString("Commands-Commands-Footer")
                    .replace("{player}", bukkitPlayer.getName())
                    .replace("{currentPage}", String.valueOf(pageNumber))
                    .replace("{totalPages}", String.valueOf(totalPages)));
        }else{
            sender.sendMessage(plugin.getMessages().getString("Messages-NoPermission"));
        }
        return false;
    }
}
