package com.doctordark.hcf.listener;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.eventgame.faction.KothFaction;
import com.doctordark.hcf.faction.event.CaptureZoneEnterEvent;
import com.doctordark.hcf.faction.event.CaptureZoneLeaveEvent;
import com.doctordark.hcf.user.FactionUser;
import com.doctordark.util.SpigotUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.hcgames.hcfactions.event.claim.PlayerClaimEnterEvent;
import org.hcgames.hcfactions.event.faction.FactionCreateEvent;
import org.hcgames.hcfactions.event.faction.FactionRemoveEvent;
import org.hcgames.hcfactions.event.faction.FactionRenameEvent;
import org.hcgames.hcfactions.event.playerfaction.PlayerJoinFactionEvent;
import org.hcgames.hcfactions.event.playerfaction.PlayerJoinedFactionEvent;
import org.hcgames.hcfactions.event.playerfaction.PlayerLeaveFactionEvent;
import org.hcgames.hcfactions.event.playerfaction.PlayerLeftFactionEvent;
import org.hcgames.hcfactions.exception.NoFactionFoundException;
import org.hcgames.hcfactions.faction.ClaimableFaction;
import org.hcgames.hcfactions.faction.Faction;
import org.hcgames.hcfactions.faction.PlayerFaction;
import org.hcgames.hcfactions.structure.FactionMember;
import org.hcgames.hcfactions.structure.RegenStatus;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

//TODO: Move to factions
public class FactionListener implements Listener{

    private static final long FACTION_JOIN_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    private static final String FACTION_JOIN_WAIT_WORDS = DurationFormatUtils.formatDurationWords(FACTION_JOIN_WAIT_MILLIS, true, true);

    private static final String LAND_CHANGED_META_KEY = "landChangedMessage";
    private static final long LAND_CHANGE_MSG_THRESHOLD = 225L;

    private final HCF plugin;

    public FactionListener(HCF plugin){
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRenameMonitor(FactionRenameEvent event){
        Faction faction = event.getFaction();
        if(faction instanceof KothFaction){
            ((KothFaction) faction).getCaptureZone().setName(event.getNewName());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionCreate(FactionCreateEvent event){
        Faction faction = event.getFaction();
        if(faction instanceof PlayerFaction){
            CommandSender sender = event.getSender();
            SpigotUtils.broadcastMessage(target -> {
                return plugin.getMessages().getString("Broadcasts.Faction.Create")
                        .replace("{factionName}", (target == null ? faction.getName() : faction.getFormattedName(target)))
                        .replace("{player}", (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()));

                /*return ChatColor.YELLOW + "Faction " + ChatColor.WHITE + (target == null ? faction.getName() : faction.getDisplayName(target)) + ChatColor.YELLOW + " has been " +
                        ChatColor.GREEN + "created" + ChatColor.YELLOW + " by " +
                        ChatColor.WHITE + (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()) + ChatColor.YELLOW + '.';*/
            });

            if(event.getSender() instanceof Player){
                plugin.getUserManager().getUser(((Player) event.getSender()).getUniqueId()).addPastFaction(event.getFaction().getName());
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoinedFaction(PlayerJoinedFactionEvent event){
        plugin.getUserManager().getUser(event.getPlayerUUID()).addPastFaction(event.getFaction().getName());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRemove(FactionRemoveEvent event){
        Faction faction = event.getFaction();
        if(faction instanceof PlayerFaction){
            CommandSender sender = event.getSender();

            for(FactionMember i : ((PlayerFaction) faction).getOnlineMembers().values()){
                Player player = Bukkit.getServer().getPlayer(i.getUniqueId());

                player.sendMessage(plugin.getMessages().getString("Broadcasts.Faction.Disband")
                        .replace("{factionName}", faction.getFormattedName(player))
                        .replace("{player}", (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName())));
            }

            /*Bukkit.broadcastMessage(new Function<CommandSender, String>() {
                @Nullable
                @Override
                public String apply(@Nullable CommandSender target) {
                    return ChatColor.YELLOW + "Faction " + ChatColor.WHITE + (target == null ? faction.getName() : faction.getDisplayName(target)) + ChatColor.YELLOW + " has been " +
                            ChatColor.RED + "disbanded" + ChatColor.YELLOW + " by " +
                            ChatColor.WHITE + (sender instanceof Player ? ((Player) sender).getDisplayName() : sender.getName()) + ChatColor.YELLOW + '.';
                }
            });*/
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRename(FactionRenameEvent event){
        Faction faction = event.getFaction();

        if(faction instanceof PlayerFaction){
            event.getSender().sendMessage(plugin.getMessages().getString("Commands.Factions.Subcommand.Rename.Renamed")
                    .replace("{oldFactionName}", event.getOldName())
                    .replace("{factionName}", event.getNewName()));


            for(FactionMember member : ((PlayerFaction)event.getFaction()).getMembers().values()){
                FactionUser user = plugin.getUserManager().getUser(member.getUniqueId());
                user.removePastFaction(event.getOldName());
                user.addPastFaction(event.getNewName());
            }
        }

        /*Faction faction = event.getFaction();
        if (faction instanceof PlayerFaction) {
            Bukkit.broadcastMessage(new Function<CommandSender, String>() {
                @Nullable
                @Override
                public String apply(@Nullable CommandSender target) {
                    Relation relation = faction.getRelation(target);
                    return ChatColor.YELLOW + "Faction " + relation.toChatColour() + event.getOriginalName() + ChatColor.YELLOW + " has been " +
                            ChatColor.AQUA + "renamed" + ChatColor.YELLOW + " to " +
                            relation.toChatColour() + event.getNewName() + ChatColor.YELLOW + '.';
                }
            });
        }*/
    }

    private long getLastLandChangedMeta(Player player){
        MetadataValue value = player.getMetadata(LAND_CHANGED_META_KEY).isEmpty() ? null : player.getMetadata(LAND_CHANGED_META_KEY).get(0);
        long millis = System.currentTimeMillis();
        long remaining = value == null ? 0L : value.asLong() - millis;
        if(remaining <= 0L){ // update the metadata.
            player.setMetadata(LAND_CHANGED_META_KEY, new FixedMetadataValue(plugin, millis + LAND_CHANGE_MSG_THRESHOLD));
        }

        return remaining;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneEnter(CaptureZoneEnterEvent event){
        Player player = event.getPlayer();
        if(getLastLandChangedMeta(player) > 0L) return; // delay before re-messaging.

        player.sendMessage(ChatColor.YELLOW + "Now entering capture zone: " + event.getCaptureZone().getDisplayName() +
                ChatColor.YELLOW + '(' + event.getFaction().getName() + ChatColor.YELLOW + ')');
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneLeave(CaptureZoneLeaveEvent event){
        Player player = event.getPlayer();
        if(getLastLandChangedMeta(player) > 0L) return; // delay before re-messaging.

        player.sendMessage(ChatColor.YELLOW + "Now leaving capture zone: " + event.getCaptureZone().getDisplayName() +
                ChatColor.YELLOW + '(' + event.getFaction().getName() + ChatColor.YELLOW + ')');
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerClaimEnter(PlayerClaimEnterEvent event){
        Faction toFaction = event.getToFaction();
        Player player = event.getPlayer();

        if(toFaction.isSafezone()){
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.setSaturation(4.0F);
        }

        if(toFaction instanceof ClaimableFaction && ((ClaimableFaction)toFaction).isSnowfall()){
            player.setPlayerWeather(WeatherType.DOWNFALL);
        }else{
            player.resetPlayerWeather();
        }

        if(this.getLastLandChangedMeta(player) <= 0L){ // delay before re-messaging.
            player.sendMessage(plugin.getMessagesOld().getString("Messages-Factions-EnterLand")
                    .replace("{factionName}", event.getToFaction().getFormattedName(player))
                    .replace("{factionLeft}", (event.getFromFaction() == null ? "" : event.getFromFaction().getFormattedName(player))));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLeftFaction(PlayerLeftFactionEvent event){
        Optional<Player> optionalPlayer = event.getPlayer();
        optionalPlayer.ifPresent(player -> plugin.getUserManager().getUser(player.getUniqueId()).setLastFactionLeaveMillis(System.currentTimeMillis()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPreFactionJoin(PlayerJoinFactionEvent event){
        PlayerFaction playerFaction = event.getFaction();
        Optional<Player> optionalPlayer = event.getPlayer();
        if(optionalPlayer.isPresent()){
            Player player = optionalPlayer.get();

            if(!plugin.getConfiguration().isKitMap() && !plugin.getEotwHandler().isEndOfTheWorld() && playerFaction.getRegenStatus() == RegenStatus.PAUSED){
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot join factions that are not regenerating DTR.");
                return;
            }

            long difference = (plugin.getUserManager().getUser(player.getUniqueId()).getLastFactionLeaveMillis() - System.currentTimeMillis()) + FACTION_JOIN_WAIT_MILLIS;
            if(difference > 0L && !player.hasPermission("hcf.faction.argument.staff.forcejoin")){
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot join factions after just leaving within " + FACTION_JOIN_WAIT_WORDS + ". " +
                        "You gotta wait another " + DurationFormatUtils.formatDurationWords(difference, true, true) + '.');
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionLeave(PlayerLeaveFactionEvent event){
        if(event.isForce() || event.isKick()){
            return;
        }

        PlayerFaction playerFaction = event.getFaction();
        Optional<Player> optional = event.getPlayer();
        if(optional.isPresent()){
            Player player = optional.get();
            if(plugin.getFactions().getFactionManager().getFactionAt(player.getLocation()) == playerFaction){
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot leave your faction whilst you remain in its' territory.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        PlayerFaction playerFaction;

        Faction factionAt = plugin.getFactions().getFactionManager().getFactionAt(player.getLocation());
        if(factionAt instanceof ClaimableFaction && ((ClaimableFaction)factionAt).isSnowfall()){
            player.setPlayerWeather(WeatherType.DOWNFALL);
        }else{
            player.resetPlayerWeather();
        }

        try{
            playerFaction = plugin.getFactions().getFactionManager().getPlayerFaction(player);
            if(playerFaction != null){
                playerFaction.sendInformation(player);
                playerFaction.broadcast(ChatColor.GOLD + "Member Online: " + ChatColor.GREEN + playerFaction.getMember(player).getRole().getAstrix() + player.getName() + ChatColor.GOLD + '.',
                        player.getUniqueId());
            }
        }catch(NoFactionFoundException ignored){
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        PlayerFaction playerFaction;
        try{
            playerFaction = plugin.getFactions().getFactionManager().getPlayerFaction(player);
            if(playerFaction != null){
                playerFaction.broadcast(ChatColor.GOLD + "Member Offline: " + ChatColor.GREEN + playerFaction.getMember(player).getRole().getAstrix() + player.getName() + ChatColor.GOLD + '.');
            }
        }catch(NoFactionFoundException ignored){
        }
    }
}
