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

package technology.brk.staff.manager;

import com.earth2me.essentials.IEssentials;
import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import technology.brk.staff.Staff;
import technology.brk.staff.event.PlayerDisableVanishEvent;
import technology.brk.staff.event.PlayerEnableVanishEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public class VanishManager{

    private final Set<UUID> vanishedUsers = new HashSet<>();
    private final Set<UUID> silentOpens = new HashSet<>();//TODO: Concurrent?

    private final EssentialsHook essentialsHook;
    private final Staff plugin;

    public VanishManager(Staff plugin){
        essentialsHook = new EssentialsHook(plugin);
        this.plugin = plugin;
    }

    public void setVanished(Player player, boolean state){
        if(!isVanished(player) && state){
            PlayerEnableVanishEvent event = new PlayerEnableVanishEvent(player);
            plugin.getServer().getPluginManager().callEvent(event);

            if(event.isCancelled()){
                return;
            }

            for(Player onlinePlayer : plugin.getServer().getOnlinePlayers()){
                if(onlinePlayer.equals(player) || onlinePlayer.hasPermission("staff.staff")){
                    continue;
                }

                onlinePlayer.hidePlayer(player);
            }

            vanishedUsers.add(player.getUniqueId());
            essentialsHook.hide(player);
        }else if(isVanished(player) && !state){
            PlayerDisableVanishEvent event = new PlayerDisableVanishEvent(player);
            plugin.getServer().getPluginManager().callEvent(event);

            if(event.isCancelled()){
                return;
            }

            for(Player onlinePlayer : plugin.getServer().getOnlinePlayers()){
                onlinePlayer.showPlayer(player);
            }

            vanishedUsers.remove(player.getUniqueId());
            essentialsHook.show(player);
        }
    }

    public void openSilently(Player player, Inventory inventory) {
        Inventory newInventory = null;

        if(inventory.getType().equals(InventoryType.CHEST)){
            newInventory = plugin.getServer().createInventory(player, inventory.getSize());
            newInventory.setContents(inventory.getContents());
        }

        if(inventory.getType().equals(InventoryType.ENDER_CHEST)){
            newInventory = player.getEnderChest();
        }

        player.sendMessage(plugin.getMessages().getString("vanish.silent_open"));

        player.openInventory(newInventory == null ? inventory : newInventory);
        silentOpens.add(player.getUniqueId());
    }

    public void handleSilentClose(Player player){
        silentOpens.remove(player.getUniqueId());
    }

    public boolean hasSilentChestOpen(Player player){
        return silentOpens.contains(player.getUniqueId());
    }

    public boolean isVanished(Player player){
        return vanishedUsers.contains(player.getUniqueId());
    }

    public Set<UUID> getVanishedUsers(){
        return ImmutableSet.copyOf(vanishedUsers);
    }

    private class EssentialsHook{

        private final boolean hooked;
        private IEssentials essentials;

        EssentialsHook(Staff plugin){
            Plugin essentialsPlugin = plugin.getServer().getPluginManager().getPlugin("Essentials");

            if(essentialsPlugin instanceof IEssentials){
                essentials = (IEssentials) essentialsPlugin;
                hooked = true;
                plugin.getLogger().info("[Vanish] Hooked into Essentials");
            }else{
                hooked = false;
            }
        }

        void hide(Player player){
            if(hooked){
                essentials.getUser(player).setHidden(true);
            }
        }

        void show(Player player){
            if(hooked){
                essentials.getUser(player).setHidden(false);
            }
        }
    }
}
