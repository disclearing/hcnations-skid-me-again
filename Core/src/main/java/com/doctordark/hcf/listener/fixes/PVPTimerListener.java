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

package com.doctordark.hcf.listener.fixes;

import com.doctordark.hcf.HCF;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.hcgames.hcfactions.faction.system.SpawnFaction;

@RequiredArgsConstructor
public class PVPTimerListener implements Listener{

    private final HCF plugin;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        doPvPTimerCheck(event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        doPvPTimerCheck(event.getPlayer(), false);
    }

    private void doPvPTimerCheck(Player player, boolean join){
        if(plugin.getSOTWManager().isActive()) return;
        if(plugin.getConfiguration().isKitMap()) return;

        if(plugin.getTimerManager().getEventTimer().getName().equals("EOTW")){
            if(plugin.getTimerManager().getInvincibilityTimer().getRemaining(player) > 0){
                plugin.getTimerManager().getInvincibilityTimer().clearCooldown(player);
            }
            return;
        }

        if(!join || !player.hasPlayedBefore()){
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "timer set Invincibility " + player.getName() + " 30m");
            if(plugin.getFactions().getFactionManager().getFactionAt(player.getLocation()) instanceof SpawnFaction){
                plugin.getTimerManager().getInvincibilityTimer().setPaused(player.getUniqueId(), true);
            }
        }
    }
}
