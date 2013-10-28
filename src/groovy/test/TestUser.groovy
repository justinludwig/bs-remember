package test

import com.pitchstone.plugin.rememberme.Token
import com.pitchstone.plugin.rememberme.rotating.RotatingTokenUser

class TestUser implements RotatingTokenUser {

    // User

    Token rememberMeToken
    Date rememberMeUntil

    // RotatingTokenUser

    Date lastHit
    Date rememberMeExpires
    String rememberMePeriod
    Token rememberMePreviousToken
    boolean rememberMeSessionCookie

    // impl

    String username
}
