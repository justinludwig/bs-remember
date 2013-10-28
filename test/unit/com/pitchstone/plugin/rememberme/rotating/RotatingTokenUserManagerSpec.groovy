package com.pitchstone.plugin.rememberme.rotating

import com.pitchstone.plugin.rememberme.BigIntToken
import com.pitchstone.plugin.rememberme.Token
import com.pitchstone.plugin.rememberme.BasicSessionlessRememberMeService
import spock.lang.Specification
import static com.pitchstone.plugin.rememberme.Validation.*

class RotatingTokenUserManagerSpec extends Specification {
    static final Date JAN_1_2000 = new Date(946684800000)

    static final Token B123 = new BigIntToken(bigInt: 123)
    static final Token B456 = new BigIntToken(bigInt: 456)

    def config = new ConfigObject()
    def manager = new RotatingTokenUserManager(
        grailsApplication: new ConfigObject(),
        basicSessionlessRememberMeService: new BasicSessionlessRememberMeService(),
    )

    def setup() {
        manager.grailsApplication.config.grails.plugin.
            basicSessionlessRememberMe.rotatingToken = config
    }

    def cleanup() {
        manager.destroy()
    }


    def "initialized with defaults"() {
        when:
            manager.afterPropertiesSet()
        then:
            manager.autoStart == true
            manager.rotatePeriod == '10 minutes'
    }

    def "config overrides defaults"() {
        when:
            config.autoStart = false
            config.rotatePeriod = '1 hour'
            manager.afterPropertiesSet()
        then:
            manager.autoStart == false
            manager.rotatePeriod == '1 hour'
    }


    def "save passes thru"() {
        setup:
            def user = Mock(RotatingTokenUser)
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store
        when:
            manager.saveRememberMeToken(user)
        then:
            1 * store.addRememberMeUser(user)
    }

    def "dump passes thru"() {
        setup:
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store
        when:
            manager.dumpRememberMeToken(B123)
        then:
            1 * store.removeRememberMeUserByToken(B123)
    }

    def "find passes thru"() {
        setup:
            def user = Mock(RotatingTokenUser)
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store

        when:
            manager.findUserByRememberMeToken(B123)
        then:
            1 * store.findRememberMeUserByToken(B123) >> user

        when:
            store.findRememberMeUserByToken(B123) >> user
        then:
            manager.findUserByRememberMeToken(B123) == user
    }

    def "hit passes thru"() {
        setup:
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store
        when:
            manager.hitRememberMeToken(B123, [:])
        then:
            1 * store.hitRememberMeUserByToken(B123)
    }


    def "parse creates BigIntToken"() {
        expect: manager.parseRememberMeToken('ew', [:]) == B123
    }


    def "when user not found, validate returns invalid"() {
        setup:
            def user = Mock(RotatingTokenUser)
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store

        when:
            manager.validateRememberMeToken(B123, [:])
        then:
            1 * store.findRememberMeUserByToken(B123) >> null

        when:
            store.findRememberMeUserByToken(B123) >> null
        then:
            manager.validateRememberMeToken(B123, [:]) == INVALID
    }

    def "when user with current token found, validate returns valid"() {
        setup:
            def user = Mock(RotatingTokenUser)
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store

        when:
            manager.validateRememberMeToken(B123, [:])
        then:
            1 * store.findRememberMeUserByToken(B123) >> user
            1 * user.getRememberMeToken() >> B123

        when:
            store.findRememberMeUserByToken(B123) >> user
            user.getRememberMeToken() >> B123
        then:
            manager.validateRememberMeToken(B123, [:]) == VALID
    }

    def "when user with non-current token found, validate returns valid with refresh"() {
        setup:
            def user = Mock(RotatingTokenUser)
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store
        when:
            store.findRememberMeUserByToken(B123) >> user
            user.getRememberMeToken() >> B456
        then:
            def validation = manager.validateRememberMeToken(B123, [:])
            validation.valid
            validation.refreshWith == user
    }


    def "generate random tokens"() {
        expect: manager.generateToken() != manager.generateToken()
    }


    def "init user with defaults"() {
        setup:
            manager.afterPropertiesSet()
            def user = new StubUser(rememberMePeriod: '1 day')
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store
        when:
            manager.initUser user
        then:
            user.rememberMeToken
            user.lastHit
            user.rememberMePeriod == '1 day'
            user.rememberMeExpires
            user.rememberMeUntil
            !user.rememberMePreviousToken
            !user.rememberMeSessionCookie
            1 * store.updateRememberMeUser(user)
    }

    def "init user with specified period"() {
        setup:
            manager.afterPropertiesSet()
            def user = new StubUser()
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store
        when:
            manager.initUser user, '1 day', true, JAN_1_2000
        then:
            user.rememberMeToken
            user.lastHit == JAN_1_2000
            user.rememberMePeriod == '1 day'
            user.rememberMeExpires == JAN_1_2000 + 1
            !user.rememberMeUntil
            !user.rememberMePreviousToken
            user.rememberMeSessionCookie
            1 * store.updateRememberMeUser(user)
    }


    def "rotate empty does nothing"() {
        setup:
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store
        when:
            manager.rotate JAN_1_2000
        then:
            1 * store.listRememberMeUsers() >> []
    }


    def "when last hit more recent than last check, rotate tokens"() {
        setup:
            def user = new StubUser(
                rememberMeToken: B123,
                lastHit: JAN_1_2000 - 1,
                rememberMePeriod: '1 week',
            )
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store
        when:
            manager.rotate JAN_1_2000 - 2, JAN_1_2000
        then:
            1 * store.listRememberMeUsers() >> [user]
            user.rememberMePreviousToken == B123
            user.rememberMeToken
            user.rememberMeToken != B123
            user.rememberMeExpires == JAN_1_2000 + 7
            user.rememberMeUntil == JAN_1_2000 + 7
            1 * store.updateRememberMeUser(user)
    }

    def "when using session cookie and last hit more recent than last check, until not set"() {
        setup:
            def user = new StubUser(
                rememberMeToken: B123,
                lastHit: JAN_1_2000 - 1,
                rememberMePeriod: '1 week',
                rememberMeSessionCookie: true,
            )
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store
        when:
            manager.rotate JAN_1_2000 - 2, JAN_1_2000
        then:
            1 * store.listRememberMeUsers() >> [user]
            user.rememberMeExpires == JAN_1_2000 + 7
            user.rememberMeUntil == null
            1 * store.updateRememberMeUser(user)
    }

    def "when last hit less recent than last check but not expired, extend until"() {
        setup:
            def user = new StubUser(
                rememberMeToken: B123,
                lastHit: JAN_1_2000 - 3,
                rememberMeExpires: JAN_1_2000 + 1,
                rememberMePeriod: '1 week',
            )
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store
        when:
            manager.rotate JAN_1_2000 - 2, JAN_1_2000
        then:
            1 * store.listRememberMeUsers() >> [user]
            user.rememberMeToken == B123
            !user.rememberMePreviousToken
            user.rememberMeExpires == JAN_1_2000 + 1
            user.rememberMeUntil == JAN_1_2000 + 7
            1 * store.updateRememberMeUser(user)
    }

    def "when using session cookie and last hit less recent than last check but not expired, until not set"() {
        setup:
            def user = new StubUser(
                rememberMeToken: B123,
                lastHit: JAN_1_2000 - 3,
                rememberMeExpires: JAN_1_2000 + 1,
                rememberMePeriod: '1 week',
                rememberMeSessionCookie: true,
            )
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store
        when:
            manager.rotate JAN_1_2000 - 2, JAN_1_2000
        then:
            1 * store.listRememberMeUsers() >> [user]
            user.rememberMeExpires == JAN_1_2000 + 1
            user.rememberMeUntil == null
            0 * store.updateRememberMeUser(user)
    }

    def "when expired, prune instead of rotate"() {
        setup:
            def user = new StubUser(
                rememberMeToken: B123,
                lastHit: JAN_1_2000 - 3,
                rememberMeExpires: JAN_1_2000 - 1,
            )
            def store = Mock(RotatingTokenUserStore)
            manager.basicSessionlessRememberMeUserStore = store
        when:
            manager.rotate JAN_1_2000 - 2, JAN_1_2000
        then:
            1 * store.listRememberMeUsers() >> [user]
            1 * store.removeRememberMeUserByToken(B123)
    }
}

class StubUser implements RotatingTokenUser {
    Token rememberMeToken
    Date rememberMeUntil
    Date lastHit
    Date rememberMeExpires
    String rememberMePeriod
    Token rememberMePreviousToken
    boolean rememberMeSessionCookie
}
