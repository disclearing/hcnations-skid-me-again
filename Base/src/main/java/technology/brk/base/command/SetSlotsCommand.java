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

package technology.brk.base.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import technology.brk.base.BasePlugin;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

public class SetSlotsCommand implements CommandExecutor {

    private static final String API_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private final BasePlugin plugin;

    private Object playerList;
    private Field maxPlayers;

    public SetSlotsCommand(BasePlugin plugin){
        try {
            Class<?> nmsPlayerListClass = Class.forName("net.minecraft.server." + API_VERSION + ".PlayerList");
            maxPlayers = nmsPlayerListClass.getDeclaredField("maxPlayers");
            maxPlayers.setAccessible(true);

            Field playerList = plugin.getServer().getClass().getDeclaredField("playerList");
            playerList.setAccessible(true);
            this.playerList = playerList.get(plugin.getServer());
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Invalid number of slots specified!");
            return false;
        }

        int slots;
        try{
            slots = Math.abs(Integer.valueOf(args[0]));
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number of slots specified!");
            return true;
        }

        try{
            maxPlayers.set(playerList, slots);
        }catch (IllegalAccessException e){
            sender.sendMessage(ChatColor.RED + "Failed to set slots - See console for error.");
            e.printStackTrace();
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Successfully set the slots to " + ChatColor.BLUE + String.valueOf(slots) + ChatColor.YELLOW + ".");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try{
                FileInputStream in = new FileInputStream("server.properties");
                Properties props = new Properties();
                props.load(in);
                in.close();

                FileOutputStream out = new FileOutputStream("server.properties");
                props.setProperty("max-players", String.valueOf(slots));
                props.store(out, null);
                out.close();
            }catch (IOException e){
                sender.sendMessage(ChatColor.RED + "Failed to update server properties, see error in console,");
                e.printStackTrace();
            }
        });
        return true;
    }
}
