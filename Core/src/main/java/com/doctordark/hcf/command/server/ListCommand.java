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

package com.doctordark.hcf.command.server;

import com.doctordark.hcf.HCF;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ListCommand implements CommandExecutor{

    private final HCF plugin;

    private final Map<String, String> groupColours;
    private final String cachedGroups;

    public ListCommand(HCF plugin){
        this.plugin = plugin;
        groupColours = new HashMap<>();

        if(plugin.getVaultHook() != null && plugin.getVaultHook().isHooked()){
            Map<String, String> groups = new LinkedHashMap<>(7);
            groups.put("Owner", "Owner");
            groups.put("Manager", "Manager");
            groups.put("hAdmin", "Head Admin");
            groups.put("Admin", "Admin");
            groups.put("jAdmin", "Junior-Admin");
            groups.put("hMod", "Head Mod");
            groups.put("Mod", "Mod");
            groups.put("tMod", "Trial Mod");
            groups.put("media", "Media");
            groups.put("YouTube", "YouTube");
            groups.put("nation+", "Nation+");
            groups.put("nation", "Nation");
            groups.put("emerald", "Emerald");
            groups.put("diamond", "Diamond");
            groups.put("gold", "Gold");
            groups.put("iron", "Iron");
            groups.put("default", "Default");

            World world = plugin.getServer().getWorlds().get(0);
            StringBuilder builder = new StringBuilder();

            groups.forEach((name, display) -> {
                String colour = ChatColor.getLastColors(plugin.getVaultHook().getChat().getGroupPrefix(world, name));
                groupColours.put(name, ChatColor.getLastColors(colour));
                builder.append(colour).append(display).append(ChatColor.WHITE).append(',').append(' ');
            });

            builder.setLength(Math.max(0, builder.length() - 2));
            cachedGroups = ChatColor.translateAlternateColorCodes('&', builder.toString());
        }else{
            cachedGroups = "";
        }
    }

    @Override //TODO: Order by group
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        Player viewer = sender instanceof Player ? (Player) sender : null;
        StringBuilder players = new StringBuilder();

        plugin.getServer().getOnlinePlayers().stream().limit(100).forEach((Consumer<Player>) player -> {
            if(viewer != null && !viewer.canSee(player)){
                return;
            }else if(plugin.getStaff() != null && plugin.getStaff().getVanishManager().isVanished(player)){
                players.append(ChatColor.GRAY).append("[HIDDEN]").append(ChatColor.RESET);
            }

            players.append(groupColours.getOrDefault(plugin.getVaultHook().getChat().getPrimaryGroup(player), "")).append(player.getName()).append(ChatColor.WHITE).append(',').append(' ');
        });

        int online = plugin.getServer().getOnlinePlayers().size();
        players.setLength(Math.max(0, players.length() - 2));

        sender.sendMessage(plugin.getMessages().getString("Commands.List.Online").replace("{online}", String.valueOf(online)).replace("{maxPlayers}", String.valueOf(plugin.getServer().getMaxPlayers())));
        sender.sendMessage(cachedGroups);
        sender.sendMessage('[' + ChatColor.translateAlternateColorCodes('&', players.toString()) + ChatColor.RESET + ']');

        if(online > 100){
            sender.sendMessage(plugin.getMessages().getString("Commands.List.Max"));
        }
        return true;
    }
}
