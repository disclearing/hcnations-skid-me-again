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

package technology.brk.staff.methods;

import lombok.RequiredArgsConstructor;
import net.minecraft.server.PlayerInteractManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import technology.brk.staff.Staff;
import technology.brk.staff.listeners.PlayerInteractListener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
@RequiredArgsConstructor
public class GoManager{

    //TODO: Concurrency + Ugly code
    private final Map<UUID, String> goData = new ConcurrentHashMap<>();
    private final Map<UUID, String> paused = new ConcurrentHashMap<>();

    private final Staff plugin;

    public void add(Player player, int time){
        final UUID uuid = player.getUniqueId();

        goData.put(uuid, Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> runTeleportation(uuid), 0L, time).getTaskId() + "#" + uuid.toString() + "#" + time);
    }

    public void remove(Player player){
        remove(player.getUniqueId());
    }

    public void remove(UUID uuid){
        if(goData.containsKey(uuid)){
            String[] storedData = goData.get(uuid).split("#");

            Bukkit.getServer().getScheduler().cancelTask(Integer.valueOf(storedData[0]));
            goData.remove(uuid);
        }

        if(paused.containsKey(uuid)){
            paused.remove(uuid);
        }
    }

    private void runTeleportation(UUID uuid){
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Player player = Bukkit.getServer().getPlayer(uuid);

            if(player == null || !player.isOnline()){
                remove(uuid);
                return;
            }

            if(!(Staff.getOnlinePlayers().size() > 1)){
                return;
            }

            String[] storedData = goData.get(uuid).split("#");

            UUID currentPlayerUUID = UUID.fromString(storedData[1]);

            Player teleportPlayer = player;

            while(teleportPlayer != null && teleportPlayer.getUniqueId().equals(currentPlayerUUID) || teleportPlayer.hasPermission("staff.bypass.go")){
                teleportPlayer = PlayerInteractListener.getRandomEntry(Staff.getOnlinePlayers());
            }

            final Player finalTeleportPlayer = teleportPlayer;
            Bukkit.getServer().getScheduler().runTask(plugin, () -> player.teleport(finalTeleportPlayer, PlayerTeleportEvent.TeleportCause.PLUGIN));

            goData.put(player.getUniqueId(), storedData[0] + "#" + teleportPlayer.getUniqueId().toString() + "#" + storedData[2]);

            player.sendMessage(plugin.getMessages().getString("Commands-Go-Teleporting")
                    .replace("{player}", teleportPlayer.getName()));
        });
    }

    public void setPaused(UUID uuid, boolean pause){
        if(pause){
            if(!paused.containsKey(uuid)){
                String[] data = goData.get(uuid).split("#");

                remove(uuid);
                paused.put(uuid, data[2]);
            }
        }else{
            if(paused.containsKey(uuid)){
                add(Bukkit.getServer().getPlayer(uuid), Integer.parseInt(paused.get(uuid)));
                paused.remove(uuid);
            }
        }
    }

    public boolean hasGoEnabled(UUID uuid){
        return goData.containsKey(uuid);
    }

    public boolean hasGoPaused(UUID uuid){
        return paused.containsKey(uuid);
    }
}
