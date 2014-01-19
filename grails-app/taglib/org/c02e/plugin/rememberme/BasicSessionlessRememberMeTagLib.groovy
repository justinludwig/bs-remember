package org.c02e.plugin.rememberme

class BasicSessionlessRememberMeTagLib {
    static namespace = 'rememberme'

    def basicSessionlessRememberMeService

    /**
     * Prints an HTML-encoded property value of the currently remembered user.
     * If no user, prints nothing.
     * @param property Name of property to print.
     */
    def user = { attrs, body ->
        if (!attrs.property) return

        def user = service.user
        if (!user) return

        def value = user[attrs.property]
        if (!value) return

        out << value?.toString()?.encodeAsHTML()
    }

    /**
     * Adds the currently remembered user to the current context.
     * Not executed if no user.
     * @param var (optional) Name of context variable (defaults to 'user').
     */
    def withUser = { attrs, body ->
        def user = service.user
        if (!user) return

        def var = attrs.var ?: 'user'
        out << body((var): user)
    }

    /**
     * Executes body if there is a currently remembered user.
     */
    def yes = { attrs, body ->
        if (!service.remembered) return

        out << body()
    }

    /**
     * Executes body if there is no currently remembered user.
     */
    def no = { attrs, body ->
        if (service.remembered) return

        out << body()
    }

    protected getService() {
        basicSessionlessRememberMeService
    }
}
