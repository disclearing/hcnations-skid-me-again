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

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class Config extends YamlConfiguration{

    private final File file;

    public Config(Plugin plugin, String name, String fileExtension){
        file = new File(name);

        if(!file.exists()){
            if(plugin.getClass().getResource("/" + name) == null){
                try{
                    file.createNewFile();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }else{
                plugin.saveResource("/" + name, false);
            }
        }

        load();
    }

    public Config(Plugin plugin, String name){
        this(plugin, name, findExtension(name));
    }

    public void header(String header){
        options().header(header);
    }

    public void save(){
        try{
            save(file);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void load(){
        try{
            load(file);
        }catch(IOException | InvalidConfigurationException e){
            e.printStackTrace();
        }
    }

    private static String findExtension(String name){
        if(name.contains(".")){
            String[] split = name.split(".");
            if(split.length > 0 && StringUtils.isNotEmpty(split[1])){
                return split[1];
            }
        }

        return ".yml";
    }
}
