package com.doctordark.hcf.economy;

import java.util.Map;
import java.util.UUID;

/**
 * Handles balances of players.
 */
public interface EconomyManager{

    String ECONOMY_SYMBOL = "$";

    /**
     * Gets the map of economy balances.
     *
     * @return the map of economy balances
     */
    Map<UUID, Double> getBalanceMap();

    /**
     * Gets the balance of a player.
     *
     * @param uuid the uuid of player to get for
     * @return the balance of the player
     */
    double getBalance(UUID uuid);

    /**
     * Sets the balance of a player.
     *
     * @param uuid   the uuid of player to set for
     * @param amount the amount to set
     * @return the new balance of player
     */
    double setBalance(UUID uuid, double amount);

    /**
     * Adds to the balance of a player.
     *
     * @param uuid   the uuid of player to add for
     * @param amount the amount to add
     * @return the new balance of player
     */
    double addBalance(UUID uuid, double amount);

    /**
     * Takes from the balance of a player.
     *
     * @param uuid   the uuid of player to take from
     * @param amount the amount to take
     * @return the new balance of player
     */
    double subtractBalance(UUID uuid, double amount);

    /**
     * Reloads the data from storage.
     */
    void reloadEconomyData();

    /**
     * Saves the data to storage.
     */
    void saveEconomyData();

    /**
     * Checks if a user is loaded in memory.
     *
     * @param uuid the uuid of a player to check
     * @return if the user is loaded or needs to be loaded from a database
     */
    boolean isLoaded(UUID uuid);
}
