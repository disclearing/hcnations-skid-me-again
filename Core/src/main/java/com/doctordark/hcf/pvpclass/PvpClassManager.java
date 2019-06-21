package com.doctordark.hcf.pvpclass;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.pvpclass.archer.ArcherClass;
import com.doctordark.hcf.pvpclass.archer.Marks;
import com.doctordark.hcf.pvpclass.bard.BardClass;
import com.doctordark.hcf.pvpclass.event.PvpClassEquipEvent;
import com.doctordark.hcf.pvpclass.event.PvpClassUnequipEvent;
import com.doctordark.hcf.pvpclass.type.MinerClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import technology.brk.util.DateTimeFormats;
import technology.brk.util.DurationFormatter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PvpClassManager implements Listener{//TODO Store pvp classes as Name, PvpClass - not list.

    // Mapping to get the PVP Class a player has equipped.
    private final Map<UUID, PvpClass> equippedClassMap = new HashMap<>();

    private final List<PvpClass> pvpClasses = new ArrayList<>();
    private final Map<String, PvpClass> classToNameMap = new HashMap<>();
    private final HCF plugin;

    public PvpClassManager(HCF plugin){
        //FIXME: Ew but nothing I can do at this time :/
        ArcherClass archerClass = new ArcherClass(plugin);
        classToNameMap.put("archer", archerClass);
        pvpClasses.add(archerClass);

        BardClass bardClass = new BardClass(plugin);
        classToNameMap.put("bard", bardClass);
        pvpClasses.add(bardClass);

        MinerClass minerClass = new MinerClass(plugin);
        classToNameMap.put("miner", minerClass);
        pvpClasses.add(minerClass);

        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
        for(PvpClass pvpClass : pvpClasses){
            if(pvpClass instanceof Listener){
                plugin.getServer().getPluginManager().registerEvents((Listener) pvpClass, plugin);
            }
        }
    }

    public void onDisable(){
        for(Map.Entry<UUID, PvpClass> entry : new HashMap<>(equippedClassMap).entrySet()){
            this.setEquippedClass(Bukkit.getPlayer(entry.getKey()), null);
        }

        this.pvpClasses.clear();
        this.equippedClassMap.clear();
        this.classToNameMap.clear();
    }

    public PvpClass getClass(String name){
        return classToNameMap.get(name);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event){
        setEquippedClass(event.getEntity(), null);
    }

    /**
     * Gets the {@link PvpClass}es held by this manager
     *
     * @return set of {@link PvpClass}es
     */
    public Collection<PvpClass> getPvpClasses(){
        return pvpClasses;
    }

    /**
     * Gets the equipped {@link PvpClass} of a {@link Player}.
     *
     * @param player the {@link Player} to get for
     * @return the equipped {@link PvpClass}
     */
    public PvpClass getEquippedClass(Player player){
        synchronized(equippedClassMap){
            return equippedClassMap.get(player.getUniqueId());
        }
    }

    public boolean hasClassEquipped(Player player, PvpClass pvpClass){
        return getEquippedClass(player) == pvpClass;
    }

    /**
     * Sets the equipped {@link PvpClass} of a {@link Player}.
     *
     * @param player   the {@link Player} to set for
     * @param pvpClass the class to equip or null to un-equip active
     */
    public void setEquippedClass(Player player, @Nullable PvpClass pvpClass){
        if(player == null){
            return;
        }

        if(pvpClass == null){
            PvpClass equipped = this.equippedClassMap.remove(player.getUniqueId());
            if(equipped != null){
                equipped.onUnequip(player);
                Bukkit.getPluginManager().callEvent(new PvpClassUnequipEvent(player, equipped));

            }
        }else if(pvpClass.onEquip(player) && pvpClass != this.getEquippedClass(player)){
            equippedClassMap.put(player.getUniqueId(), pvpClass);
            Bukkit.getPluginManager().callEvent(new PvpClassEquipEvent(player, pvpClass));
        }
    }

    public void provideScoreboard(Player player, List<String> lines){
        PvpClass playerPvPClass = plugin.getPvpClassManager().getEquippedClass(player);

        if(playerPvPClass != null){
            lines.add(plugin.getMessages().getString("scoreboard.classes.active").replace("{class}", playerPvPClass.getName()));
            playerPvPClass.provideScoreboard(player, lines);
        }

        ArcherClass archerClass = (ArcherClass) pvpClasses.get(0);
        for(UUID archer : archerClass.getAllMarks().keySet()){
            if(!player.getUniqueId().equals(archer)){
                Marks archerMarks = archerClass.getMarks(archer);

                if(archerMarks.isMarked(player)){
                    for(String message : plugin.getMessages().getString("scoreboard.classes.archer.marked").split("\n")){
                        lines.add(message.replace("{archer}", Bukkit.getServer().getPlayer(archerMarks.getArcherUUID()).
                                getName()).replace("{expires}", DurationFormatter.getRemaining(archerMarks.getTimer().getRemaining(player), true)));
                    }
                }

                break;
            }
        }
    }

}
