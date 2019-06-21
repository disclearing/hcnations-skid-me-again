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

package org.hcgames.kmextra.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Messages {

    private static HashMap<String, String> messages = new HashMap<>();

    public static void load(Plugin plugin){
        File messages = new File(plugin.getDataFolder(), "messages.yml");
        if(!messages.exists()){
            try{
                messages.createNewFile();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        InputStream inputStream = plugin.getClass().getResourceAsStream("/messages.yml");
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        YamlConfiguration internalMessages = new YamlConfiguration();

        try{
            internalMessages.load(inputStreamReader);
        }catch(IOException | InvalidConfigurationException e){
            e.printStackTrace();
            return;
        }finally{
            try{
                inputStreamReader.close();
                inputStream.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

        YamlConfiguration externalMessages = YamlConfiguration.loadConfiguration(messages);

        for(String key : internalMessages.getKeys(true)){
            String message;

            if(!externalMessages.contains(key)){
                message = internalMessages.getString(key);
                externalMessages.set(key, message);
            }else{
                message = externalMessages.getString(key);
            }

            Messages.messages.put(key.toLowerCase(), ChatColor.translateAlternateColorCodes('&', message));
        }

        try{
            externalMessages.save(messages);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static String get(String key){
        return messages.containsKey(key.toLowerCase()) ? messages.get(key.toLowerCase()) : "Missing message: " + key;
    }

}
