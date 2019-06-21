package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.timer.PlayerTimer;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.command.FactionExecutor;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import technology.brk.util.DurationFormatter;
import technology.brk.util.command.CommandArgument;

import java.util.Optional;
import java.util.UUID;

/**
 * Faction argument used to teleport to {@link Faction} home {@link Location}s.
 */

//TODO: Core hooks, own timer system etc etc + event
public class FactionHomeArgument extends CommandArgument {

    private final FactionExecutor factionExecutor;
    private final HCFactions plugin;

    public FactionHomeArgument(FactionExecutor factionExecutor, HCFactions plugin) {
        super("home", "Teleport to the faction home.");
        this.factionExecutor = factionExecutor;
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

        if (args.length >= 2 && args[1].equalsIgnoreCase("set")) {
            factionExecutor.getArgument("sethome").onCommand(sender, command, label, args);
            return true;
        }

        UUID uuid = player.getUniqueId();

        PlayerTimer timer = HCF.getPlugin().getTimerManager().getEnderPearlTimer();
        long remaining = timer.getRemaining(player);

        if (remaining > 0L) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Home-TimerActive")
                    .replace("{timerName}", timer.getName()));

            return true;
        }

        if ((remaining = (timer = HCF.getPlugin().getTimerManager().getCombatTimer()).getRemaining(player)) > 0L) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Home-TimerActive")
                    .replace("{timerName}", timer.getDisplayName()));

            return true;
        }

        PlayerFaction playerFaction;
        try {
            playerFaction = plugin.getFactionManager().getPlayerFaction(uuid);
        } catch (NoFactionFoundException e) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Global-NotInFaction"));
            return true;
        }

        Optional<Location> home = playerFaction.getHome();

        if (!home.isPresent()) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Home-NoFactionHomeSet"));
            return true;
        }

        if (plugin.getConfiguration().getMaxHeightFactionHome() != -1 && home.get().getY() > plugin.getConfiguration().getMaxHeightFactionHome()) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Home-HomeAboveHeightLimit")
                    .replace("{factionHomeHeightLimit}", String.valueOf(plugin.getConfiguration().getMaxHeightFactionHome()))
                    .replace("{factionHomeX}", String.valueOf(home.get().getBlockX()))
                    .replace("{factionHomeZ}", String.valueOf(home.get().getBlockZ())));

            return true;
        }

        Faction factionAt = plugin.getFactionManager().getFactionAt(player.getLocation());

        if (factionAt != playerFaction && factionAt instanceof PlayerFaction && plugin.getConfiguration().isAllowTeleportingInEnemyTerritory()) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Home-InEnemyClaim")
                    .replace("{commandLabel}", label));
            return true;
        }

        long millis;
        if (factionAt.isSafezone()) {
            millis = 0L;
        } else {
            String name;
            switch (player.getWorld().getEnvironment()) {
                case THE_END:
                    name = "End";
                    millis = plugin.getConfiguration().getFactionHomeTeleportDelayEndMillis();
                    break;
                case NETHER:
                    name = "Nether";
                    millis = plugin.getConfiguration().getFactionHomeTeleportDelayNetherMillis();
                    break;
                case NORMAL:
                    name = "Overworld";
                    millis = plugin.getConfiguration().getFactionHomeTeleportDelayOverworldMillis();
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognised environment");
            }

            if (millis == -1L) {
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Home-DisabledInWorld")
                        .replace("{worldName}", name));
                return true;
            }
        }

        if (factionAt != playerFaction && factionAt instanceof PlayerFaction) {
            millis *= 2L;
        }

        HCF.getPlugin().getTimerManager().getTeleportTimer().teleport(player, home.get(), millis,
                HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Home-Teleporting")
                        .replace("{time}", DurationFormatter.getRemaining(millis, true, false)),
                PlayerTeleportEvent.TeleportCause.COMMAND);

        return true;
    }
}
