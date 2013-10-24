package com.pitchstone.plugin.rememberme.rotating.inmemory

import com.pitchstone.plugin.rememberme.rotating.RotatingTokenUser
import com.pitchstone.plugin.rememberme.rotating.RotatingTokenUserStore

/**
 * Sample in-memory user-store implementation.
 */
class InMemoryUserStore implements RotatingTokenUserStore {

    /**
     * Maps logged-in tokens to users.
     * Tokens in this map do not need to be refreshed.
     */
    Map<byte[],RotatingTokenUser> userByToken = [:]

    /**
     * Maps logged-in users to tokens.
     * Tokens in this map do not need to be refreshed.
     */
    Map<RotatingTokenUser,byte[]> tokenByUser = [:]

    /**
     * Maps old logged-in tokens to users.
     * Tokens in this map need to be refreshed.
     */
    Map<byte[],RotatingTokenUser> userByPrevious = [:]

    /**
     * Maps logged-in users to old tokens.
     * Tokens in this map need to be refreshed.
     */
    Map<RotatingTokenUser,byte[]> previousByUser = [:]

    // RotatingTokenUserStore

    void addRememberMeUser(RotatingTokenUser user) {
        synchronized(this) {
            userByToken[user.rememberMeToken] = user
            tokenByUser[user] = user.rememberMeToken

            if (user.rememberMePreviousToken) {
                userByPrevious[user.rememberMePreviousToken] = user
                previousByUser[user] = user.rememberMePreviousToken
            }
        }
    }

    void removeRememberMeUserByToken(byte[] token) {
        def user = findRememberMeUserByToken(token)
        if (!user) return

        synchronized(this) {
            userByToken.remove user.rememberMeToken
            tokenByUser.remove user
            userByPrevious.remove user.rememberMePreviousToken
            previousByUser.remove user
        }
    }

    void updateRememberMeUser(RotatingTokenUser user) {
        synchronized(this) {
            def oldToken = tokenByUser[user]
            if (oldToken != user.rememberMeToken) {
                tokenByUser[user] = user.rememberMeToken
                userByToken.remove oldToken
                userByToken[user.rememberMeToken] = user
            }

            def oldPrevToken = previousByUser[user]
            if (oldPrevToken != user.rememberMePreviousToken) {
                if (oldPrevToken) {
                    previousByUser.remove user
                    userByPrevious.remove oldPrevToken
                }
                if (user.rememberMePreviousToken) {
                    previousByUser[user] = user.rememberMePreviousToken
                    userByPrevious[user.rememberMePreviousToken] = user
                }
            }
        }
    }

    void hitRememberMeUserByToken(byte[] token) {
        def user = findRememberMeUserByToken(token)
        if (!user) return

        user.lastHit = new Date()
    }

    RotatingTokenUser findRememberMeUserByToken(byte[] token) {
        synchronized(this) {
            userByToken[token] ?: userByPrevious[token]
        }
    }

    Collection<RotatingTokenUser> listRememberMeUsers() {
        synchronized(this) {
            new ArrayList(tokenByUser.keySet())
        }
    }
}
