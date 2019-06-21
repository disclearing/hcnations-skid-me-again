package org.hcgames.hcfactions.command.argument;

import com.doctordark.hcf.HCF;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.PlayerFaction;
import technology.brk.util.command.CommandArgument;

import java.util.UUID;

public class FactionClaimArgument extends CommandArgument {

    private final HCFactions plugin;

    public FactionClaimArgument(HCFactions plugin) {
        super("claim", "Claim land in the Wilderness.", new String[]{"claimland"});
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
        UUID uuid = player.getUniqueId();

        PlayerFaction playerFaction;
        try {
            playerFaction = plugin.getFactionManager().getPlayerFaction(uuid);
        } catch (NoFactionFoundException e) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Global-NotInFaction"));
            return true;
        }


        if (playerFaction.isRaidable()) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Claim-NoClaimRaidable"));
            return true;
        }

        PlayerInventory inventory = player.getInventory();

        if (inventory.contains(plugin.getClaimHandler().getClaimWand())) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Claim-ClaimWandInvAlready"));
            return true;
        }

        if (inventory.contains(plugin.getClaimHandler().getClaimWand())) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Claim-SubClaimInInvError"));
            return true;
        }

        if (!inventory.addItem(plugin.getClaimHandler().getClaimWand()).isEmpty()) {
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Claim-InvFull"));
            return true;
        }

        sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Claim-Added")
                .replace("{commandLabel}", label));

        return true;
    }
}
