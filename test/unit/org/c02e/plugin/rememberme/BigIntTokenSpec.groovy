package org.c02e.plugin.rememberme

import grails.test.mixin.support.GrailsUnitTestMixin
import org.codehaus.groovy.grails.plugins.codecs.Base64Codec
import org.codehaus.groovy.grails.plugins.codecs.URLCodec
import spock.lang.Specification

@Mixin(GrailsUnitTestMixin)
class BigIntTokenSpec extends Specification {

    def setup() {
        mockCodec Base64Codec
        mockCodec URLCodec
    }


    def "cookieValue is null by default"() {
        expect: new BigIntToken().cookieValue == null
    }

    def "cookieValue can be set to string"() {
        when: def token = new BigIntToken(cookieValue: 'foo')
        then: token.cookieValue == 'foo'
    }


    def "bigInt is null by default"() {
        expect: new BigIntToken().bigInt == null
    }

    def "bigInt can be set to number"() {
        when: def token = new BigIntToken(bigInt: 123)
        then: token.bigInt == 123
    }


    def "setting bigInt sets cookieValue"() {
        when: def token = new BigIntToken(bigInt: 123)
        then: token.cookieValue == 'ew'
    }

    def "setting cookieValue sets bigInt"() {
        when: def token = new BigIntToken(cookieValue: 'ew')
        then: token.bigInt == 123
    }

    def "url-encoded cookieValues are decoded before converting to bigInt"() {
        when: def token = new BigIntToken(cookieValue: 'X%2f%2b0')
        then: token.bigInt == 6291380
    }


    def "toString is cookieValue"() {
        expect:
            new BigIntToken(cookieValue: 'foo') as String == 'foo'
            new BigIntToken(bigInt: 123) as String == 'ew'
    }


    def "equal when value the same"() {
        expect: new BigIntToken(bigInt: 123) == new BigIntToken(cookieValue: 'ew')
    }

    def "not equal when value different"() {
        expect: new BigIntToken(bigInt: 123) != new BigIntToken(bigInt: 456)
    }


    def "same hash key for two tokens of same value"() {
        when: def map = [(new BigIntToken(bigInt: 123)): 'foo']
        then: map[new BigIntToken(cookieValue: 'ew')] == 'foo'
    }


    def "generates random tokens"() {
        when:
            def token1 = BigIntToken.generate()
            def token2 = BigIntToken.generate()
        then:
            token1.bigInt != null
            token2.bigInt != null
            token1.bigInt != token2.bigInt
    }


}
