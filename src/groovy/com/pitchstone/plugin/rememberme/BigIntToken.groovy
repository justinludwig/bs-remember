package com.pitchstone.plugin.rememberme

import java.security.SecureRandom

/**
 * Cookie token with a BigInteger representation.
 */
class BigIntToken implements Token {
    /** Bytes used by token. */
    static int length = 8
    /** Shared PRNG. */
    protected static generator
    /** Lock to share PRNG. */
    protected static generatorUseLock = new Object()
    /** Count of PRNG uses. */
    protected static int generatorUseCount
    /** Max PRNG uses before re-seeding. */
    static int generatorUseMax = 1000

    /**
     * Generates a new random token.
     */
    static BigIntToken generate() {
        def random = new byte[length]
        synchronized (generatorUseLock) {
            if (generator == null || ++generatorUseCount > generatorUseMax) {
                generator = new SecureRandom()
                generatorUseCount = 0
            }
            generator.nextBytes(random)
        }
        new BigIntToken(bigInt: new BigInteger(random))
    }

    // Token

    String cookieValue

    String getCookieValue() {
        if (cookieValue == null && bigInt != null)
            cookieValue = bigInt.toByteArray().encodeAsBase64().replaceFirst(/=+$/, '')
        return cookieValue
    }

    void setCookieValue(String x) {
        cookieValue = x
        bigInt = null
    }

    // Object

    boolean equals(Object o) {
        o instanceof BigIntToken && getBigInt() == o.getBigInt()
    }

    int hashCode() {
        getBigInt()?.hashCode() ?: 0
    }

    String toString() {
        getCookieValue() ?: 'null'       
    }

    // impl

    BigInteger bigInt

    BigInteger getBigInt() {
        if (bigInt == null && cookieValue != null)
            bigInt = new BigInteger(cookieValue.decodeBase64())
        return bigInt
    }

    void setBigInt(BigInteger x) {
        bigInt = x
        cookieValue = null
    }
}
