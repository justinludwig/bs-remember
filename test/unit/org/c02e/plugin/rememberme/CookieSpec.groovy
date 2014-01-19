package org.c02e.plugin.rememberme

import javax.servlet.http.Cookie as ServletCookie
import spock.lang.Specification

class CookieSpec extends Specification {
    static final long JAN_1_2000 = 946684800000

    def cookie = new Cookie(
        name: 'bs_me',
        maxAge: 1800, // 30 minutes
    )

    def "empty cookie can be created"() {
        when: def cookie = new Cookie()
        then:
            cookie.name == ''
            cookie.value == null
            cookie.path == null
            cookie.domain == null
            cookie.maxAge == -1
            !cookie.secure
            !cookie.httpOnly
    }

    def "cookie can be created from another cookie"() {
        when:
            def original = new ServletCookie('name', 'value')
            original.path = 'path'
            original.domain = 'domain'
            original.maxAge = 1
            original.secure = true
            def cookie = new Cookie(original)
        then:
            cookie.name == 'name'
            cookie.value == 'value'
            cookie.path == 'path'
            cookie.domain == 'domain'
            cookie.maxAge == 1
            cookie.secure
            !cookie.httpOnly
    }


    def "format maxAge of 0 is Jan 1 1970"() {
        expect: cookie.formatMaxAge(0) == 'Thu, 01-Jan-1970 00:00:00 GMT'
    }

    def "format maxAge of 300 is 5 minutes from now"() {
        expect: cookie.formatMaxAge(300, JAN_1_2000) == 'Sat, 01-Jan-2000 00:05:00 GMT'
    }


    def "cancel cookie"() {
        setup:
            def added = false
            def response = [ addCookie: { added = true } ]
        when: cookie.cancel response
        then:
            added
            cookie.maxAge == 0
            cookie.path == '/'
    }

    
    def "send basic cookie"() {
        setup:
            def headers = [:]
            def response = [ addHeader: { k,v -> headers[k] = v } ]
        when:
            cookie.value = '123'
            cookie.send response, JAN_1_2000
        then:
            headers.size() == 1
            headers.'Set-Cookie' ==
                'bs_me=123; expires=Sat, 01-Jan-2000 00:30:00 GMT'
    }
    
    def "send custom cookie"() {
        setup:
            def headers = [:]
            def response = [ addHeader: { k,v -> headers[k] = v } ]
        when:
            cookie.name = 'm&m'
            cookie.value = '(1.23%)'
            cookie.path = '/app'
            cookie.domain = 'example.com'
            cookie.maxAge = 60
            cookie.secure = true
            cookie.httpOnly = true
            cookie.send response, JAN_1_2000
        then:
            headers.size() == 1
            headers.'Set-Cookie' ==
                'm&m=(1.23%); expires=Sat, 01-Jan-2000 00:01:00 GMT; domain=example.com; path=/app; secure; httponly'
    }
}
