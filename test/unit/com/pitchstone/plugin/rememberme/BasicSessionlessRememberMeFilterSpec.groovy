package com.pitchstone.plugin.rememberme

import grails.test.mixin.support.GrailsUnitTestMixin
import org.codehaus.groovy.grails.plugins.codecs.Base64Codec
import spock.lang.Specification
import static com.pitchstone.plugin.rememberme.BasicSessionlessRememberMeService.*

@Mixin(GrailsUnitTestMixin)
class BasicSessionlessRememberMeFilterSpec extends Specification {
    static final long JAN_1_2000 = 946684800000
    static final long SEP_9_2001 = 1000000000000

    def filter = new BasicSessionlessRememberMeFilter(
        basicSessionlessRememberMeService: [
            cookieName: 'bs_me',
            domain: '',
            secure: false,
            httpOnly: false,
        ],
    )

    def setup() {
        mockCodec Base64Codec
    }


    def "when no action, cookie not sent"() {
        setup:
            def request = stubRequest()
            def response = stubResponse()
        when:
            filter.writeCookie request, response, JAN_1_2000
        then:
            response.headers == [:]
            response.cookies == []
    }

    def "when remember action, cookie sent"() {
        setup:
            def request = stubRequest()
            def response = stubResponse()
        when:
            request[ATTR_ACTION] = ACTION_REMEMBER
            request[ATTR_TOKEN] = [1,2,3] as byte[]
            request[ATTR_UNTIL] = new Date(SEP_9_2001)
            filter.writeCookie request, response, JAN_1_2000
        then:
            response.headers.size() == 1
            response.headers.'Set-Cookie' ==
                'bs_me=AQID; expires=Sun, 09-Sep-2001 01:46:40 GMT; path=/'
            response.cookies == []
    }

    def "when remember action, cookie sent just once"() {
        setup:
            def request = stubRequest()
            def response = stubResponse()
        when:
            request[ATTR_ACTION] = ACTION_REMEMBER
            request[ATTR_TOKEN] = [1,2,3] as byte[]
            request[ATTR_UNTIL] = new Date(SEP_9_2001)
            filter.writeCookie request, response, JAN_1_2000
            filter.writeCookie request, response, JAN_1_2000
        then:
            response.headers.size() == 1
            response.headers.'Set-Cookie' ==
                'bs_me=AQID; expires=Sun, 09-Sep-2001 01:46:40 GMT; path=/'
            response.cookies == []
    }

    def "when remember action with null until, cookie sent without expires"() {
        setup:
            def request = stubRequest()
            def response = stubResponse()
        when:
            request[ATTR_ACTION] = ACTION_REMEMBER
            request[ATTR_TOKEN] = [1,2,3] as byte[]
            filter.writeCookie request, response, JAN_1_2000
        then:
            response.headers.size() == 1
            response.headers.'Set-Cookie' == 'bs_me=AQID; path=/'
            response.cookies == []
    }

    def "when forget action, cookie canceled"() {
        setup:
            def request = stubRequest('/', [[name: 'bs_me', value: 'foo']])
            def response = stubResponse()
        when:
            request[ATTR_ACTION] = ACTION_FORGET
            request[ATTR_TOKEN] = [1,2,3] as byte[]
            request[ATTR_UNTIL] = new Date(SEP_9_2001)
            filter.writeCookie request, response, JAN_1_2000
        then:
            response.headers == [:]
            response.cookies.size() == 1
            response.cookies[0].name == 'bs_me'
            response.cookies[0].value == null
            response.cookies[0].path == '/'
            response.cookies[0].maxAge == 0
    }

    def "when forget action with no current cookie, cookie not canceled"() {
        setup:
            def request = stubRequest()
            def response = stubResponse()
        when:
            request[ATTR_ACTION] = ACTION_FORGET
            request[ATTR_TOKEN] = [1,2,3] as byte[]
            request[ATTR_UNTIL] = new Date(SEP_9_2001)
            filter.writeCookie request, response, JAN_1_2000
        then:
            response.headers == [:]
            response.cookies == []
    }


    protected stubRequest(String requestURI = '/', List cookies = []) {
        def request; request = [
            requestURI: requestURI,
            cookies: cookies,
            getAttribute: { request[it] },
            removeAttribute: { request.remove(it) },
            setAttribute: { n,v -> request[n] = v },
        ]
        return request
    }

    protected stubResponse() {
        def headers = [:]
        def cookies = []
        [
            // HttpServletResponse api
            addCookie: { cookies << it },
            addHeader: { k,v ->
                headers[k] = headers.containsKey(k) ? "${headers[k]}\n${v}" : v
            },
            // impl
            headers: headers,
            cookies: cookies,
        ]
    }

}
