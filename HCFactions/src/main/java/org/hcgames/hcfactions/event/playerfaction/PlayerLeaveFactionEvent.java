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

package org.hcgames.hcfactions.event.playerfaction;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.hcgames.hcfactions.faction.PlayerFaction;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class PlayerLeaveFactionEvent extends PlayerFactionEvent implements Cancellable{

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private Player player;

    @Getter
    private final CommandSender sender;

    @Getter
    private final UUID uniqueID;

    @Getter
    private final FactionLeaveCause cause;

    @Getter
    private final boolean isKick;

    @Getter
    private final boolean force;

    public PlayerLeaveFactionEvent(@NonNull CommandSender sender, @Nullable Player player, @NonNull UUID playerUUID, @NonNull PlayerFaction faction, @NonNull FactionLeaveCause cause, boolean isKick, boolean force){
        super(faction);

        this.sender = sender;
        this.player = player;
        this.uniqueID = playerUUID;
        this.cause = cause;
        this.isKick = isKick;
        this.force = force;
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(player);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public enum FactionLeaveCause {
        KICK, LEAVE, DISBAND
    }
}
