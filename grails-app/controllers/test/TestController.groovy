package test

class TestController {
    def rememberMe // service
    def rememberMeUsers // user manager

    def index() { }

    def login() {
        def message = ''

        if (request.post) {
            // login user if posted correct password
            if (params.password == 'password') {
                // use 'long term' period if rememberme checkbox checked
                def period = params.rememberme ? '5 minutes' : '1 minute'
                // use session-cookie (browser forgets when closed)
                // if rememberme checkbox not checked
                def sessionCookie = !params.rememberme

                // initialize the user session object
                def user = new TestUser(username: params.username)
                rememberMeUsers.initUser user, period, sessionCookie

                // set the rememberme cookie
                rememberMe.remember user
                return redirect(uri: params.next ?: '/')
            }

            // else re-display login page with error
            message = 'Sorry, wrong password'
        }

        [message: message]
    }

    def logout() {
        rememberMe.forget()
        redirect action: 'index'
    }

    // logged-in users only -- protected by TestFilters forceLogin
    def user() { }
}
