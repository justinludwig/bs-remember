package com.pitchstone.plugin.rememberme

import org.springframework.beans.factory.InitializingBean

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

    def grailsApplication

    /** Name of cookie in which to store flash. */
    String cookieName = 'bs_remember_me'
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

    def getConfig() {
        grailsApplication?.config?.grails?.plugin?.basicSessionlessRememberMe
    }
}
