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

import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpiringMap;
import org.bukkit.Bukkit;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.event.playerfaction.PlayerFactionFocusEvent;
import org.hcgames.hcfactions.event.playerfaction.PlayerFactionUnfocusEvent;
import org.hcgames.hcfactions.faction.PlayerFaction;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FocusHandler{

    private final ExpiringMap<UUID, FocusTarget> map = ExpiringMap.builder().asyncExpirationListener(inner = new Inner()).expiration(2, TimeUnit.HOURS).build();

    private final HCFactions plugin;
    private final Inner inner;

    public FocusHandler(HCFactions plugin){
        this.plugin = plugin;
    }

    private class Inner implements ExpirationListener<UUID, FocusTarget>{

        @Override
        public void expired(@Nullable UUID uuid, FocusTarget target){
            Optional<PlayerFaction> factionOptional = target.getCurrent();
            if(factionOptional.isPresent()) {
                PlayerFaction current = factionOptional.get();
                current.fr(target);

                PlayerFactionUnfocusEvent event = new PlayerFactionUnfocusEvent(current, target, !Bukkit.isPrimaryThread());
                plugin.getServer().getPluginManager().callEvent(event);
            }
        }
    }

    public void focus(PlayerFaction current, FocusTarget target){
        PlayerFactionFocusEvent event = new PlayerFactionFocusEvent(current, target, !Bukkit.isPrimaryThread());
        plugin.getServer().getPluginManager().callEvent(event);

        if(!event.isCancelled()) {
            current.af(target);
            map.put(target.getMapKey(), target);
        }
    }

    public void unfocus(UUID mapKey){
        if(map.containsKey(mapKey)){
            inner.expired(null, map.get(mapKey));
            map.remove(mapKey);
        }
    }
}
