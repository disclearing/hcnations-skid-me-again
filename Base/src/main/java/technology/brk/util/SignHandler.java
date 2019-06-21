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

package technology.brk.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class SignHandler implements Listener {
    private final Multimap<UUID, SignChange> signUpdateMap = HashMultimap.create();
    private final JavaPlugin plugin;

    public SignHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerKick(PlayerQuitEvent event) {
        this.cancelTasks(event.getPlayer(), null, false);
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.cancelTasks(event.getPlayer(), null, false);
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        this.cancelTasks(event.getPlayer(), null, false);
    }

    public boolean showLines(final Player player, Sign sign, String[] newLines, long ticks, boolean forceChange) {
        SignChange signChange;
        String[] lines = sign.getLines();
        if (Arrays.equals(lines, newLines)) {
            return false;
        }

        Collection<SignChange> signChanges = this.getSignChanges(player);
        Iterator<SignChange> iterator = signChanges.iterator();
        while (iterator.hasNext()) {
            signChange = iterator.next();
            if (!signChange.sign.equals(sign)) continue;
            if (!forceChange && Arrays.equals(signChange.newLines, newLines)) {
                return false;
            }
            signChange.runnable.cancel();
            iterator.remove();
            break;
        }

        final Location location = sign.getLocation();
        player.sendSignChange(location, newLines);
        signChange = new SignChange(sign, newLines);
        if (signChanges.add(signChange)) {
            final Block block = sign.getBlock();
            final BlockState previous = block.getState();
            SignChange finalSignChange = signChange;

            BukkitRunnable runnable = new BukkitRunnable(){

                public void run() {
                    if (SignHandler.this.signUpdateMap.remove(player.getUniqueId(), finalSignChange) && previous.equals(block.getState())) {
                        player.sendSignChange(location, lines);
                    }
                }
            };
            runnable.runTaskLater(this.plugin, ticks);
            signChange.runnable = runnable;
        }
        return true;
    }

    public Collection<SignChange> getSignChanges(Player player) {
        return this.signUpdateMap.get(player.getUniqueId());
    }

    public void cancelTasks(@Nullable Sign sign) {
        Iterator iterator = this.signUpdateMap.values().iterator();
        while (iterator.hasNext()) {
            SignChange signChange = (SignChange)iterator.next();
            if (sign != null && !signChange.sign.equals(sign)) continue;
            signChange.runnable.cancel();
            signChange.sign.update();
            iterator.remove();
        }
    }

    public void cancelTasks(Player player, @Nullable Sign sign, boolean revertLines) {
        UUID uuid = player.getUniqueId();
        Iterator iterator = this.signUpdateMap.entries().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            if (!entry.getKey().equals(uuid)) continue;
            SignChange signChange = (SignChange)entry.getValue();
            if (sign != null && !signChange.sign.equals(sign)) continue;
            if (revertLines) {
                player.sendSignChange(signChange.sign.getLocation(), signChange.sign.getLines());
            }
            signChange.runnable.cancel();
            iterator.remove();
        }
    }

    private static final class SignChange {
        public BukkitRunnable runnable;
        public final Sign sign;
        public final String[] newLines;

        public SignChange(Sign sign, String[] newLines) {
            this.sign = sign;
            this.newLines = newLines;
        }
    }

}

