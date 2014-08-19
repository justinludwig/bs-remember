package org.c02e.plugin.rememberme

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
            cookieValue = encodeWeb64(bigInt.toByteArray())
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
            bigInt = new BigInteger(decodeWeb64(cookieValue))
        return bigInt
    }

    void setBigInt(BigInteger x) {
        bigInt = x
        cookieValue = null
    }

    String encodeWeb64(byte[] b) {
        if (!b) return ''
        b.encodeAsBase64().
            replaceAll(/\+/, '-').
            replaceAll('/', '_').
            replaceFirst(/=+$/, '')
    }

    byte[] decodeWeb64(String s) {
        (s ?: '').replaceAll(/-/, '+').replaceAll(/_/, '/').decodeBase64()
    }

}
