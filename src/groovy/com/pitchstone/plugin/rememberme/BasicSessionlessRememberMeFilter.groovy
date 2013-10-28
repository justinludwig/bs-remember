package com.pitchstone.plugin.rememberme

import java.io.IOException
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletResponse
import static com.pitchstone.plugin.rememberme.BasicSessionlessRememberMeService.*

/**
 * Filter that writes rememberme cookie as late as possible.
 */
class BasicSessionlessRememberMeFilter implements Filter {

    def basicSessionlessRememberMeService

    // Filter

    void init(FilterConfig config) throws ServletException {
    }

    void destroy() {
    }

    void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain)
    throws IOException, ServletException {
        if (service)
            rs = new ResponseWrapper((HttpServletResponse) rs, {
                writeCookie rq, rs
            })
        chain.doFilter rq, rs
    }

    // impl

    def getService() {
        basicSessionlessRememberMeService
    }

    /**
     * Writes the rememberme cookie to the response.
     */
    void writeCookie(request, response, long now = 0) {
        def action = request.getAttribute(ATTR_ACTION)
        if (!action) return

        // avoid writing more than once per request
        request.removeAttribute ATTR_ACTION

        def token = request.getAttribute(ATTR_TOKEN)
        def until = request.getAttribute(ATTR_UNTIL)

        def cookie = new Cookie(
            name: service.cookieName,
            value: token.toString(),
            path: request.contextPath ?: '/',
            secure: service.secure,
            httpOnly: service.httpOnly,
        )
        // avoid setting domain unless it's actually going to be used --
        // otherwise, response impl may add empty domain to Set-Cookie header
        if (service.domain)
            cookie.domain = service.domain

        cookie.setExpires until, now

        if (action != ACTION_FORGET)
            cookie.send response, now
        // clear if existing cookie
        else if (request.cookies.any { it.name == service.cookieName })
            cookie.cancel response
    }
}
