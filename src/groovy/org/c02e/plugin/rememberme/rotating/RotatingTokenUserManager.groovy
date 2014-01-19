package org.c02e.plugin.rememberme.rotating

import org.c02e.plugin.rememberme.BigIntToken
import org.c02e.plugin.rememberme.Token
import org.c02e.plugin.rememberme.User
import org.c02e.plugin.rememberme.UserManager
import org.c02e.plugin.rememberme.Validation
import java.security.SecureRandom
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean

/**
 * User-manager implementation which rotates user tokens.
 * Users must implement {@link RotatingTokenUser} interface.
 */
class RotatingTokenUserManager implements UserManager, InitializingBean {

    def basicSessionlessRememberMeService
    def basicSessionlessRememberMeUserStore
    def grailsApplication

    volatile boolean running

    /**
     * Interval after which to rotate or prune tokens.
     */
    boolean autoStart

    /**
     * Interval after which to rotate or prune tokens.
     */
    String rotatePeriod

    // InitializingBean

    void afterPropertiesSet() {
        def config = this.config

        autoStart = autoStart ?: config?.autoStart != false
        rotatePeriod = rotatePeriod ?: config?.rotatePeriod ?: '10 minutes'
        
        if (autoStart)
            startRotating()
    }

    // DisposableBean

    void destroy() {
        stopRotating()
    }

    // UserManager

    void saveRememberMeToken(User user) {
        store.addRememberMeUser user
    }

    void dumpRememberMeToken(Token token) {
        store.removeRememberMeUserByToken token
    }

    User findUserByRememberMeToken(Token token) {
        store.findRememberMeUserByToken token
    }

    Token parseRememberMeToken(String cookieValue, request) {
        new BigIntToken(cookieValue: cookieValue)
    }

    Validation validateRememberMeToken(Token token, request) {
        def user = store.findRememberMeUserByToken(token)
        // invalid if pruned
        if (!user) return Validation.INVALID

        // refresh with current token if not already using it
        token == user.rememberMeToken ? Validation.VALID : [
            isValid: { -> true },
            getRefreshWith: { -> user },
        ] as Validation
    }

    void hitRememberMeToken(Token token, request) {
        store.hitRememberMeUserByToken token
    }

    // impl

    /**
     * Initializes a new user with a new token and last-access date,
     * and calculates the until/expiry for the user.
     * @param User to initialize.
     * @param period (optional) rememberMePeriod with which to init the user.
     * If not specified, uses the period already set on the user.
     * @param sessionCookie (optional) rememberMeSessionCookie value
     * with which to init the user.
     * If not specified, uses the value already set on the user.
     * @param now (optional) Current date with which to initialize.
     */
    void initUser(RotatingTokenUser user,
            String period = null, boolean sessionCookie = false,
            Date now = new Date()) {
        user.rememberMeToken = generateToken()

        if (!user.lastHit)
            user.lastHit = now

        if (period)
            user.rememberMePeriod = period
        else
            period = user.rememberMePeriod

        if (sessionCookie)
            user.rememberMeSessionCookie = true

        def expires = calcUntil(period, now)
        user.rememberMeExpires = expires

        if (!user.rememberMeSessionCookie)
            user.rememberMeUntil = expires

        store.updateRememberMeUser user
    }

    /**
     * Generates a new random token.
     */
    Token generateToken() {
        BigIntToken.generate()
    }

    /**
     * Starts rotating thread.
     */
    void startRotating() {
        // already running
        if (running) return
        running = true

        def now = new Date()
        // initial prev: reverse rotatePeriod in the past
        def prev = calcUntil(rotatePeriod, now)
        prev = new Date(now.time * 2 - prev.time)

        Thread.start {
            while (running) {
                service?.log?.info "rotating tokens..."

                try {
                    rotate prev, now
                } catch (Throwable e) {
                    service?.log?.error "error rotating tokens", e
                }

                // sleep until next rotation period
                prev = new Date()
                now = calcUntil(rotatePeriod, prev)
                service?.log?.info "...rotate done until ${now}"

                Thread.sleep now.time - prev.time
            }
        }
    }

    /**
     * Stops rotating thread.
     */
    void stopRotating() {
        running = false
    }

    /**
     * Rotates once, pruning expired user sessions,
     * and extending sessions active since last rotation.
     * @param prev Date of previous rotation.
     * @param now (optional) Date of current rotation.
     */
    void rotate(Date prev, Date now = new Date()) {
        store.listRememberMeUsers().each { user ->
            // rotate if last access more recent than last rotation
            if (user.lastHit > prev) {
                user.rememberMePreviousToken = user.rememberMeToken
                user.rememberMeToken = generateToken()

                def newUntil = calcUntil(user.rememberMePeriod, now)
                user.rememberMeExpires = newUntil
                if (!user.rememberMeSessionCookie)
                    user.rememberMeUntil = newUntil

                store.updateRememberMeUser user

            // prune expired since last hit
            } else if (user.rememberMeExpires < now) {
                store.removeRememberMeUserByToken user.rememberMeToken

            // extend window for potential refresh
            } else if (!user.rememberMeSessionCookie) {
                user.rememberMeUntil = calcUntil(user.rememberMePeriod, now)
                store.updateRememberMeUser user
            }
        }
    }

    protected getConfig() {
        grailsApplication?.config?.grails?.plugin?.basicSessionlessRememberMe?.rotatingToken
    }

    protected getService() {
        basicSessionlessRememberMeService
    }

    protected getStore() {
        basicSessionlessRememberMeUserStore
    }

    protected Date calcUntil(String period, Date now = new Date()) {
        service.calculateUntil period, now
    }
}
