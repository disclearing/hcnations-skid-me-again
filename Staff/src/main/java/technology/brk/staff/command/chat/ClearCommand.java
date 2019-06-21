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
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import technology.brk.staff.Staff;

import java.util.Random;

@RequiredArgsConstructor
public class ClearCommand implements CommandExecutor{

    private final static Random random = new Random();
    private final Staff plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]){
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            for(Player i : plugin.getServer().getOnlinePlayers()){
                clear(i, sender.getName());
            }

            plugin.getLogger().info("Chat cleared by " + sender.getName());
        });
        return true;
    }

    private void clear(Player player, String clearer){
        if(plugin.getFreezeManager().isFrozen(player) && !plugin.getConfig().getBoolean("clearchat.ignore_frozen")){
            return;
        }

        if(!player.hasPermission("staff.bypass.clearchat")){
            if(plugin.getConfig().getBoolean("clearchat.fool_tabby")){
                for(int count = 0; count <= plugin.getConfig().getInt("clearchat.lines"); count++){
                    player.sendMessage(StringUtils.repeat(" ", random.nextInt(30)));
                }
            }else{
                for(int count = 0; count <= plugin.getConfig().getInt("clearchat.lines"); count++){
                    player.sendMessage("");
                }
            }
        }

        player.sendMessage(plugin.getMessages().getString("chat.clearchat", clearer));
    }
}
