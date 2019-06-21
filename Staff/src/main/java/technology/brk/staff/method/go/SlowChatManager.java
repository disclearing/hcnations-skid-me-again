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

package technology.brk.staff.method.go;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

//TODO: New version: ChatManager (After VanishManager & command rewrite is done)
@Deprecated
public class SlowChatManager{

    @Getter private boolean enabled;
    @Getter private long delay;

    private Map<UUID, Long> lastSentTimes = new ConcurrentHashMap<>();//TODO: Concurrent

    public void handleLogout(Player player){
        if(!lastSentTimes.containsKey(player.getUniqueId())){
            return;
        }

        lastSentTimes.remove(player.getUniqueId());
    }

    public void setEnabled(boolean enabled){
        if(!enabled && this.enabled){
            lastSentTimes.clear();
        }

        this.enabled = !this.enabled;
    }

    public boolean setDelay(int delay){
        long newDelay = TimeUnit.SECONDS.toMillis(delay);

        if(newDelay != this.delay){
            this.delay = newDelay;
            lastSentTimes.clear();
            return true;
        }

        return false;
    }

    public void logTime(Player player){
        logTime(player.getUniqueId());
    }

    public void logTime(UUID uuid){
        lastSentTimes.put(uuid, System.currentTimeMillis());
    }

    public long getLastTime(Player player){
        return getLastTime(player.getUniqueId());
    }

    public long getLastTime(UUID uuid){
        if(!lastSentTimes.containsKey(uuid)){
            return 0;
        }

        return lastSentTimes.get(uuid);
    }

    public boolean hasBeenLogged(Player player){
        return lastSentTimes.containsKey(player.getUniqueId());
    }
}
