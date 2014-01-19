package org.c02e.plugin.rememberme

/**
 * Represents the result of validating a cookie token.
 */
interface Validation {
    static final Validation VALID = [
        isValid: { -> true },
        getRefreshWith: { -> null },
    ] as Validation
    static final Validation INVALID = [
        isValid: { -> false },
        getRefreshWith: { -> null },
    ] as Validation

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
