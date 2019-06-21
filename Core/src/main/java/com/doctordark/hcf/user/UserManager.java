/*
 * Copyright (C) 2016 SystemUpdate (https://systemupdate.io) All Rights Reserved
 */

package com.doctordark.hcf.user;

import java.util.Map;
import java.util.UUID;

public interface UserManager{

    /**
     * Gets a map of {@link FactionUser} this manager holds.
     *
     * @return map of user UUID strings to their corresponding {@link FactionUser}.
     */
    Map<UUID, FactionUser> getUsers();

    /**
     * Gets a {@link FactionUser} by their {@link UUID} asynchronously.
     *
     * @param uuid the {@link UUID} to get from
     * @return the {@link FactionUser} with the {@link UUID}
     */
    FactionUser getUserAsync(UUID uuid);

    /**
     * Gets a {@link FactionUser} by their {@link UUID}.
     *
     * @param uuid the {@link UUID} to get from
     * @return the {@link FactionUser} with the {@link UUID}
     */
    FactionUser getUser(UUID uuid);

    /**
     * Loads the user data from storage.v
     */
    void reloadUserData();

    /**
     * Saves the user data to storage.
     */
    void saveUserData();


    boolean userExists(UUID uuid);
}
