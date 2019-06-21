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

package technology.brk.staff.manager;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import technology.brk.staff.Staff;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatManager{

    private Set<UUID> ghostMuted = new HashSet<>(); //TODO: Concurrent?

    @Getter @Setter
    private boolean chatLocked;

    private final Staff plugin;

    public ChatManager(Staff plugin){
        if(plugin.getConfig().contains("ghost_mutes")){
            ghostMuted.addAll(plugin.getConfig().getStringList("ghost_mutes").stream().map(UUID::fromString).collect(Collectors.toList()));
        }

        this.plugin = plugin;
    }

    public boolean isGhostMuted(Player player){
        return ghostMuted.contains(player.getUniqueId());
    }

    public boolean setGhostMuted(Player player, boolean state){
        if(state){
            ghostMuted.add(player.getUniqueId());
        }else{
            ghostMuted.remove(player.getUniqueId());
        }

        return state;
    }

    public void saveGhostMutes(){
        plugin.getConfig().set("ghost_mutes", ghostMuted.stream().map(Object::toString).collect(Collectors.toList()));
        plugin.saveConfig();
    }
}
