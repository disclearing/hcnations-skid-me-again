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

import org.bukkit.configuration.MemorySection;
import org.hcgames.kmextra.KMExtra;
import technology.brk.util.file.Config;

import java.util.*;

public class ProfileManager {

    private final Map<UUID, Profile> profiles = new HashMap<>();
    private final Config userConfig;

    public ProfileManager(KMExtra plugin){
        userConfig = new Config(plugin, "users.yml");
        loadProfileData();
    }

    public Profile getProfile(UUID player) {
        if(profiles.containsKey(player)) {
            return profiles.get(player);
        }

        Profile profile = new Profile();
        profiles.put(player, profile);

        return profile;
    }

    public Profile getProfileAsync(UUID player){
        synchronized (profiles){
            if(profiles.containsKey(player)) {
                return profiles.get(player);
            }

            Profile profile = new Profile();
            profiles.put(player, profile);

            return profile;
        }
    }

    public void loadProfileData(){
        Object object = userConfig.get("users");
        if(object instanceof MemorySection){
            MemorySection section = (MemorySection) object;
            Collection<String> keys = section.getKeys(false);

            for(String id : keys){
                profiles.put(UUID.fromString(id), (Profile) userConfig.get(section.getCurrentPath() + '.' + id));
            }
        }
    }

    public void saveProfileData(){
        Set<Map.Entry<UUID, Profile>> entrySet = profiles.entrySet();
        Map<String, Profile> saveMap = new LinkedHashMap<>(entrySet.size());

        for(Map.Entry<UUID, Profile> entry : entrySet){
            saveMap.put(entry.getKey().toString(), entry.getValue());
        }

        userConfig.set("users", saveMap);
        userConfig.save();
    }
}
