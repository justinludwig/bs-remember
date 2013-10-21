package com.pitchstone.plugin.rememberme

import java.text.SimpleDateFormat
import javax.servlet.http.Cookie as ServletCookie

class Cookie extends ServletCookie {

    Cookie() {
        super('null', null)
    }

    Cookie(ServletCookie cookie) {
        this()

        if (!cookie) return
        '''
            comment domain maxAge name path secure value version httpOnly
        '''.trim().split(/\s+/).findAll {
            // don't try to access if not available (eg httpOnly)
            // or if null (eg domain)
            cookie.hasProperty(it) && cookie[it]
        }.each {
            this[it] = cookie[it]
        }
    }

    // Cookie

    Object clone() {
        new Cookie(this)
    }

    /** Add setter to name. */
    String name = ''

    // impl

    /** Pre-servlet-3.0 compatability. */
    boolean httpOnly

    /** Sets maxAge via expiration date. */
    void setExpires(Date date, long now = 0) {
        if (date) {
            maxAge = ((date.time - (now ?: System.currentTimeMillis())) / 1000) as Integer
            if (maxAge < 0)
                maxAge = 0
        } else {
            maxAge = -1
        }
    }

    /**
     * Sets this cookie on the specified response.
     */
    void send(response, long now = 0) {
        // write header manually for pre-servlet-3.0 compatability
        def parts = [ "${name}=${value?:''}" ]
        if (maxAge >= 0)
            parts << "expires=${getFormattedMaxAge(now)}"
        if (domain)
            parts << "domain=${domain}"
        if (path)
            parts << "path=${path}"
        if (secure)
            parts << 'secure'
        if (httpOnly)
            parts << 'httponly'
        response.addHeader 'Set-Cookie', parts.join('; ')
    }

    /**
     * Sets this cookie on the specified response in a way that will clear it.
     */
    void cancel(response) {
        maxAge = 0
        if (!path)
            path = '/'
        value = null
        response.addCookie this
    }

    /**
     * Returns a string formatted as this cookie's 'expires' date.
     */
    String getFormattedMaxAge(long now = 0) {
        formatMaxAge maxAge, now
    }

    /**
     * Returns a string formatted as a cookie 'expires' date
     * the specified seconds in the future.
     */
    String formatMaxAge(Integer maxAge, long now = 0) {
        def ms = maxAge ? (now ?: System.currentTimeMillis()) + maxAge * 1000l : 0
        formatDate new Date(ms)
    }

    /**
     * Returns a string formatted as a cookie 'expires' date
     * with the specified date.
     */
    String formatDate(Date date) {
        def pattern = "EEE, dd-MMM-yyyy HH:mm:ss 'GMT'"
        def formatter = new SimpleDateFormat(pattern, Locale.US)
        formatter.timeZone = TimeZone.getTimeZone('GMT')
        formatter.format date
    }

}
