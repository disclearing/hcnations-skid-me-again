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

package technology.brk.staff.player;

import org.bukkit.entity.Player;
import technology.brk.staff.Staff;

import java.util.HashMap;
import java.util.UUID;

@Deprecated //TODO: Rewrite entire thing
public class PlayerManager{

    private HashMap<UUID, StaffMember> players = new HashMap<>();

    private boolean enabled;
    private Staff plugin;

    public void onEnable(Staff plugin){
        this.plugin = plugin;
        enabled = true;
    }

    public void onDisable(){
        for(UUID uuid : players.keySet()){
            players.get(uuid).close(false);
            players.remove(uuid);
        }
    }

    public void handleLogin(Player player){
        if(!enabled){
            return;
        }

        players.put(player.getUniqueId(), new StaffMember(player, plugin));
    }

    public void handleLogout(Player player){
        if(players.containsKey(player.getUniqueId())){
            players.get(player.getUniqueId()).close(true);
            players.remove(player.getUniqueId());
        }
    }

    public StaffMember getPlayer(Player player){
        return getPlayer(player.getUniqueId());
    }

    public StaffMember getPlayer(UUID uuid){
        return players.get(uuid);
    }
}
