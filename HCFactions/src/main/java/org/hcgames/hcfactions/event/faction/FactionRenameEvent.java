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

package org.hcgames.hcfactions.event.faction;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.hcgames.hcfactions.event.FactionEvent;
import org.hcgames.hcfactions.faction.Faction;

@Getter @Setter
public class FactionRenameEvent extends FactionEvent<Faction> implements Cancellable{

    private final static HandlerList handlers = new HandlerList();
    private final CommandSender sender;

    private final String oldName;
    private String newName;

    private final boolean displayName;
    private boolean cancelled;

    public FactionRenameEvent(@NonNull CommandSender sender, @NonNull Faction faction, @NonNull String oldName, @NonNull String newName, @NonNull boolean displayName, boolean async){
        super(faction);
        this.sender = sender;
        this.oldName = oldName;
        this.newName = newName;
        this.displayName = displayName;
    }

    public FactionRenameEvent(@NonNull CommandSender sender, @NonNull Faction faction, @NonNull String oldName, @NonNull String newName, @NonNull boolean displayName){
        this(sender, faction, oldName, newName, displayName, false);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList(){
        return handlers;
    }
}
