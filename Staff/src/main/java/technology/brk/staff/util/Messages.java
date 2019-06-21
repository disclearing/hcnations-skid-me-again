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

package technology.brk.staff.util;

import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Messages {

    private final static Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private YamlConfiguration config;

    private boolean msgDebug;
    private String fileName;

    @SneakyThrows
    public Messages(Plugin plugin, String fileName, boolean msgDebug){
        this.msgDebug = msgDebug;
        this.fileName = fileName;

        File file = new File(plugin.getDataFolder(), fileName);
        boolean modified = false;

        if(!file.exists()){
            plugin.saveResource(fileName, false);
            config = YamlConfiguration.loadConfiguration(file);
        }else{
            config = YamlConfiguration.loadConfiguration(file);
            try(InputStream stream = plugin.getClass().getResourceAsStream("/" + fileName); InputStreamReader reader = new InputStreamReader(stream)){
                YamlConfiguration internalMessages = YamlConfiguration.loadConfiguration(reader);
                internalMessages.options().header("File configuration for " + plugin.getDescription().getName() +
                        ". (Last updated: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + ")");

                for(String key : internalMessages.getKeys(true)){
                    if(!config.contains(key)){
                        Object object = internalMessages.get(key);

                        if(!(object instanceof MemorySection)){
                            if(msgDebug){
                                plugin.getLogger().severe("[" + fileName + "] Adding missing message: " + key);
                            }

                            config.set(key, object);
                            modified = true;
                        }
                    }
                }
            }
        }

        if(modified){
            config.save(file);
        }
    }

    public String getString(String key){
        return getString(key, EMPTY_OBJECT_ARRAY);
    }

    public String getString(String key, Object... arguments){
        return config.contains(key) ? arguments.length > 0 ? format(config.getString(key), arguments) : ChatColor.translateAlternateColorCodes('&', config.getString(key)) : null;
    }

    public List<String> getStringList(String key){
        return getStringList(key, EMPTY_OBJECT_ARRAY);
    }

    public List<String> getStringList(String key, Object... arguments){
        List<String> returned = config.getStringList(key);

        if(returned == null || returned.isEmpty()){
            return returned;
        }

        return arguments.length > 0 ? returned.stream().map(i -> format(i, arguments)).collect(Collectors.toList()) :
                returned.stream().map(i -> ChatColor.translateAlternateColorCodes('&', i)).collect(Collectors.toList());
    }

    private String format(String message, Object... replacements){
        message = ChatColor.translateAlternateColorCodes('&', message);
        int current = 0;

        for(Object replacement : replacements){
            message = message.replace("{" + current + "}", String.valueOf(replacement));
            current++;
        }

        return message;
    }

}
