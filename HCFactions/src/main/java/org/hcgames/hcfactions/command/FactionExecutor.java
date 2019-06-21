package org.hcgames.hcfactions.command;

import com.doctordark.hcf.HCF;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.command.argument.FactionAcceptArgument;
import org.hcgames.hcfactions.command.argument.FactionAllyArgument;
import org.hcgames.hcfactions.command.argument.FactionAnnouncementArgument;
import org.hcgames.hcfactions.command.argument.FactionChatArgument;
import org.hcgames.hcfactions.command.argument.FactionClaimArgument;
import org.hcgames.hcfactions.command.argument.FactionClaimChunkArgument;
import org.hcgames.hcfactions.command.argument.FactionClaimsArgument;
import org.hcgames.hcfactions.command.argument.FactionCreateArgument;
import org.hcgames.hcfactions.command.argument.FactionDemoteArgument;
import org.hcgames.hcfactions.command.argument.FactionDepositArgument;
import org.hcgames.hcfactions.command.argument.FactionDisbandArgument;
import org.hcgames.hcfactions.command.argument.FactionFocusArgument;
import org.hcgames.hcfactions.command.argument.FactionHelpArgument;
import org.hcgames.hcfactions.command.argument.FactionHomeArgument;
import org.hcgames.hcfactions.command.argument.FactionInviteArgument;
import org.hcgames.hcfactions.command.argument.FactionInvitesArgument;
import org.hcgames.hcfactions.command.argument.FactionKickArgument;
import org.hcgames.hcfactions.command.argument.FactionLeaderArgument;
import org.hcgames.hcfactions.command.argument.FactionLeaveArgument;
import org.hcgames.hcfactions.command.argument.FactionListArgument;
import org.hcgames.hcfactions.command.argument.FactionLivesArgument;
import org.hcgames.hcfactions.command.argument.FactionMapArgument;
import org.hcgames.hcfactions.command.argument.FactionMessageArgument;
import org.hcgames.hcfactions.command.argument.FactionOpenArgument;
import org.hcgames.hcfactions.command.argument.FactionPastFactionsArgument;
import org.hcgames.hcfactions.command.argument.FactionPromoteArgument;
import org.hcgames.hcfactions.command.argument.FactionRemoveCooldownArgument;
import org.hcgames.hcfactions.command.argument.FactionRenameArgument;
import org.hcgames.hcfactions.command.argument.FactionReviveArgument;
import org.hcgames.hcfactions.command.argument.FactionSetHomeArgument;
import org.hcgames.hcfactions.command.argument.FactionShowArgument;
import org.hcgames.hcfactions.command.argument.FactionSnowArgument;
import org.hcgames.hcfactions.command.argument.FactionStuckArgument;
import org.hcgames.hcfactions.command.argument.FactionUnallyArgument;
import org.hcgames.hcfactions.command.argument.FactionUnclaimArgument;
import org.hcgames.hcfactions.command.argument.FactionUninviteArgument;
import org.hcgames.hcfactions.command.argument.FactionWithdrawArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionBanArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionClaimForArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionClearClaimsArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionForceDemoteArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionForceJoinArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionForceKickArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionForceLeaderArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionForcePromoteArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionForceRenameArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionForceUnclaimHereArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionMuteArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionReloadArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionRemoveArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionSetDeathbanMultiplierArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionSetDtrArgument;
import org.hcgames.hcfactions.command.argument.staff.FactionSetDtrRegenArgument;
import technology.brk.util.command.ArgumentExecutor;
import technology.brk.util.command.CommandArgument;

/**
 * Class to handle the command and tab completion for the faction command.
 */
public class FactionExecutor extends ArgumentExecutor {

    private final CommandArgument helpArgument;

    public FactionExecutor(HCFactions plugin) {
        super("faction");

        addArgument(new FactionAcceptArgument(plugin));
        addArgument(new FactionAllyArgument(plugin));
        addArgument(new FactionAnnouncementArgument(plugin));
        addArgument(new FactionChatArgument(plugin));
        //TODO addArgument(new FactionChatSpyArgument(plugin));
        addArgument(new FactionClaimArgument(plugin));
        addArgument(new FactionClaimChunkArgument(plugin));
        addArgument(new FactionClaimForArgument(plugin));
        addArgument(new FactionClaimsArgument(plugin));
        addArgument(new FactionClearClaimsArgument(plugin));
        addArgument(new FactionCreateArgument(plugin));
        addArgument(new FactionDemoteArgument(plugin));
        addArgument(new FactionDepositArgument(plugin));
        addArgument(new FactionDisbandArgument(plugin));
        addArgument(new FactionSetDtrRegenArgument(plugin));
        addArgument(new FactionForceDemoteArgument(plugin));
        addArgument(new FactionForceJoinArgument(plugin));
        addArgument(new FactionForceKickArgument(plugin));
        addArgument(new FactionForceLeaderArgument(plugin));
        addArgument(new FactionForcePromoteArgument(plugin));
        addArgument(new FactionForceUnclaimHereArgument(plugin));
        addArgument(helpArgument = new FactionHelpArgument(this, plugin));
        addArgument(new FactionHomeArgument(this, plugin));
        addArgument(new FactionInviteArgument(plugin));
        addArgument(new FactionInvitesArgument(plugin));
        addArgument(new FactionKickArgument(plugin));
        addArgument(new FactionLeaderArgument(plugin));
        addArgument(new FactionLeaveArgument(plugin));
        addArgument(new FactionListArgument(plugin));
        addArgument(new FactionMapArgument(plugin));
        addArgument(new FactionMessageArgument(plugin));
        addArgument(new FactionMuteArgument(plugin));
        addArgument(new FactionBanArgument(plugin));
        addArgument(new FactionOpenArgument(plugin));
        addArgument(new FactionRemoveArgument(plugin));
        addArgument(new FactionRenameArgument(plugin));
        addArgument(new FactionPromoteArgument(plugin));
        addArgument(new FactionSetDtrArgument(plugin));
        addArgument(new FactionSetDeathbanMultiplierArgument(plugin));
        addArgument(new FactionSetHomeArgument(plugin));
        addArgument(new FactionShowArgument(plugin));
        addArgument(new FactionStuckArgument(plugin));
        addArgument(new FactionUnclaimArgument(plugin));
        addArgument(new FactionUnallyArgument(plugin));
        addArgument(new FactionUninviteArgument(plugin));
        addArgument(new FactionWithdrawArgument(plugin));
        addArgument(new FactionLivesArgument(plugin));
        addArgument(new FactionReviveArgument(plugin));
        addArgument(new FactionFocusArgument(plugin));
        addArgument(new FactionRemoveCooldownArgument(plugin));
        addArgument(new FactionReloadArgument(plugin));
        addArgument(new FactionForceRenameArgument(plugin));
        addArgument(new FactionSnowArgument(plugin));
        addArgument(new FactionPastFactionsArgument(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            helpArgument.onCommand(sender, command, label, args);
            return true;
        }

        CommandArgument argument = getArgument(args[0]);
        if (argument != null) {
            String permission = argument.getPermission();
            if (permission == null || sender.hasPermission(permission)) {
                argument.onCommand(sender, command, label, args);
                return true;
            }
        }else{
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Unknown-Subcommand")
                    .replace("{subCommand}", args[0])
                    .replace("{commandLabel}", command.getName()));
            return true;
        }

        helpArgument.onCommand(sender, command, label, args);
        return true;
    }
}
