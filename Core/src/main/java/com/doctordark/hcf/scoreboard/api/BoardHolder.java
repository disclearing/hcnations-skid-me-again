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

package com.doctordark.hcf.scoreboard.api;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public abstract class BoardHolder<T extends Board> implements Listener{

    //TODO: Implement providers with a "update" method and timer pool
    //TODO: Look into displayNames & team#setName (It's in the packet!)
    //TODO: Also... bulk updates would be a nice addition (Would help with flickering too)
    //TODO: Incorporate Tab API & nametag api (for colouring names - setPrefix(player, prefix))

    private final Map<UUID, T> boards;
    private final Function<Player, T> boardSupplier;

    public BoardHolder(Plugin plugin, Function<Player, T> boardSupplier){
        boards = new HashMap<>();
        this.boardSupplier = boardSupplier;

        plugin.getServer().getPluginManager().registerEvents(new InternalListener(), plugin);
    }

    public T getBoard(Player player){
        return boards.get(player.getUniqueId());
    }

    public Collection<T> getBoards(){
        return boards.values();
    }

    protected abstract String getTitle();

    private class InternalListener implements Listener{

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerJoin(PlayerJoinEvent event){
            Player player = event.getPlayer();
            T board;

            boards.put(player.getUniqueId(), board = boardSupplier.apply(player));
            board.setTitle(getTitle());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event){
            Board board = boards.remove(event.getPlayer().getUniqueId());

            if(board != null){
                board.cancelTask();
            }
        }

    }

}
