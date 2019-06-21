package com.doctordark.hcf.pvpclass;

import com.doctordark.hcf.HCF;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Represents a class that can apply PVP buffs for players.
 */
public abstract class PvpClass{

    public static final long DEFAULT_MAX_DURATION = TimeUnit.MINUTES.toMillis(8L);

    protected final Set<PotionEffect> passiveEffects = new HashSet<>();
    protected final String name;
    protected final long warmupDelay;

    public PvpClass(String name, long warmupDelay){
        this.name = name;
        this.warmupDelay = warmupDelay;
    }

    /**
     * Gets the name of this {@link PvpClass}.
     *
     * @return the name
     */
    public String getName(){
        return name;
    }

    /**
     * Gets the warmup delay of this {@link PvpClass}.
     *
     * @return the warmup delay in milliseconds
     */
    public long getWarmupDelay(){
        return warmupDelay;
    }

    /**
     * Method called when a {@link Player} equips a {@link PvpClass}.
     *
     * @param player the equipping {@link Player}
     * @return true if successfully equipped
     */
    public boolean onEquip(Player player){
        for(PotionEffect effect : passiveEffects){
            player.addPotionEffect(effect, true);
        }

        player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Messages-Kit-Activated")
                .replace("{kitName}", name));
        return true;
    }

    /**
     * Method called when a {@link Player} unequips a {@link PvpClass}.
     *
     * @param player the unequipping {@link Player}
     */
    public void onUnequip(Player player){
        for(PotionEffect effect : passiveEffects){
            for(PotionEffect active : player.getActivePotionEffects()){
                if(active.getDuration() > DEFAULT_MAX_DURATION && active.getType().equals(effect.getType()) && active.getAmplifier() == effect.getAmplifier()){
                    player.removePotionEffect(effect.getType());
                    break;
                }
            }
        }

        player.sendMessage(HCF.getPlugin().getMessagesOld().getString("Messages-Kit-Deactivated")
                .replace("{kitName}", name));
    }

    //TODO: More automated method (Use something like ArmorType with a getter or something
    public abstract boolean isApplicableFor(Player player);

    public abstract void provideScoreboard(Player player, List<String> lines);
}
