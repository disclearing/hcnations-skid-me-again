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

package org.hcgames.kmextra.command;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.system.SpawnFaction;
import org.hcgames.kmextra.KMExtra;
import org.hcgames.kmextra.profile.Profile;
import org.hcgames.kmextra.util.Messages;

@RequiredArgsConstructor
public class ChestCommand implements CommandExecutor{

    private final KMExtra plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
        if(!(sender instanceof Player)){
            sender.sendMessage(Messages.get("Stats-Console"));
            return true;
        }

        Player player = (Player) sender;
        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());

        if(profile.getChest() == null){
            profile.setChest(plugin.getServer().createInventory(player, player.hasPermission("kmextra.largechest") ? 18 : 9));
        }else if(player.hasPermission("kmextra.largechest") && profile.getChest().getSize() == 9){
            Inventory newInventory = plugin.getServer().createInventory(player, 18);
            newInventory.setContents(profile.getChest().getContents());
            profile.setChest(newInventory);
        }

        Faction factionAt = plugin.getFactions().getFactionManager().getFactionAt(player.getLocation());

        if(!(factionAt instanceof SpawnFaction)){
            player.sendMessage(Messages.get("Chest-Spawn-Only"));
            return true;
        }

        player.openInventory(profile.getChest());
        return true;
    }
}
