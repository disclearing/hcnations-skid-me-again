package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.timer.type.StuckTimer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import technology.brk.util.DurationFormatter;
import technology.brk.util.command.CommandArgument;

/**
 * Faction argument used to teleport to a nearby {@link org.bukkit.Location} safely if stuck.
 */
public class FactionStuckArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionStuckArgument(HCFactions plugin) {
        super("stuck", "Teleport to a safe position.", new String[]{"trap", "trapped"});
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-ConsoleOnly"));
            return true;
        }

        Player player = (Player) sender;

        if(HCF.getPlugin().getSOTWManager().isPaused()){
            sender.sendMessage(HCF.getPlugin().getMessages().getString("Commands.Factions.Subcommand.Stuck.SOTW-Paused-Disabled"));
            return true;
        }

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Stuck-OverworldOnly"));
            //sender.sendMessage(ChatColor.RED + "You can only use this command from the overworld.");
            return true;
        }

        StuckTimer stuckTimer = HCF.getPlugin().getTimerManager().getStuckTimer();

        if (!stuckTimer.setCooldown(player, player.getUniqueId())) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Stuck-TimerRunning")
                    .replace("{timerName}", stuckTimer.getDisplayName()));
            //sender.sendMessage(ChatColor.RED + "Your " + stuckTimer.getName() + ChatColor.RED + " timer is already active.");
            return true;
        }

        sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Stuck-Teleporting")
                .replace("{time}", DurationFormatter.getRemaining(stuckTimer.getRemaining(player), true, false))
                .replace("{maxBlocksDistance}", String.valueOf(StuckTimer.MAX_MOVE_DISTANCE)));
        //sender.sendMessage(ChatColor.YELLOW + stuckTimer.getName() + ChatColor.YELLOW + " timer has started. " +
        //        "Teleport will occur in " + ChatColor.AQUA + DurationFormatter.getRemaining(stuckTimer.getRemaining(player), true, false) + ChatColor.YELLOW + ". " +
        //        "This will cancel if you move more than " + StuckTimer.MAX_MOVE_DISTANCE + " blocks.");

        return true;
    }
}
