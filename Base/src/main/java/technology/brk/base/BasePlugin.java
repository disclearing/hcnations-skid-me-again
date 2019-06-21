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
package technology.brk.base;

import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import technology.brk.base.command.SetSlotsCommand;
import technology.brk.util.DateTimeFormats;
import technology.brk.util.PersistableLocation;
import technology.brk.util.SignHandler;
import technology.brk.util.chat.Lang;
import technology.brk.util.cuboid.Cuboid;
import technology.brk.util.cuboid.NamedCuboid;
import technology.brk.util.itemdb.ItemDb;
import technology.brk.util.itemdb.SimpleItemDb;

import java.io.IOException;
import java.util.Optional;
import java.util.TimeZone;

public class BasePlugin extends JavaPlugin implements Listener{

    @Getter
    private SignHandler signHandler;

    @Getter
    private ItemDb itemDb;

    private static BasePlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();

        ConfigurationSerialization.registerClass(PersistableLocation.class);
        ConfigurationSerialization.registerClass(Cuboid.class);
        ConfigurationSerialization.registerClass(NamedCuboid.class);

        registerManagers();
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        this.signHandler.cancelTasks(null);
        plugin = null;
    }

    private void registerManagers() {
        this.itemDb = new SimpleItemDb(this);
        this.signHandler = new SignHandler(this);
        DateTimeFormats.reload(TimeZone.getTimeZone(getConfig().getString("TIMEZONE")));

        try{
            Lang.initialize("en_US");
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void registerCommands(){
        getCommand("setslots").setExecutor(new SetSlotsCommand(this));
    }

    private void registerListeners(){
        PluginManager manager = getServer().getPluginManager();

        manager.registerEvents(signHandler, this);
        manager.registerEvents(this, this);
    }

    public static BasePlugin getPlugin() {
        return plugin;
    }

    @EventHandler(ignoreCancelled=true, priority= EventPriority.NORMAL)
    private void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.NORMAL)
    private void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onLeavesDecay(LeavesDecayEvent event){
        event.setCancelled(true);
    }

}

