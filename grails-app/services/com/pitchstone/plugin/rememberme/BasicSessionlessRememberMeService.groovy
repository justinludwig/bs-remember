package com.pitchstone.plugin.rememberme

import javax.servlet.http.Cookie as ServletCookie
import org.springframework.beans.factory.InitializingBean
import org.springframework.web.context.request.RequestAttributes as RA
import org.springframework.web.context.request.RequestContextHolder as RCH

/**
 * Service for managing rememberme cookie.
 */
class BasicSessionlessRememberMeService implements InitializingBean {
    static final String ATTR_ACTION = 'BasicSessionlessRememberMeService.ATTR_ACTION'
    static final String ATTR_TOKEN = 'BasicSessionlessRememberMeService.ATTR_TOKEN'
    static final String ATTR_UNTIL = 'BasicSessionlessRememberMeService.ATTR_UNTIL'
    static final String ACTION_REMEMBER = 'REMEMBER'
    static final String ACTION_FORGET = 'FORGET'

    static transactional = false

    def basicSessionlessRememberMeUserManagerService
    def grailsApplication

    /** Name of cookie in which to store flash. */
    String cookieName
    /** Custom domain for which to save the cookie. */
    String domain
    /** True if HTTPS-only. */
    Boolean secure
    /** True if not accessible to JavaScript. */
    Boolean httpOnly

    // InitializingBean

    void afterPropertiesSet() {
        def config = this.config

        cookieName = cookieName ?: config?.cookieName ?: 'bs_me'
        domain = domain ?: config?.domain ?: ''
        secure = secure != null ? secure : !!config?.secure
        httpOnly = httpOnly != null ? httpOnly : config?.httpOnly != false
    }

    // impl

    /**
     * Remembers the specified user as the current user,
     * refreshing the rememberme cookie.
     * Calls {@link UserManager#saveRememberMeToken}.
     */
    void remember(User user) {
        setAction ACTION_REMEMBER, user.rememberMeToken, user.rememberMeUntil
        users?.saveRememberMeToken user
    }

    /**
     * Forgets the current user, clearing the rememberme cookie.
     * Calls {@link UserManager#dumpRememberMeToken}.
     */
    void forget() {
        def token = this.token
        action = ACTION_FORGET
        users?.dumpRememberMeToken token
    }

    /**
     * True if the current user is remembered.
     */
    boolean isRemembered() {
        token
    }

    /**
     * Returns the current user, or null.
     */
    User getUser() {
        def t = token
        t ? users?.findUserByRememberMeToken(t) : null
    }

    /**
     * Returns the current rememberme cookie token.
     */
    Token getToken() {
        def token = getAttr(ATTR_TOKEN)
        if (token) return token

        def action = getAttr(ATTR_ACTION)
        if (action == ACTION_FORGET) return null

        token = tokenFromCookie
        validateToken(token) ? token : null
    }

    /**
     * Returns the date at which the specified period expires.
     * @param period Period definition, such as '1 month and 5 days'
     * (or '1 month, 5 days' or '1M5d', etc).
     * Amounts must be whole numbers, units must be one of:<ul>
     *  <li>years
     *  <li>months
     *  <li>weeks
     *  <li>days
     *  <li>hours
     *  <li>minutes
     *  <li>seconds
     * </ul>
     * @param now (optional) Start date for period (defaults to current date).
     */
    Date calculateUntil(String period, Date now = new Date()) {
        if (!period) throw new IllegalArgumentException('empty period')
        if (!now) throw new IllegalArgumentException('null now')

        now.toCalendar().with {
            def matches = period =~ /(\d+)[^\dA-Za-z]*([A-Za-z]+)/
            if (!matches)
                throw new IllegalArgumentException("invalid period: $period")

            matches.each { match ->
                def (part, amount, unit) = match[0..2]
                def field

                switch (unit) {
                    case ~/(?i)y.*/: field = YEAR; break
                    case ~/M|(?i)mo.*/: field = MONTH; break
                    case ~/(?i)w.*/: field = WEEK_OF_YEAR; break
                    case ~/(?i)d.*/: field = DATE; break
                    case ~/(?i)h.*/: field = HOUR; break
                    case ~/m|(?i)mi.*/: field = MINUTE; break
                    case ~/(?i)s.*/: field = SECOND; break
                }

                if (!field)
                    throw new IllegalArgumentException(
                        "invalid period: $period ($part)")
                add field, amount as Integer
            }
            return time
        }
    }

    protected getConfig() {
        grailsApplication?.config?.grails?.plugin?.basicSessionlessRememberMe
    }

    protected getUsers() {
        basicSessionlessRememberMeUserManagerService
    }

    /**
     * Returns the token as read directly from the rememberme cookie.
     */
    protected boolean validateToken(Token token) {
        if (!token) return false

        def validation = users?.validateRememberMeToken(token, request)
        // forget invalid token
        if (!validation?.valid) {
            action = ACTION_FORGET
            return false
        }

        def user = validation.refreshWith
        // refresh with new token
        if (user)
            setAction ACTION_REMEMBER, user.rememberMeToken, user.rememberMeUntil
        // just use existing token
        else
            setAction null, token

        users.hitRememberMeToken user?.rememberMeToken ?: token, request
        return true
    }

    /**
     * Returns the token as read directly from the rememberme cookie.
     */
    protected Token getTokenFromCookie() {
        // cookie no longer valid if remember/forget action set
        if (getAttr(ATTR_ACTION)) return null

        def value = cookies.find { it.name == cookieName }?.value
        if (!value) return

        users.parseRememberMeToken value, request
    }

    protected setAction(String action, Token token = null, Date until = null) {
        setAttr ATTR_ACTION, action
        setAttr ATTR_TOKEN, token
        setAttr ATTR_UNTIL, until
    }

    /**
     * Returns the specified request attribute or null.
     */
    protected Object getAttr(key) {
        RCH?.requestAttributes?.getAttribute key, RA.SCOPE_REQUEST
    }

    /**
     * Sets the specified request attribute.
     */
    protected void setAttr(key, value) {
        if (value == null)
            RCH?.requestAttributes?.removeAttribute key, RA.SCOPE_REQUEST
        else
            RCH?.requestAttributes?.setAttribute key, value, RA.SCOPE_REQUEST
    }

    /**
     * Returns the current cookie list.
     */
    protected ServletCookie[] getCookies() {
        request?.cookies
    }

    /**
     * Returns the current request.
     */
    protected getRequest() {
        RCH?.requestAttributes?.request
    }
}
