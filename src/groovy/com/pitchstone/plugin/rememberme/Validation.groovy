package com.pitchstone.plugin.rememberme

/**
 * Represents the result of validating a cookie token.
 */
interface Validation {
    static final Validation VALID = [valid: true] as Validation
    static final Validation INVALID = [valid: false] as Validation

    /**
     * True if token is valid.
     */
    boolean isValid()

    /**
     * User with which to refresh cookie.
     * Null if token invalid, or if token doesn't need refreshing.
     */
    User getRefreshWith()
}
