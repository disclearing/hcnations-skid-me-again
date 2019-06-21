package org.hcgames.hcfactions.structure;

import org.bukkit.entity.Player;
import org.hcgames.hcfactions.faction.Faction;

/**
 * Represents the {@link Role} of a {@link Player} in a {@link Faction}.
 */
public enum Role {

    LEADER("Leader", "***"),
    COLEADER("Co Leader", "**"),
    CAPTAIN("Captain", "*"),
    MEMBER("Member", "");

    private final String name;
    private final String astrix;

    Role(String name, String astrix) {
        this.name = name;
        this.astrix = astrix;
    }

    /**
     * Gets the name of this {@link Role}.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the astrix of this {@link Role}.
     *
     * @return the astrix
     */
    public String getAstrix() {
        return astrix;
    }
}
