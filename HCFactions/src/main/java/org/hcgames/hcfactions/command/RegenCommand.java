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

package org.hcgames.hcfactions.command;

import com.doctordark.hcf.Configuration;
import com.doctordark.hcf.HCF;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.RegenStatus;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class RegenCommand implements CommandExecutor, TabCompleter{

    private final HCFactions plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessages().getString("Error-Messages.PlayerOnly"));
            return true;
        }

        Player player = (Player) sender;
        PlayerFaction playerFaction;

        if(!plugin.getFactionManager().hasFaction(player)){
            sender.sendMessage(plugin.getMessages().getString("Error-Messages.NotInFaction"));
            return true;
        }

        playerFaction = plugin.getFactionManager().getPlayerFaction(player);

        RegenStatus regenStatus = playerFaction.getRegenStatus();
        switch (regenStatus) {
            case FULL:
                sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Regen.Full"));
                return true;
            case PAUSED:
                sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Regen.Paused")
                        .replace("{dtrFreezeTimeLeft}", DurationFormatUtils.formatDurationWords(playerFaction.getRemainingRegenerationTime(), true, true)));
                return true;
            case REGENERATING:
                sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Regen.Regenerating")
                        .replace("{regenSymbol}", regenStatus.getSymbol())
                        .replace("{factionDeathsUntilRaidable}", String.valueOf(playerFaction.getDeathsUntilRaidable()))
                        .replace("{factionDTRIncrement}", String.valueOf(plugin.getConfiguration().getFactionDtrUpdateIncrement()))
                        .replace("{factionDTRIncrementWords}", plugin.getConfiguration().getFactionDtrUpdateTimeWords())
                        .replace("{factionDTRETA}", DurationFormatUtils.formatDurationWords(getRemainingRegenMillis(playerFaction), true, true)));
                return true;
        }

        sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Regen.Unknown"));
        return true;
    }

    public long getRemainingRegenMillis(PlayerFaction faction) {
        long millisPassedSinceLastUpdate = System.currentTimeMillis() - faction.getLastDtrUpdateTimestamp();
        double dtrRequired = faction.getMaximumDeathsUntilRaidable() - faction.getDeathsUntilRaidable();
        Configuration configuration = HCF.getPlugin().getConfiguration();
        return (long) ((configuration.getFactionDtrUpdateMillis() / configuration.getFactionDtrUpdateIncrement()) * dtrRequired) - millisPassedSinceLastUpdate;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return Collections.emptyList();
    }

}
