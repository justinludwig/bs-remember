package org.c02e.plugin.rememberme.rotating

import org.c02e.plugin.rememberme.Token

/**
 * Saves and retrieves rotating-token users.
 */
interface RotatingTokenUserStore {

    /**
     * Adds the specified user to the store.
     * @param user User to add.
     */
    void addRememberMeUser(RotatingTokenUser user)

    /**
     * Removes the specified user from the store.
     * @param token Token of user to remove.
     */
    void removeRememberMeUserByToken(Token token)

    /**
     * Saves the updates to the specified user.
     * @param user User to save.
     */
    void updateRememberMeUser(RotatingTokenUser user)

    /**
     * Updates the {@linke RotatingTokenUser#lastHit} property of the specified user.
     * @param token Token of user to update.
     */
    void hitRememberMeUserByToken(Token token)

    /**
     * Returns the specified user session; or null if not found.
     * @param token Token of user to find.
     * @return Found user or null.
     */
    RotatingTokenUser findRememberMeUserByToken(Token token)

    /**
     * Lists all the active user sessions.
     * @return List of all active users, or empty (never null).
     */
    Collection<RotatingTokenUser> listRememberMeUsers()
}
