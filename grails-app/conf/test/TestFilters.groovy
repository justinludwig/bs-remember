package test

class TestFilters {
    def rememberMe

    def filters = {
        // force users to login to access 'user' page
        forceLogin (controller: 'test', action: 'user') {
            before = {
                if (rememberMe.remembered) return

                // after logging in, return to this url
                def url = request.queryString ?
                    "${request.forwardURI}?${request.queryString}" :
                    request.forwardURI

                redirect controller: 'test', action: 'login', params: [next: url]
                return false
            }
        }
    }
}
