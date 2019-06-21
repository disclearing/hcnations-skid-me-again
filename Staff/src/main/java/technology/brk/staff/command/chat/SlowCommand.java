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

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import technology.brk.staff.Staff;

@RequiredArgsConstructor
public class SlowCommand implements CommandExecutor{

    private final Staff plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
        int delay = 0;

        if(args.length >= 1){
            try{
                delay = Integer.valueOf(args[0]);
            }catch(NumberFormatException exception){
                sender.sendMessage(plugin.getMessages().getString("error.invalid_number", args[0]));
                return true;
            }

            if(delay <= 0){
                sender.sendMessage(plugin.getMessages().getString("error.number_too_small", delay));
                return true;
            }
        }

        if(!plugin.getSlowChatManager().isEnabled() && delay == 0){
            sender.sendMessage(plugin.getMessages().getString("slow.usage"));
            return true;
        }

        if(plugin.getSlowChatManager().isEnabled() && delay == 0){
            plugin.getSlowChatManager().setEnabled(false);
            plugin.getServer().broadcastMessage(plugin.getMessages().getString("slow.disabled", sender.getName()));
            return true;
        }

        plugin.getSlowChatManager().setDelay(delay);
        if(!plugin.getSlowChatManager().isEnabled()){
            plugin.getSlowChatManager().setEnabled(true);
            plugin.getServer().broadcastMessage(plugin.getMessages().getString("slow.enabled", sender.getName()));
        }

        sender.sendMessage(plugin.getMessages().getString("slow.set", delay, delay == 1 ? "" : "s"));
        return true;
    }
}
