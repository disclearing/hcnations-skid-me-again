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

package technology.brk.staff.listener;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import technology.brk.staff.Staff;
import technology.brk.staff.player.StaffMember;
import technology.brk.staff.util.Permissions;

@RequiredArgsConstructor
public class StaffListener implements Listener{

    private final Staff plugin;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocessListen(PlayerCommandPreprocessEvent event){
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {//TODO: ASYNC?!
            if(event.getPlayer().hasPermission(Permissions.BYPASS_COMMAND_LOGGING)){
                return;
            }

            StaffMember player = plugin.getPlayerManager().getPlayer(event.getPlayer().getUniqueId());

            if(player == null){
                return;
            }

            String command = event.getMessage();

            if(command.startsWith("/")){
                command = command.substring(1);
            }

            player.logCommand(command);
        });
    }

}
