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

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.event.FactionEvent;
import org.hcgames.hcfactions.faction.ClaimableFaction;

import java.util.Collection;

@Getter @Setter
public abstract class ClaimChangeEvent extends FactionEvent<ClaimableFaction> {

    private final Collection<Claim> claims;
    private final ClaimChangeReason reason;
    private final CommandSender sender;

    ClaimChangeEvent(@NonNull CommandSender sender, @NonNull ClaimableFaction faction, @NonNull Collection<Claim> claims, @NonNull ClaimChangeReason reason) {
        super(faction);
        Validate.isTrue(!claims.isEmpty(), "Claims cannot be empty");
        this.sender = sender;
        this.claims = claims;
        this.reason = reason;
    }

    ClaimChangeEvent(@NonNull CommandSender sender, @NonNull ClaimableFaction faction, @NonNull Collection<Claim> claims, @NonNull ClaimChangeReason reason, boolean async) {
        super(faction, async);
        Validate.isTrue(!claims.isEmpty(), "Claims cannot be empty");
        this.sender = sender;
        this.claims = claims;
        this.reason = reason;
    }


    public boolean isSingleton(){
        return claims.size() == 1;
    }

    public enum ClaimChangeReason{
        UNCLAIM, CLAIM, RESIZE, DISBAND
    }
}
