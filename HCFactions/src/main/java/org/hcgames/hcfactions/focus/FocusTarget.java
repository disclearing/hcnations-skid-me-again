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

package org.hcgames.hcfactions.focus;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;

import java.util.Optional;
import java.util.UUID;

@Getter
public class FocusTarget {

    private final HCFactions plugin;

    private final UUID current;
    @Getter private final UUID target;

    @Getter private UUID mapKey = UUID.randomUUID();
    @Getter private final boolean factionTarget;

    private FocusTarget(HCFactions plugin, UUID current, UUID target, boolean factionTarget){
        this.plugin = plugin;
        this.current = current;
        this.target = target;
        this.factionTarget = factionTarget;
    }

    public FocusTarget(HCFactions plugin, PlayerFaction current, PlayerFaction target){
        this(plugin, current.getUniqueID(), target.getUniqueID(), true);
    }

    public FocusTarget(HCFactions plugin, PlayerFaction current, Player target){
        this(plugin, current.getUniqueID(), target.getUniqueId(), false);
    }

    public Optional<PlayerFaction> getCurrent(){
        try {
            return Optional.of(plugin.getFactionManager().getFaction(current, PlayerFaction.class));
        } catch (NoFactionFoundException e) {
            return Optional.empty();
        }
    }
}
