package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.user.FactionUser;
import com.doctordark.hcf.visualise.VisualType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.claim.Claim;
import org.hcgames.hcfactions.faction.LandMap;
import technology.brk.base.GuavaCompat;
import technology.brk.util.command.CommandArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Faction argument used to view a interactive map of {@link Claim}s.
 */
public class FactionMapArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionMapArgument(HCFactions plugin) {
        super("map", "View all claims around your chunk.");
        this.plugin = plugin;
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " [factionName]";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-ConsoleOnly"));
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        final FactionUser factionUser = HCF.getPlugin().getUserManager().getUser(uuid);
        final VisualType visualType;
        if (args.length < 2) {
            visualType = VisualType.CLAIM_MAP;
        } else if ((visualType = GuavaCompat.getIfPresent(VisualType.class, args[1]).orElse(VisualType.NONE)) == VisualType.NONE) {
            //player.sendMessage(ChatColor.RED + "Visual type " + args[1] + " not found.");
            player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Map-VisualTypeNotFound")
                    .replace("{visualType}", args[1]));
            return true;
        }

        boolean newShowingMap = !(player.hasMetadata("claimMap") && player.getMetadata("claimMap").get(0).asBoolean());
        if (newShowingMap) {
            if (!LandMap.updateMap(player, plugin, visualType, true)) {
                return true;
            }
        } else {
            HCF.getPlugin().getVisualiseHandler().clearVisualBlocks(player, visualType, null);
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Map-DisabledClaimPillars"));
            //sender.sendMessage(ChatColor.RED + "Claim pillars are no longer shown.");
        }

        player.setMetadata("claimMap", new FixedMetadataValue(plugin, newShowingMap));
        return true;
    }

    private static List<String> visualTypes;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        return visualTypes;
    }

    static {
        VisualType[] values = VisualType.values();
        visualTypes = new ArrayList<>(values.length);
        for (VisualType visualType : values) {
            visualTypes.add(visualType.name());
        }
    }

}
