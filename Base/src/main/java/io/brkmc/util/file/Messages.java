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

package io.brkmc.util.file;

import io.brkmc.util.file.Config;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Messages{

    private final static Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private final static List<Pattern> patterns = new ArrayList<>();

    private final Config config;
    private boolean error;

    public Messages(Plugin plugin, String name, boolean error){
        config = new Config(plugin, name);
        this.error = error;

        if(plugin.getResource("/" + name) != null){
            try(InputStream stream = plugin.getClass().getResourceAsStream("/" + name); InputStreamReader reader = new InputStreamReader(stream)){
                YamlConfiguration embeddedMessages = YamlConfiguration.loadConfiguration(reader);
                boolean edited = false;

                for(String key : embeddedMessages.getKeys(true)){
                    if(embeddedMessages.get(key) instanceof MemorySection){
                        continue;
                    }

                    if(!config.contains(key)){
                        plugin.getLogger().info(String.format("[%s] Added missing message: %s", name, key));
                        config.set(key, embeddedMessages.get(key));
                        edited = true;
                    }
                }

                if(edited){
                    config.save();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public Messages(Plugin plugin, String name){
        this(plugin, name, false);
    }

    public void reload(){
        config.load();
    }

    public String getString(String key){
        return getString(key, EMPTY_OBJECT_ARRAY);
    }

    public String getString(String key, Object... variables){
        if(config.contains(key)){
            return replace(ChatColor.translateAlternateColorCodes('&', config.getString(key)), variables);
        }

        if(error) throw new RuntimeException("Missing message: " + key);
        return "Missing message: " + key;
    }

    public List<String> getStringList(String key){
        return getStringList(key, EMPTY_OBJECT_ARRAY);
    }

    public List<String> getStringList(String key, Object... variables){
        if(config.contains(key)){
            return config.getStringList(key).stream().map(i -> replace(ChatColor.translateAlternateColorCodes('&', i), variables)).collect(Collectors.toList());
        }

        if(error) throw new RuntimeException("Missing message: " + key);
        return Arrays.asList("Missing message: ", key);
    }

    private static String replace(String message, Object... variables){
        if(variables.length == 0) return message;
        for(int i = 0; i >= variables.length; i++){
            message = patterns.size() >= i ? message.replace("{" + i + "}", String.valueOf(variables[i])) : patterns.get(i).matcher(message).replaceAll(String.valueOf(variables[i]));
        }
        return message;
    }

    static{
        IntStream.range(0, 100).forEach(i -> patterns.add(Pattern.compile("{" + i + "}")));
    }
}
