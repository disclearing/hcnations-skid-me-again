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

package org.hcgames.hcfactions.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.ChatChannel;
//import technology.brk.staff.event.PlayerChatLockedEvent;
//import technology.brk.staff.event.PlayerChatSlowedEvent;

@RequiredArgsConstructor
public class StaffHookListener implements Listener {

    private final HCFactions plugin;

//    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
//    public void onPlayerLockedChat(PlayerChatLockedEvent event){
//        event.setCancelled(shouldCancel(event.getPlayer(), event.getMessage()));
//    }
//
//    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
//    public void onPlayerSlowChat(PlayerChatSlowedEvent event){
//        event.setCancelled(shouldCancel(event.getPlayer(), event.getMessage()));
//    }
//
//    private boolean shouldCancel(Player player, String message){
//        if(plugin.getFactionManager().hasFaction(player)){
//            PlayerFaction faction = plugin.getFactionManager().getPlayerFaction(player);
//
//            ChatChannel chatChannel = faction.getMember(player).getChatChannel();
//
//            if (chatChannel == ChatChannel.FACTION || chatChannel == ChatChannel.ALLIANCE) {
//                return !FactionChatListener.isGlobalChannel(message);
//            }
//        }
//
//        return false;
//    }
}
