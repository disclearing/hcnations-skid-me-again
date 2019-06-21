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

package technology.brk.staff.command.chat;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import technology.brk.staff.Staff;

@RequiredArgsConstructor
public class StaffChatCommand implements CommandExecutor{

    private final Staff plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(args.length == 0){
            sender.sendMessage(plugin.getMessages().getString("commands.staffchat.usage"));
            return true;
        }

        String message;
        if(sender instanceof ConsoleCommandSender){
            message = plugin.getMessages().getString("chat.staffchat", "Console", Joiner.on(' ').join(args));
            sender.sendMessage(message);
        }else{
            message = plugin.getMessages().getString("chat.staffchat", sender.getName(), Joiner.on(' ').join(args));
            plugin.getServer().getConsoleSender().sendMessage(message);
        }

        plugin.getServer().broadcast(message, "staff.staffchat.receive");
        return true;
    }

}
