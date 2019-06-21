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
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.event.playerfaction.PlayerFactionChatEvent;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.ChatChannel;
import org.hcgames.hcfactions.structure.Role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class FactionChatListener implements Listener{

    private final HCFactions plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();

        PlayerFaction playerFaction = plugin.getFactionManager().hasFaction(player) ? plugin.getFactionManager().getPlayerFaction(player) : null;
        ChatChannel chatChannel = playerFaction == null ? ChatChannel.PUBLIC : playerFaction.getMember(player).getChatChannel();

        // Handle faction or alliance chat modes.
        Set<Player> recipients = event.getRecipients();
        if (chatChannel == ChatChannel.FACTION || chatChannel == ChatChannel.ALLIANCE || chatChannel == ChatChannel.OFFICER) {
            if (isGlobalChannel(message)) { // allow players to use '!' to bypass friendly chat.
                message = message.substring(1, message.length()).trim();
                event.setMessage(message);
            } else {
                Collection<Player> online = chatChannel == ChatChannel.OFFICER ? new ArrayList<>() : playerFaction.getOnlinePlayers();
                if (chatChannel == ChatChannel.ALLIANCE) {
                    Collection<PlayerFaction> allies = playerFaction.getAlliedFactions();
                    for (PlayerFaction ally : allies) {
                        online.addAll(ally.getOnlinePlayers());
                    }
                }

                if(chatChannel == ChatChannel.OFFICER){
                    online.addAll(playerFaction.getOnlineMembers().entrySet().stream().filter(member ->
                            member.getValue().getRole() != Role.MEMBER).map(member -> plugin.getServer()
                            .getPlayer(member.getKey())).collect(Collectors.toList()));
                }

                recipients.retainAll(online);
                event.setFormat(chatChannel.getRawFormat(player));
                String displayName = player.getDisplayName();
                ConsoleCommandSender console = Bukkit.getConsoleSender();

                Bukkit.getPluginManager().callEvent(new PlayerFactionChatEvent(true, playerFaction, player, chatChannel, recipients, event.getMessage()));

                event.setCancelled(true);
                console.sendMessage(String.format(event.getFormat(), displayName, message));
                for (Player recipient : event.getRecipients()) {
                    recipient.sendMessage(String.format(event.getFormat(), displayName, message));
                }
            }
        }
    }

    /**
     * Checks if a message should be posted in {@link ChatChannel#PUBLIC}.
     *
     * @param input the message to check
     * @return true if the message should be posted in {@link ChatChannel#PUBLIC}
     */
    public static boolean isGlobalChannel(String input) {
        int length = input.length();

        if (length > 1 && input.startsWith("!")) {
            for (int i = 1; i < length; i++) {
                char character = input.charAt(i);

                // Ignore whitespace to prevent blank messages
                if (Character.isWhitespace(character)) {
                    continue;
                }

                // Player is faking a command
                return character != '/';
            }
        }

        return false;
    }

}
