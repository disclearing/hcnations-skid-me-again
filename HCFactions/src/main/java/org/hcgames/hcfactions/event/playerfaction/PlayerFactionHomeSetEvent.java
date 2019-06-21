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

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.hcgames.hcfactions.faction.PlayerFaction;

import javax.annotation.Nullable;
import java.util.Optional;

public class PlayerFactionHomeSetEvent extends PlayerFactionEvent implements Cancellable{

    private final static HandlerList handlers = new HandlerList();

    private final Location oldHome;
    private final Location newHome;

    private boolean cancelled;

    public PlayerFactionHomeSetEvent(PlayerFaction faction, @Nullable Location oldHome, @Nullable Location newHome, boolean async) {
        super(faction, async);
        this.oldHome = oldHome;
        this.newHome = newHome;
    }

    public PlayerFactionHomeSetEvent(PlayerFaction faction, @Nullable Location oldHome, @Nullable Location newHome) {
        this(faction, oldHome, newHome, false);
    }

    public Optional<Location> getOldHome(){
        return Optional.ofNullable(oldHome);
    }

    public Optional<Location> getNewHome(){
        return Optional.ofNullable(newHome);
    }

    @Override
    public void setCancelled(boolean cancelled){
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled(){
        return cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public HandlerList getHandlerList(){
        return handlers;
    }
}
