package org.c02e.plugin.rememberme

import org.c02e.plugin.rememberme.BigIntToken
import org.c02e.plugin.rememberme.Token
import grails.test.mixin.support.GrailsUnitTestMixin
import org.codehaus.groovy.grails.plugins.codecs.Base64Codec
import org.codehaus.groovy.grails.plugins.codecs.URLCodec
import org.springframework.web.context.request.RequestAttributes as RA
import org.springframework.web.context.request.RequestContextHolder as RCH
import spock.lang.Specification
import static org.c02e.plugin.rememberme.BasicSessionlessRememberMeService.*

@Mixin(GrailsUnitTestMixin)
class BasicSessionlessRememberMeServiceSpec extends Specification {
    static final long JAN_1_2000 = 946684800000
    static final long SEP_9_2001 = 1000000000000

    static final Token B123 = new BigIntToken(bigInt: 123)
    static final Token B456 = new BigIntToken(bigInt: 456)

    def config = new ConfigObject()
    def service = new BasicSessionlessRememberMeService(
        grailsApplication: new ConfigObject(),
    )

    def setup() {
        mockCodec Base64Codec
        mockCodec URLCodec

        def request = [
            cookies: [] as Cookie[],
            getAttribute: { request[it] },
            removeAttribute: { request.remove(it) },
            setAttribute: { n,v -> request[n] = v },
        ]
        RCH.metaClass.static.getRequestAttributes = { -> [
            getAttribute: { String name, int scope -> request[name] },
            removeAttribute: { String name, int scope -> request.remove name },
            setAttribute: { String name, Object value, int scope -> 
                request[name] = value
            },
            request: request,
        ] }

        service.grailsApplication.config.grails.plugin.basicSessionlessRememberMe =
            config
    }


    def "initialized with defaults"() {
        when:
            service.afterPropertiesSet()
        then:
            service.cookieName == 'bs_me'
            service.domain == ''
            service.secure == false
            service.httpOnly == true
    }

    def "config overrides defaults"() {
        when:
            config.cookieName = 'remember'
            config.domain = 'example.com'
            config.secure = true
            config.httpOnly = false
            service.afterPropertiesSet()
        then:
            service.cookieName == 'remember'
            service.domain == 'example.com'
            service.secure == true
            service.httpOnly == false
    }


    def "when null period, calculate until throws exception"() {
        when: service.calculateUntil(null)
        then: thrown IllegalArgumentException
    }

    def "when empty period, calculate until throws exception"() {
        when: service.calculateUntil('')
        then: thrown IllegalArgumentException
    }

    def "when null now, calculate until throws exception"() {
        when: service.calculateUntil('1 day', null)
        then: thrown IllegalArgumentException
    }

    def "when bogus period, calculate until throws exception"() {
        when: service.calculateUntil('foo')
        then: thrown IllegalArgumentException
    }

    def "when bogus unit, calculate until throws exception"() {
        when: service.calculateUntil('1 foo')
        then: thrown IllegalArgumentException
    }

    def "when bogus part, calculate until throws exception"() {
        when: service.calculateUntil('1 month, 2 foo, 3 minutes')
        then: thrown IllegalArgumentException
    }

    def "calculate until 1 second"() {
        setup:
            def start = new Date(SEP_9_2001)
            def end = SEP_9_2001 + 1000l
        expect:
            service.calculateUntil('1 second', start).time == end
            service.calculateUntil('1s', start).time == end
            service.calculateUntil('1S', start).time == end
    }

    def "calculate until 1 minute"() {
        setup:
            def start = new Date(SEP_9_2001)
            def end = SEP_9_2001 + 60 * 1000l
        expect:
            service.calculateUntil('1 minute', start).time == end
            service.calculateUntil('1m', start).time == end
            service.calculateUntil('1MI', start).time == end
    }

    def "calculate until 1 hour"() {
        setup:
            def start = new Date(SEP_9_2001)
            def end = SEP_9_2001 + 60 * 60 * 1000l
        expect:
            service.calculateUntil('1 hour', start).time == end
            service.calculateUntil('1h', start).time == end
            service.calculateUntil('1H', start).time == end
    }

    def "calculate until 1 day"() {
        setup:
            def start = new Date(SEP_9_2001)
            def end = SEP_9_2001 + 24 * 60 * 60 * 1000l
        expect:
            service.calculateUntil('1 day', start).time == end
            service.calculateUntil('1d', start).time == end
            service.calculateUntil('1D', start).time == end
    }

    def "calculate until 1 week"() {
        setup:
            def start = new Date(SEP_9_2001)
            def end = SEP_9_2001 + 7 * 24 * 60 * 60 * 1000l
        expect:
            service.calculateUntil('1 week', start).time == end
            service.calculateUntil('1w', start).time == end
            service.calculateUntil('1W', start).time == end
    }

    def "calculate until 1 month"() {
        setup:
            def start = new Date(SEP_9_2001)
            def end = SEP_9_2001 + 30 * 24 * 60 * 60 * 1000l
        expect:
            service.calculateUntil('1 month', start).time == end
            service.calculateUntil('1mo', start).time == end
            service.calculateUntil('1M', start).time == end
    }

    def "calculate until 1 year"() {
        setup:
            def start = new Date(SEP_9_2001)
            def end = SEP_9_2001 + 365 * 24 * 60 * 60 * 1000l
        expect:
            service.calculateUntil('1 year', start).time == end
            service.calculateUntil('1y', start).time == end
            service.calculateUntil('1Y', start).time == end
    }

    def "calculate until 3 hours, 45 minutes"() {
        setup:
            def start = new Date(SEP_9_2001)
            def end = SEP_9_2001 + 3 * 60 * 60 * 1000l + 45 * 60 * 1000l
        expect:
            service.calculateUntil('3 hours, 45 minutes', start).time == end
            service.calculateUntil('3hr, 45min', start).time == end
            service.calculateUntil('3H 45MIN', start).time == end
            service.calculateUntil('3h45m', start).time == end
    }

    def "calculate until 1 month, 0 weeks, 2 days"() {
        setup:
            def start = new Date(SEP_9_2001)
            def end = SEP_9_2001 + 32 * 24 * 60 * 60 * 1000l
        expect:
            service.calculateUntil('1 month, 0 weeks, 2 days', start).time == end
            service.calculateUntil('1mo, 0wk, 2d', start).time == end
            service.calculateUntil('1M 0W 2D', start).time == end
            service.calculateUntil('1M0w2d', start).time == end
    }


    def "remember sets token and until"() {
        when:
            def u = null
            service.basicSessionlessRememberMeUserManagerService = [
                saveRememberMeToken: { u = it },
                dumpRememberMeToken: { },
            ]
            service.forget()
            service.remember([
                getRememberMeToken: { -> B123 },
                getRememberMeUntil: { -> new Date(SEP_9_2001) },
            ] as User)
        then:
            service.token == B123
            u.rememberMeToken == B123
            service.getAttr(ATTR_UNTIL) == new Date(SEP_9_2001)
    }


    def "forget clears token and until"() {
        when:
            def t = null
            service.basicSessionlessRememberMeUserManagerService = [
                saveRememberMeToken: { },
                dumpRememberMeToken: { t = it },
            ]
            service.remember([
                getRememberMeToken: { -> B123 },
                getRememberMeUntil: { -> new Date(SEP_9_2001) },
            ] as User)
            service.forget()
        then:
            service.token == null
            t == B123
            service.getAttr(ATTR_UNTIL) == null
    }


    def "when no token, user is null"() {
        expect: service.token == null
    }

    def "when valid token, user found"() {
        when:
            service.setAttr ATTR_TOKEN, B123
            service.basicSessionlessRememberMeUserManagerService = [
                findUserByRememberMeToken: { [
                    getRememberMeToken: { -> it },
                ] as User },
            ]
        then: service.user.rememberMeToken == B123
    }


    def "when no token, not remembered"() {
        expect: !service.remembered
    }

    def "when valid token, remembered"() {
        when: service.setAttr ATTR_TOKEN, B123
        then: service.remembered
    }


    def "when no token to parse, token is null"() {
        expect: service.token == null
    }

    def "when token already parsed as attr, token uses attr value"() {
        when: service.setAttr ATTR_TOKEN, B123
        then: service.token == B123
    }

    def "when token not parsed but in cookie, token checks cookie and validates"() {
        when:
            service.afterPropertiesSet()
            service.request.cookies = [[name: 'bs_me', value: 'ew']] as Cookie[]
            service.basicSessionlessRememberMeUserManagerService = [
                parseRememberMeToken: { v,r -> B123 },
                validateRememberMeToken: { t,r -> [valid: true] },
                hitRememberMeToken: { t,r -> },
            ]
        then: service.token == B123
    }

    def "when invalid token not parsed but in cookie, token checks cookie and is null"() {
        when:
            service.afterPropertiesSet()
            service.request.cookies = [[name: 'bs_me', value: 'ew']] as Cookie[]
            service.basicSessionlessRememberMeUserManagerService = [
                parseRememberMeToken: { v,r -> B123 },
                validateRememberMeToken: { t,r -> null },
                hitRememberMeToken: { t,r -> },
            ]
        then: service.token == null
    }

    def "when invalid token already parsed, token not re-parsed, is null"() {
        when:
            service.afterPropertiesSet()
            service.request.cookies = [[name: 'bs_me', value: 'ew']] as Cookie[]
            service.setAttr ATTR_ACTION, ACTION_FORGET
        then: service.token == null
    }


    def "when token is null, validateToken does nothing"() {
        when:
            service.setAttr ATTR_ACTION, 'no action'
            service.setAttr ATTR_TOKEN, 'no token'
            service.setAttr ATTR_UNTIL, 'no until'
        then:
            !service.validateToken(null)
            service.getAttr(ATTR_ACTION) == 'no action'
            service.getAttr(ATTR_TOKEN) == 'no token'
            service.getAttr(ATTR_UNTIL) == 'no until'
    }

    def "when token invalid, validateToken forgets token"() {
        when:
            service.setAttr ATTR_ACTION, 'no action'
            service.setAttr ATTR_TOKEN, 'no token'
            service.setAttr ATTR_UNTIL, 'no until'

            def hit = null
            service.basicSessionlessRememberMeUserManagerService = [
                validateRememberMeToken: { t,r -> null },
                hitRememberMeToken: { t,r -> hit = t },
            ]
        then:
            !service.validateToken(B123)
            service.getAttr(ATTR_ACTION) == ACTION_FORGET
            service.getAttr(ATTR_TOKEN) == null
            service.getAttr(ATTR_UNTIL) == null
            hit == null
    }

    def "when token valid, validateToken clears action"() {
        when:
            service.setAttr ATTR_ACTION, 'no action'
            service.setAttr ATTR_TOKEN, 'no token'
            service.setAttr ATTR_UNTIL, 'no until'

            def hit = null
            service.basicSessionlessRememberMeUserManagerService = [
                validateRememberMeToken: { t,r -> [valid: true] },
                hitRememberMeToken: { t,r -> hit = t },
            ]
        then:
            service.validateToken(B123)
            service.getAttr(ATTR_ACTION) == null
            service.getAttr(ATTR_TOKEN) == B123
            service.getAttr(ATTR_UNTIL) == null
            hit == B123
    }

    def "when token needs refresh, validateToken remembers new token"() {
        when:
            service.setAttr ATTR_ACTION, 'no action'
            service.setAttr ATTR_TOKEN, 'no token'
            service.setAttr ATTR_UNTIL, 'no until'

            def hit = null
            service.basicSessionlessRememberMeUserManagerService = [
                validateRememberMeToken: { t,r -> [valid: true, refreshWith: [
                    rememberMeToken: B456,
                    rememberMeUntil: null,
                ]] },
                hitRememberMeToken: { t,r -> hit = t },
            ]
        then:
            service.validateToken(B123)
            service.getAttr(ATTR_ACTION) == ACTION_REMEMBER
            service.getAttr(ATTR_TOKEN) == B456
            service.getAttr(ATTR_UNTIL) == null
            hit == B456
    }

    def "when token needs refresh with new expires, validateToken remembers new token and new expires"() {
        when:
            service.setAttr ATTR_ACTION, 'no action'
            service.setAttr ATTR_TOKEN, 'no token'
            service.setAttr ATTR_UNTIL, 'no until'

            def hit = null
            service.basicSessionlessRememberMeUserManagerService = [
                validateRememberMeToken: { t,r -> [valid: true, refreshWith: [
                    rememberMeToken: B456,
                    rememberMeUntil: new Date(SEP_9_2001),
                ]] },
                hitRememberMeToken: { t,r -> hit = t },
            ]
        then:
            service.validateToken(B123)
            service.getAttr(ATTR_ACTION) == ACTION_REMEMBER
            service.getAttr(ATTR_TOKEN) == B456
            service.getAttr(ATTR_UNTIL) == new Date(SEP_9_2001)
            hit == B456
    }


    def "when action is forget, tokenFromCookie is null"() {
        when: service.setAttr ATTR_ACTION, ACTION_FORGET
        then: service.tokenFromCookie == null
    }

    def "when action is remember, tokenFromCookie is null"() {
        when: service.setAttr ATTR_ACTION, ACTION_REMEMBER
        then: service.tokenFromCookie == null
    }

    def "when no cookies, tokenFromCookie is null"() {
        expect: service.tokenFromCookie == null
    }

    def "when other cookies, tokenFromCookie is null"() {
        when: service.request.cookies = [[name: 'foo'], [name: 'bar']] as Cookie[]
        then: service.tokenFromCookie == null
    }

    def "tokenFromCookie extracts rememberme cookie"() {
        when:
            service.afterPropertiesSet()
            service.request.cookies = [
                [name: 'foo'],
                [name: 'bs_me', value: 'ew'],
                [name: 'bar'],
            ] as Cookie[]
            service.basicSessionlessRememberMeUserManagerService = [
                parseRememberMeToken: { v,r -> new BigIntToken(cookieValue: v) },
            ]
        then:
            service.tokenFromCookie == B123
    }

}
