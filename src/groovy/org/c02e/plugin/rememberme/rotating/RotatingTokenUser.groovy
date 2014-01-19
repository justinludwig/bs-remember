package org.c02e.plugin.rememberme.rotating

import org.c02e.plugin.rememberme.Token
import org.c02e.plugin.rememberme.User

/**
 * User interface required for {@link RotatingTokenUserManager}.
 */
interface RotatingTokenUser extends User {

    // User (add setters)

    /**
     * Current cookie token for logged-in user.
     */
    Token rememberMeToken

    /**
     * Date at which the user's cookie should expire;
     * null if the cookie expires when the user quits his/her browser.
     */
    Date rememberMeUntil

    // RotatingTokenUser

    /**
     * Last-access date for user.
     */
    Date lastHit

    /**
     * Date after which the user's cookie is no longer valid; never null.
     * The rememberMeUntil date is when the browser should stop sending the cookie;
     * the rememberMeExpires date is when the server should stop accepting the cookie.
     * The dates are extended when the tokens are rotated if there was a user hit
     * since the last rotation; if there wasn't a hit, we still want the browser
     * to keep sending the cookie until the next rotation, when we can check again
     * to see if there was a hit and the dates should be extended.
     * The cookie will be pruned only if the cookie expired since the last check
     * AND there wasn't a user hit since the last check --
     * if there was a hit, the dates always will be extended.
     */
    Date rememberMeExpires

    /**
     * Period by which to extend the rememberMeExpires date
     * when the token is rotated; never empty or null.
     * Period should be a string like '1 month and 5 days'
     * (or '1 month, 5 days' or '1M5d', etc).
     */
    String rememberMePeriod

    /**
     * Previous cookie token for logged-in user, or null.
     */
    Token rememberMePreviousToken

    /**
     * True if cookie expires when user quits his/her browser.
     * If true, rememberMeUtil should be null
     * (but either way, rememberMeExpires should always reflect
     * the date after which the current cookie will no longer be valid).
     */
    boolean rememberMeSessionCookie
}
