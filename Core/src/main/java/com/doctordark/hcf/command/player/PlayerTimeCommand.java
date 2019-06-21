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

package com.doctordark.hcf.command.player;

import com.doctordark.hcf.HCF;
import com.doctordark.util.Permissions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.util.org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import technology.brk.util.uuid.UUIDHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PlayerTimeCommand implements CommandExecutor, Listener{

    private Cache<UUID, Integer> cache;

    private HCF plugin;

    public PlayerTimeCommand(HCF plugin){
        this.plugin = plugin;

        cache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(100).build(new CacheLoader<UUID, Integer>(){
            @Override
            public Integer load(UUID uuid) throws Exception{
                File statFile = new File(plugin.getServer().getWorlds().get(0).getWorldFolder(), "stats" + File.separator + uuid.toString() + ".json");

                if(!statFile.exists()){
                    return -1;
                }

                InputStream inputStream = null;

                int response = -3;

                try{
                    inputStream = new FileInputStream(statFile);
                    JSONObject jsonObject = (JSONObject) new JSONParser().parse(IOUtils.toString(inputStream));

                    if(jsonObject.containsKey("stat.playOneMinute")){
                        response = ((Long) jsonObject.get("stat.playOneMinute")).intValue();
                    }else{
                        response = -2;
                    }

                }catch(IOException | ParseException e){
                    e.printStackTrace();
                }finally{
                    if(!(inputStream == null)){
                        try{
                            inputStream.close();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }

                return response;
            }
        });

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
        Player player;

        if(args.length >= 1){
            player = plugin.getServer().getPlayer(args[0]);
        }else if(sender instanceof Player){
            player = (Player) sender;
        }else{
            sender.sendMessage(plugin.getMessages().getString("Commands.PlayerTime.Invalid-Usage")
                    .replace("{commandLabel}", commandLabel));
            return true;
        }

        if(player != null){
            if(player.hasPermission(Permissions.STAFF_IDENTIFIER) && (sender instanceof Player && !sender.hasPermission("hcf.command.playertime.viewstaff"))){
                sender.sendMessage(plugin.getMessages().getString("Error-Messages.NoPermission"));
                return true;
            }

            sender.sendMessage(plugin.getMessages().getString("Commands.PlayerTime.Output")
                    .replace("{player}", player.getName())
                    .replace("{time}", formatPlayTime(player.getStatistic(Statistic.PLAY_ONE_TICK))));
            return true;
        }

        sender.sendMessage(ChatColor.GRAY + "(Loading..)");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID userUUID = UUIDHandler.getUUID(args[0]);

            if(userUUID == null){
                sender.sendMessage(plugin.getMessages().getString("Error-Messages.InvalidPlayer")
                        .replace("{player}", args[0]));
                return;
            }

            //TODO Perform staff perm check

            plugin.getLogger().info("[Player Time] Loading offline profile: " + userUUID + " (" + args[0] + ")");

            int response;

            try{
                response = cache.get(userUUID);
            }catch(ExecutionException e){
                e.printStackTrace();
                response = -3;
            }

            switch(response){
                case -3:
                    sender.sendMessage(ChatColor.RED + "Internal Error");
                    break;
                case -2:
                    sender.sendMessage(plugin.getMessages().getString("Commands.PlayerTime.Error.No-Time-Recorded"));
                    break;
                case -1:
                    sender.sendMessage(plugin.getMessages().getString("Commands.PlayerTime.Error.Missing-File"));
                    break;
                default:
                    sender.sendMessage(plugin.getMessages().getString("Commands.PlayerTime.Output")
                            .replace("{player}", args[0])
                            .replace("{time}", formatPlayTime(response)));
                    break;
            }

        });
        return true;
    }

    private String formatPlayTime(int ticks){
        return DurationFormatUtils.formatDurationWords(TimeUnit.SECONDS.toMillis(ticks / 20), true, true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event){
        if(!cache.asMap().containsKey(event.getPlayer().getUniqueId())){
            return;
        }

        cache.invalidate(event.getPlayer().getUniqueId());
    }
}
