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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import technology.brk.staff.Staff;
import technology.brk.staff.event.PlayerChatLockedEvent;
import technology.brk.staff.event.PlayerChatSlowedEvent;

import java.util.Collections;

@RequiredArgsConstructor
public class ChatListener implements Listener{

    private final Staff plugin;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();

        if(plugin.getChatManager().isChatLocked() && !event.getPlayer().hasPermission("staff.bypass.lockedchat")){
            PlayerChatLockedEvent playerChatLockedEvent = new PlayerChatLockedEvent(player, event.getMessage());
            plugin.getServer().getPluginManager().callEvent(playerChatLockedEvent);

            if(!playerChatLockedEvent.isCancelled()) {
                event.getPlayer().sendMessage(plugin.getMessages().getString("Interaction-Chat-LockedChat"));
                event.setCancelled(true);
            }
        }

        if(plugin.getSlowChatManager().isEnabled() && !player.hasPermission("staff.bypass.slowchat")){
            if(!plugin.getSlowChatManager().hasBeenLogged(player)){
                plugin.getSlowChatManager().logTime(player);
            }else{
                long lastSentWithDelay = plugin.getSlowChatManager().getLastTime(player) + plugin.getSlowChatManager().getDelay();
                if(System.currentTimeMillis() <= lastSentWithDelay){
                    PlayerChatSlowedEvent playerChatSlowedEvent = new PlayerChatSlowedEvent(player, event.getMessage());
                    plugin.getServer().getPluginManager().callEvent(playerChatSlowedEvent);

                    if(!playerChatSlowedEvent.isCancelled()) {
                        double timeLeft = (lastSentWithDelay - System.currentTimeMillis()) / 1000.0F;

                        event.getPlayer().sendMessage(plugin.getMessages().getString("Interaction-Chat-SlowedChat")
                                .replace("{time}", String.format("%.1f", timeLeft))
                                .replace("{s}", (timeLeft == 1 ? "" : "s")));
                        event.setCancelled(true);
                    }
                }else{
                    plugin.getSlowChatManager().logTime(event.getPlayer());
                }
            }
        }

        if(!event.isCancelled()){
            if(plugin.getChatManager().isGhostMuted(player)){
                event.getRecipients().retainAll(Collections.singleton(player));
            }
        }
    }

}
