package org.c02e.plugin.rememberme.rotating.inmemory

import org.c02e.plugin.rememberme.BigIntToken
import org.c02e.plugin.rememberme.Token
import org.c02e.plugin.rememberme.rotating.RotatingTokenUser
import spock.lang.Specification

class InMemoryUserStoreSpec extends Specification {
    static final Token B123 = new BigIntToken(bigInt: 123)
    static final Token B456 = new BigIntToken(bigInt: 456)
    static final Token B789 = new BigIntToken(bigInt: 789)

    def store = new InMemoryUserStore()


    def "empty finds nothing"() {
        expect: store.findRememberMeUserByToken(B123) == null
    }

    def "empty lists nothing"() {
        expect: store.listRememberMeUsers() as List == []
    }

    def "empty hits nothing"() {
        when: store.hitRememberMeUserByToken(B123)
        then: notThrown Exception
    }


    def "add allows find by token"() {
        when: store.addRememberMeUser(new StubUser(rememberMeToken: B123))
        then: store.findRememberMeUserByToken(B123)
    }

    def "add allows find by previous token"() {
        when: store.addRememberMeUser(new StubUser(
            rememberMeToken: B123,
            rememberMePreviousToken: B456,
        ))
        then: store.findRememberMeUserByToken(B456)
    }

    def "add includes in list"() {
        when: store.addRememberMeUser(new StubUser(
            rememberMeToken: B123,
            rememberMePreviousToken: B456,
        ))
        then: store.listRememberMeUsers()*.rememberMeToken == [B123]
    }


    def "after removing, token cannot be found or listed"() {
        when:
            store.addRememberMeUser(new StubUser(
                rememberMeToken: B123,
                rememberMePreviousToken: B456,
            ))
            store.removeRememberMeUserByToken(B123)
        then:
            store.findRememberMeUserByToken(B123) == null
            store.findRememberMeUserByToken(B456) == null
            store.listRememberMeUsers() as List == []
    }

    def "after removing by previous, token cannot be found or listed"() {
        when:
            store.addRememberMeUser(new StubUser(
                rememberMeToken: B123,
                rememberMePreviousToken: B456,
            ))
            store.removeRememberMeUserByToken(B456)
        then:
            store.findRememberMeUserByToken(B123) == null
            store.findRememberMeUserByToken(B456) == null
            store.listRememberMeUsers() as List == []
    }


    def "hit by token"() {
        when:
            store.addRememberMeUser(new StubUser(
                rememberMeToken: B123,
                rememberMePreviousToken: B456,
            ))
            store.hitRememberMeUserByToken(B123)
        then:
            store.findRememberMeUserByToken(B123).lastHit
            store.findRememberMeUserByToken(B456).lastHit
    }

    def "hit by previous token"() {
        when:
            store.addRememberMeUser(new StubUser(
                rememberMeToken: B123,
                rememberMePreviousToken: B456,
            ))
            store.hitRememberMeUserByToken(B456)
        then:
            store.findRememberMeUserByToken(B123).lastHit
            store.findRememberMeUserByToken(B456).lastHit
    }


    def "update token with nothing changes nothing"() {
        when:
            def user = new StubUser(
                rememberMeToken: B123,
                rememberMePreviousToken: B456,
            )
            store.addRememberMeUser(user)
            store.updateRememberMeUser(user)
        then:
            store.findRememberMeUserByToken(B123).rememberMeToken == B123
            store.findRememberMeUserByToken(B456).rememberMePreviousToken == B456
            store.listRememberMeUsers()*.rememberMeToken == [B123]
            store.listRememberMeUsers()*.rememberMePreviousToken == [B456]
    }

    def "update token with token changes token"() {
        when:
            def user = new StubUser(rememberMeToken: B123)
            store.addRememberMeUser(user)

            user.rememberMeToken = B456
            user.rememberMePreviousToken = B123
            store.updateRememberMeUser(user)
        then:
            store.findRememberMeUserByToken(B123).rememberMeToken == B456
            store.findRememberMeUserByToken(B456).rememberMePreviousToken == B123
            store.listRememberMeUsers()*.rememberMeToken == [B456]
            store.listRememberMeUsers()*.rememberMePreviousToken == [B123]
    }

    def "update token with previous token changes previous token"() {
        when:
            def user = new StubUser(
                rememberMeToken: B123,
                rememberMePreviousToken: B456,
            )
            store.addRememberMeUser(user)

            user.rememberMeToken = B789
            user.rememberMePreviousToken = B123
            store.updateRememberMeUser(user)
        then:
            store.findRememberMeUserByToken(B123).rememberMeToken == B789
            store.findRememberMeUserByToken(B456) == null
            store.findRememberMeUserByToken(B789).rememberMePreviousToken == B123
            store.listRememberMeUsers()*.rememberMeToken == [B789]
            store.listRememberMeUsers()*.rememberMePreviousToken == [B123]
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
