package com.pitchstone.plugin.rememberme.rotating

import com.pitchstone.plugin.rememberme.Token
import com.pitchstone.plugin.rememberme.User

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
     * The rememberMeUntil date is when the next refresh of the cookie should expire;
     * the rememberMeExpires date is when the current cookie is no longer acceptable.
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
     * If true, rememberMeUtil should always be false
     * (but either way, rememberMeExpires should always reflect
     * the date after which the current cookie will no longer be valid).
     */
    boolean rememberMeSessionCookie
}
