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

package technology.brk.staff.command;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import technology.brk.staff.Staff;
import technology.brk.staff.methods.ReportManager;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor @Deprecated
public class ReportCommand implements CommandExecutor{

    private final Staff plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]){
        if(commandLabel.equalsIgnoreCase("report")){
            if(!sender.hasPermission("staff.command.report") && sender instanceof Player){
                sender.sendMessage(plugin.getMessages().getString("Messages-NoPermission"));
                return true;
            }

            if(!(sender instanceof Player)){
                sender.sendMessage("player only :)");
                return true;
            }

            Player player = (Player) sender;

            if(!(args.length >= 2)){
                sender.sendMessage(plugin.getMessages().getString("Commands-Report-InvalidUsage"));
                return true;
            }

            Player targetPlayer = Bukkit.getServer().getPlayer(args[0]);

            if(targetPlayer == null || !targetPlayer.isOnline()){
                sender.sendMessage(plugin.getMessages().getString("Messages-InvalidPlayer")
                        .replace("{player}", args[0]));
                return true;
            }

            if(targetPlayer.getUniqueId().equals(player.getUniqueId()) && plugin.getReportManager().hasReportedSelf(player.getName())){
                sender.sendMessage(plugin.getMessages().getString("Commands-Report-AlreadyReportedSelf"));
                return true;
            }

            if(plugin.getReportManager().inCooldown(player)){
                if(!(System.currentTimeMillis() >= (plugin.getReportManager().getCooldown(player) + TimeUnit.SECONDS.toMillis(plugin.getConfig().getInt("Reports.Cooldown-Time"))))){
                    sender.sendMessage(plugin.getMessages().getString("Commands-Report-Cooldown")
                            .replace("{time}", String.valueOf(TimeUnit.MILLISECONDS.toSeconds((plugin.getReportManager().getCooldown(player) + TimeUnit.SECONDS.toMillis(plugin.getConfig().getInt("Reports.Cooldown-Time"))) - System.currentTimeMillis()))));
                    return true;
                }
            }

            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 1; i < args.length; i++){
                stringBuilder.append(args[i]).append(" ");
            }

            ReportManager.Report report = plugin.getReportManager().buildReport(targetPlayer.getName(), sender.getName(), stringBuilder.toString());

            String broadcastMessage = plugin.getMessages().getString("Commands-Report-Broadcast")
                    .replace("{reporter}", report.getReporter())
                    .replace("{player}", report.getPlayer())
                    .replace("{reason}", report.getReason())
                    .replace("{report-id}", String.valueOf(report.getId()));

            for(Player i : Bukkit.getServer().getOnlinePlayers()){
                if(!i.hasPermission("prime.staff")){
                    continue;
                }

                i.sendMessage(broadcastMessage);
            }

            sender.sendMessage(plugin.getMessages().getString("Commands-Report-Sent"));

            plugin.getReportManager().addReport(report);
            plugin.getReportManager().startCooldown(player);

            if(targetPlayer.getUniqueId().equals(player.getUniqueId())){
                plugin.getReportManager().addReportedSelf(player.getName());
            }
            return true;
        }

        if(commandLabel.equalsIgnoreCase("viewreports")){
            if(!(sender instanceof Player)){
                sender.sendMessage(plugin.getMessages().getString("Error-Messages.Player-Only"));
                return true;
            }

            if(!sender.hasPermission("staff.command.viewreports")){
                sender.sendMessage(plugin.getMessages().getString("Messages-NoPermission"));
                return true;
            }

            int page = 1;

            if(args.length >= 1){
                try{
                    page = Integer.valueOf(args[0]);
                }catch(NumberFormatException exception){
                    sender.sendMessage(plugin.getMessages().getString("Error-Messages.Invalid-Number")
                            .replace("{number}", args[0]));
                    return true;
                }
            }

            if(plugin.getReportManager().getPagesCount() == 0){
                sender.sendMessage(plugin.getMessages().getString("Commands-ViewReports-NoReports"));
                return true;
            }

            Inventory inventory = plugin.getReportManager().getGUI(page);

            if(inventory == null){
                sender.sendMessage(plugin.getMessages().getString("Commands-ViewReports-InvalidPage")
                        .replace("{maxPages}", String.valueOf(plugin.getReportManager().getPagesCount())));
                return true;
            }

            Player player = (Player) sender;

            player.openInventory(inventory);
            plugin.getReportManager().openPage(player.getUniqueId(), page);

            sender.sendMessage(plugin.getMessages().getString("Commands-ViewReports-Opening")
                    .replace("{page}", String.valueOf(page)));
            return true;
        }
        return true;
    }
}
