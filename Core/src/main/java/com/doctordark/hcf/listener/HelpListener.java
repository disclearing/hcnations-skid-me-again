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

package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class HelpListener implements Listener{

    private final ImmutableMultimap<String, String> files;

    public HelpListener(HCF plugin){
        File folder = new File(plugin.getDataFolder(), "help");

        if(!folder.exists()){
            folder.mkdir();
            files = ImmutableMultimap.of();
            return;
        }

        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if(files != null){
            for(File file : files){
                try{
                    builder.putAll(file.getName().replace(".txt", "").toLowerCase(), FileUtils.readLines(file)
                            .stream().map(line -> ChatColor.translateAlternateColorCodes('&', line))
                            .collect(Collectors.toList()));
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }

        this.files = builder.build();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
        String command = event.getMessage().split(" ")[0].toLowerCase().replace("/", "");

        if(files.containsKey(command)){
            files.get(command).forEach(event.getPlayer()::sendMessage);
            event.setCancelled(true);
        }
    }

    public void unregister(){
        PlayerCommandPreprocessEvent.getHandlerList().unregister(this);
    }
}
