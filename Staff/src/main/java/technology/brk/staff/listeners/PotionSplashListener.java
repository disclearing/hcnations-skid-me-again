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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import technology.brk.staff.Staff;

import java.util.Collection;
import java.util.HashSet;

@RequiredArgsConstructor @Deprecated
public class PotionSplashListener implements Listener{

    private final Staff plugin;

    //TODO: Implement everywhere
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPotionSplash(PotionSplashEvent event) {
        if(event.getEntity().getShooter() instanceof Player){
            Player player = (Player) event.getEntity().getShooter();

            if(plugin.getFreezeManager().isFrozen(player)){
                event.setCancelled(true);
                return;
            }

            if(plugin.getVanishManager().isVanished(player) & !player.hasPermission("staff.bypass.vanish.interact")){
                player.sendMessage(plugin.getMessages().getString("Interaction-Vanish-PotionThrow"));
                event.setCancelled(true);
                return;
            }

            if(plugin.getStaffMode().inStaffMode(player) && !player.hasPermission("staff.bypass.staffmode.interact")){
                player.sendMessage(plugin.getMessages().getString("Interaction-StaffMode-PotionThrow"));
                event.setCancelled(true);
                return;
            }
        }

        Collection<LivingEntity> toRemove = new HashSet<>();

        event.getAffectedEntities().stream().filter(livingEntity -> livingEntity instanceof Player).forEach(livingEntity -> {
            Player player = (Player) livingEntity;

            if (plugin.getVanishManager().isVanished(player) || plugin.getFreezeManager().isFrozen(player) || plugin.getStaffMode().inStaffMode(player)) {
                toRemove.add(livingEntity);
            }
        });

        event.getAffectedEntities().removeAll(toRemove);
        toRemove.clear();
    }
}
