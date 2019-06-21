package com.doctordark.hcf.faction.event;

import org.bukkit.event.Event;
import org.hcgames.hcfactions.faction.Faction;

import java.util.Objects;

/**
 * Represents a faction related event
 */
public abstract class FactionEvent extends Event{

    protected final Faction faction;

    public FactionEvent(final Faction faction){
        this.faction = Objects.requireNonNull(faction, "Faction cannot be null");
    }

    FactionEvent(final Faction faction, boolean async){
        super(async);
        this.faction = Objects.requireNonNull(faction, "Faction cannot be null");
    }

    /**
     * Returns the {@link Faction} involved in this event
     *
     * @return the {@link Faction} that is involved in this event
     */
    public Faction getFaction(){
        return faction;
    }
}
