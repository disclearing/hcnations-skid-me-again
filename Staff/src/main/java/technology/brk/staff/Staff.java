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

package technology.brk.staff;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import technology.brk.staff.command.CommandsCommand;
import technology.brk.staff.command.FreezeCommand;
import technology.brk.staff.command.GoCommand;
import technology.brk.staff.command.InvseeCommand;
import technology.brk.staff.command.ReportCommand;
import technology.brk.staff.command.StaffCommand;
import technology.brk.staff.command.VanishCommand;
import technology.brk.staff.command.chat.ClearCommand;
import technology.brk.staff.command.chat.GhostMuteCommand;
import technology.brk.staff.command.chat.LockCommand;
import technology.brk.staff.command.chat.SlowCommand;
import technology.brk.staff.command.chat.StaffChatCommand;
import technology.brk.staff.hook.IcefyreHook;
import technology.brk.staff.listener.ChatListener;
import technology.brk.staff.listener.FreezeListener;
import technology.brk.staff.listener.InvseeListener;
import technology.brk.staff.listener.StaffListener;
import technology.brk.staff.listener.StaffModeListener;
import technology.brk.staff.listener.VanishListener;
import technology.brk.staff.listeners.BlockBreakListener;
import technology.brk.staff.listeners.BlockPlaceListener;
import technology.brk.staff.listeners.EntityDamageListener;
import technology.brk.staff.listeners.InventoryClickListener;
import technology.brk.staff.listeners.InventoryCloseListener;
import technology.brk.staff.listeners.PlayerBucketFillListener;
import technology.brk.staff.listeners.PlayerInteractListener;
import technology.brk.staff.listeners.PlayerJoinListener;
import technology.brk.staff.listeners.PlayerLeaveListener;
import technology.brk.staff.listeners.PlayerPickupItemListener;
import technology.brk.staff.listeners.PlayerRespawnListener;
import technology.brk.staff.listeners.PlayerToggleSneakListener;
import technology.brk.staff.listeners.PotionSplashListener;
import technology.brk.staff.manager.ChatManager;
import technology.brk.staff.manager.FreezeManager;
import technology.brk.staff.manager.VanishManager;
import technology.brk.staff.manager.invsee.InvseeManager;
import technology.brk.staff.method.go.SlowChatManager;
import technology.brk.staff.methods.GoManager;
import technology.brk.staff.methods.ReportManager;
import technology.brk.staff.methods.StaffMode;
import technology.brk.staff.player.PlayerManager;
import technology.brk.staff.util.Messages;

import java.util.Collection;
import java.util.Map;

@Getter
public class Staff extends JavaPlugin{

    private Messages messages;

    private GoManager goManager;

    private InvseeManager invseeManager;

    private ReportManager reportManager;

    private StaffMode staffMode;

    private VanishManager vanishManager;

    private FreezeManager freezeManager;

    @Setter private int staffOnline;

    private PlayerManager playerManager;

    private SlowChatManager slowChatManager;

    private ChatManager chatManager;

    private IcefyreHook icefyreHook;

    public static Collection<Player> getOnlinePlayers() {
        Object ret = Bukkit.getOnlinePlayers();
        return (Collection)(ret instanceof Collection ? (Collection)ret : ImmutableList.copyOf(((Player[])ret)));
    }

    @Override
    public void onEnable(){
        saveDefaultConfig();
        messages = new Messages(this, "messages.yml", true);

        registerManagers();
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable(){
        chatManager.saveGhostMutes();
        playerManager.onDisable();
    }

    private void registerManagers(){
        vanishManager = new VanishManager(this);
        chatManager = new ChatManager(this);
        freezeManager = new FreezeManager(this);
        invseeManager = new InvseeManager(this);

        goManager = new GoManager(this);
        staffMode = new StaffMode(this);
        reportManager = new ReportManager(this);
        slowChatManager = new SlowChatManager();
        playerManager = new PlayerManager();
        playerManager.onEnable(this);
        
        icefyreHook = new IcefyreHook(this);
    }

    private void registerListeners(){
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new StaffListener(this), this);
        pm.registerEvents(new VanishListener(this), this);
        pm.registerEvents(new StaffModeListener(this), this);
        pm.registerEvents(new FreezeListener(this), this);
        pm.registerEvents(new InvseeListener(this), this);

        //All of this is for staff mode reports :/
        //Could be condensed down into 2 classes
        pm.registerEvents(new PlayerLeaveListener(this), this);
        pm.registerEvents(new PlayerToggleSneakListener(this), this);
        pm.registerEvents(new InventoryCloseListener(this), this);
        pm.registerEvents(new EntityDamageListener(this), this);
        pm.registerEvents(new PlayerPickupItemListener(this), this);
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new PotionSplashListener(this), this);
        pm.registerEvents(new InventoryClickListener(this), this);
        pm.registerEvents(new PlayerRespawnListener(this), this);
        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new BlockPlaceListener(this), this);
        pm.registerEvents(new BlockBreakListener(this), this);
        pm.registerEvents(new PlayerBucketFillListener(this), this);
    }

    private void registerCommands() {
        getCommand("clear").setExecutor(new ClearCommand(this));
        getCommand("lock").setExecutor(new LockCommand(this));
        getCommand("slow").setExecutor(new SlowCommand(this));
        getCommand("ghostmute").setExecutor(new GhostMuteCommand(this));
        getCommand("staffchat").setExecutor(new StaffChatCommand(this));
        getCommand("vanish").setExecutor(new VanishCommand(this));
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("invsee").setExecutor(new InvseeCommand(this));

        getCommand("commands").setExecutor(new CommandsCommand(this));
        getCommand("go").setExecutor(new GoCommand(this));
        getCommand("staff").setExecutor(new StaffCommand(this));

        ReportCommand report = new ReportCommand(this);
        getCommand("report").setExecutor(report);
        getCommand("viewreports").setExecutor(report);

        for(Map.Entry<String, Map<String, Object>> entry : getDescription().getCommands().entrySet()){
            PluginCommand command = getCommand(entry.getKey());
            command.setPermission("staff.command." + entry.getKey());
            command.setPermissionMessage(messages.getString("error.no_permission"));
        }
    }

    public void handleLogin(Player player){
        playerManager.handleLogin(player);

        //TODO Old
        if(player.hasPermission("server.staff")){
            staffOnline++;
        }

        staffMode.handleLogin(player);
    }

    public void handleLogout(Player player){
        playerManager.handleLogout(player);
        slowChatManager.handleLogout(player);

        //TODO Old
        if(player.hasPermission("server.staff")){
            staffOnline--;
        }

        if(goManager.hasGoEnabled(player.getUniqueId())){
            player.sendMessage(getMessages().getString("Commands-Go-Disabled"));
        }

        reportManager.handleLogout(player);
        goManager.remove(player);
        staffMode.handleLogout(player);
    }
}
