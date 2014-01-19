package org.c02e.plugin.rememberme

/**
 * Manages user tokens, and the sessions they represent.
 */
interface UserManager {

    /**
     * Saves the specified user session to the user-manager store.
     * Called by {@link BasicSessionlessRememberMeService#remember}.
     * @param user User to save.
     */
    void saveRememberMeToken(User user)

    /**
     * Ejects the specified user session from the user-manager store.
     * Called by {@link BasicSessionlessRememberMeService#forget}.
     * @param token Token of user to dump.
     */
    void dumpRememberMeToken(Token token)

    /**
     * Returns the specified user session; or null if not found.
     * @param token Token of user to find.
     * @return Found user or null.
     */
    User findUserByRememberMeToken(Token token)

    /**
     * Called by {@link BasicSessionlessRememberMeService} (at most once a request)
     * to parse the request's rememberme cookie into a token.
     * @param cookieValue Value to parse.
     * @param request HttpServletRequest.
     * @return Parsed token.
     */
    Token parseRememberMeToken(String cookieValue, request)

    /**
     * Called by {@link BasicSessionlessRememberMeService} (at most once a request)
     * to validate the request's rememberme cookie.
     * @param token Token to validate.
     * @param request HttpServletRequest.
     * @return Validation result.
     */
    Validation validateRememberMeToken(Token token, request)

    /**
     * Called by {@link BasicSessionlessRememberMeService} (at most once a request)
     * every time a rememberme cookie is validated as valid.
     * @param token Validated token.
     * @param request HttpServletRequest.
     */
    void hitRememberMeToken(Token token, request)
}
