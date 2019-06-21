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

package technology.brk.staff.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;
import technology.brk.staff.Staff;

@RequiredArgsConstructor @Deprecated
public class PlayerRespawnListener implements Listener {

    private final Staff plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event){
        if(plugin.getFreezeManager().isFrozen(event.getPlayer())){//TODO: Freeze code?!
            event.getPlayer().setWalkSpeed(0.2F);
            event.getPlayer().removePotionEffect(PotionEffectType.JUMP);
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRespawn2(PlayerRespawnEvent event){
        if(plugin.getStaffMode().inStaffMode(event.getPlayer())){
            plugin.getStaffMode().disableStaffMode(event.getPlayer());
            plugin.getStaffMode().enableStaffMode(event.getPlayer());
        }
    }
}
