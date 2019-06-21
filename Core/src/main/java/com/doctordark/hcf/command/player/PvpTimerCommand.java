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
import com.doctordark.hcf.timer.type.InvincibilityTimer;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import technology.brk.util.BukkitUtils;
import technology.brk.util.DurationFormatter;

import java.util.Collections;
import java.util.List;

/**
 * Command used to manage the {@link InvincibilityTimer} of {@link Player}s.
 */
public class PvpTimerCommand implements CommandExecutor, TabCompleter{

    private static final ImmutableList<String> COMPLETIONS = ImmutableList.of("enable", "time");
    private final HCF plugin;

    public PvpTimerCommand(HCF plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(!(sender instanceof Player)){
            sender.sendMessage(plugin.getMessages().getString("Error-Messages.PlayerOnly"));
            return true;
        }

        Player player = (Player) sender;
        InvincibilityTimer pvpTimer = plugin.getTimerManager().getInvincibilityTimer();

        if(args.length < 1){
            printUsage(sender, label, pvpTimer);
            return true;
        }

        if(args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("off")){
            if(pvpTimer.getRemaining(player) <= 0L){
                sender.sendMessage(plugin.getMessages().getString("Commands.PvPTimer.Timer.NotActive")
                        .replace("{timerName}", pvpTimer.getName()));
                return true;
            }

            sender.sendMessage(plugin.getMessages().getString("Commands.PvPTimer.Timer.Disabled")
                    .replace("{timerName}", pvpTimer.getName()));
            pvpTimer.clearCooldown(player);
            return true;
        }

        if(args[0].equalsIgnoreCase("remaining") || args[0].equalsIgnoreCase("time") || args[0].equalsIgnoreCase("left") || args[0].equalsIgnoreCase("check")){
            long remaining = pvpTimer.getRemaining(player);
            if(remaining <= 0L){
                sender.sendMessage(plugin.getMessages().getString("Commands.PvPTimer.Timer.NotActive")
                        .replace("{timerName}", pvpTimer.getName()));
                return true;
            }

            sender.sendMessage(plugin.getMessages().getString("Commands.PvPTimer.Active.Output")
                    .replace("{timerName}", pvpTimer.getName())
                    .replace("{timerTimeRemaining}", DurationFormatter.getRemaining(remaining, true, false))
                    .replace("{isPausedText}", (pvpTimer.isPaused(player) ? plugin.getMessages().getString("Commands.PvPTimer.Active.Paused") : "")));
            return true;
        }

        printUsage(sender, label, pvpTimer);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        return args.length == 1 ? BukkitUtils.getCompletions(args, COMPLETIONS) : Collections.emptyList();
    }

    /**
     * Prints the usage of this command to a sender.
     *
     * @param sender the sender to print for
     * @param label  the label used for command
     */
    private void printUsage(CommandSender sender, String label, InvincibilityTimer pvpTimer){
        sender.sendMessage(plugin.getMessages().getString("Commands.PvPTimer.Usage")
                .replace("{timerName}", pvpTimer.getName())
                .replace("{commandLabel}", label));
    }
}
