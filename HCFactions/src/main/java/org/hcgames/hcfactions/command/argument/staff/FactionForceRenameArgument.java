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

package org.hcgames.hcfactions.command.argument.staff;

import com.doctordark.hcf.HCF;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.hcgames.hcfactions.HCFactions;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.manager.SearchCallback;
import technology.brk.util.JavaUtils;
import technology.brk.util.command.CommandArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FactionForceRenameArgument extends CommandArgument{

    private final HCFactions plugin;

    public FactionForceRenameArgument(final HCFactions plugin) {
        super("forcename", "Forces a rename of a faction.");
        this.plugin = plugin;
        this.permission = "hcf.command.faction.argument." + getName();
    }

    @Override
    public String getUsage(String label) {
        return '/' + label + ' ' + getName() + " <oldName> <newName>";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage(label));
            return true;
        }

        String newName = args[2];

        if (plugin.getConfiguration().getFactionDisallowedNames().contains(newName)) {
            //sender.sendMessage(ChatColor.RED + "'" + newName + "' is a blocked faction name.");
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Rename-BlockedName")
                    .replace("{factionName}", newName));
            return true;
        }

        int value = plugin.getConfiguration().getFactionNameMinCharacters();

        if (newName.length() < value) {
            //sender.sendMessage(ChatColor.RED + "Faction names must have at least " + value + " characters.");
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Rename-MinimumChars")
                    .replace("{minChars}", String.valueOf(value)));
            return true;
        }

        value = plugin.getConfiguration().getFactionNameMaxCharacters();

        if (newName.length() > value) {
            //sender.sendMessage(ChatColor.RED + "Faction names cannot be longer than " + value + " characters.");
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Rename-MaximumChars")
                    .replace("{maxChars}", String.valueOf(value)));
            return true;
        }

        if (!JavaUtils.isAlphanumeric(newName)) {
            //sender.sendMessage(ChatColor.RED + "Faction names may only be alphanumeric.");
            sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Rename-MustBeAlphanumeric"));
            return true;
        }

        try {
            if (plugin.getFactionManager().getFaction(newName) != null) {
                //sender.sendMessage(ChatColor.RED + "Faction " + newName + ChatColor.RED + " already exists.");
                sender.sendMessage(HCF.getPlugin().getMessagesOld().getString("Commands-Factions-Rename-NameAlreadyExists")
                        .replace("{factionNewName}", newName));
                return true;
            }
        } catch (NoFactionFoundException ignored) {}


        plugin.getFactionManager().advancedSearch(args[1], Faction.class, new SearchCallback<Faction>() {
            @Override
            public void onSuccess(Faction faction){
                String oldName = faction.getName();
                if(faction.setName(newName, sender)){
                    BukkitCommand.broadcastCommandMessage(sender, ChatColor.YELLOW + "Renamed " + oldName + " to " + faction.getName(), true);
                }
            }

            @Override
            public void onFail(FailReason reason) {
                sender.sendMessage(plugin.getMessages().getString("commands.error.faction_not_found", args[1]));
            }
        });

        return true;
    }

}
