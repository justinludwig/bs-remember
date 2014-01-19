package org.c02e.plugin.rememberme

/**
 * Represents a logged-in user.
 */
interface User {

    /**
     * Current cookie token for logged-in user.
     */
    Token getRememberMeToken()

    /**
     * Date at which the user's cookie should expire;
     * null if the cookie expires when the user quits his/her browser.
     */
    Date getRememberMeUntil()
}
