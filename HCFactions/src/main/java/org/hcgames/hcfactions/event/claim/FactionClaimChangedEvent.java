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

package org.hcgames.hcfactions.event.claim;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.faction.ClaimableFaction;

import java.util.Collection;

public class FactionClaimChangedEvent extends ClaimChangeEvent {

    private final static HandlerList handlers = new HandlerList();

    public FactionClaimChangedEvent(@NonNull CommandSender sender, @NonNull ClaimableFaction faction, @NonNull Collection<Claim> claims, @NonNull ClaimChangeReason reason, boolean async){
        super(sender, faction, claims, reason, async);
    }

    public FactionClaimChangedEvent(@NonNull CommandSender sender, @NonNull ClaimableFaction faction, @NonNull Collection<Claim> claims, @NonNull ClaimChangeReason reason){
        this(sender, faction, claims, reason, false);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList(){
        return handlers;
    }
}
