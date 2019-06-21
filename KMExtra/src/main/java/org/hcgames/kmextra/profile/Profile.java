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

package org.hcgames.kmextra.profile;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import technology.brk.util.InventorySerialisation;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class Profile implements ConfigurationSerializable{

    private int highestKillStreak;
    private int killstreak;

    @Setter private Inventory chest;

    public Profile(){

    }

    public Profile(Map<String, Object> map){
        highestKillStreak = (Integer) map.get("highestKillStreak");

        if(map.containsKey("chest")){
            try{
                chest = InventorySerialisation.fromBase64((String) map.get("chest"));
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void setKillStreak(int killstreak) {
        this.killstreak = killstreak;

        if(killstreak > highestKillStreak){
            highestKillStreak = killstreak;
        }
    }

    @Override
    public Map<String, Object> serialize(){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("highestKillStreak", highestKillStreak);
        if(chest != null){
            map.put("chest", InventorySerialisation.toBase64(chest));
        }
        return map;
    }
}
