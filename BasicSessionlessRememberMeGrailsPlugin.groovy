
import org.c02e.plugin.rememberme.BasicSessionlessRememberMeFilter
import org.springframework.web.filter.DelegatingFilterProxy

class BasicSessionlessRememberMeGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        'grails-app/views/error.gsp',
        'grails-app/*/test/*.*',
        'src/*/test/*.*',
        'web-app/*/test/*.*',
    ]

    def title = "Basic Sessionless Remember Me Plugin" // Headline display name of the plugin
    def author = "Justin Ludwig"
    def authorEmail = "justin@codetechnology.com"
    def description = '''
Uses a cookie to keep track of logged-in users instead of the servlet session.
'''.trim()

    // URL to the plugin's documentation
    def documentation = ''//"http://grails.org/plugin/basic-sessionless-remember-me"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [
        name: "CODE Technology",
        url: "http://www.codetechnology.com/",
    ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/justinludwig/bs-remember" ]

    def doWithWebDescriptor = { xml ->
        log.info "installing BasicSessionlessRememberMeFilter"

        xml.filter[0] + {
            filter {
                'filter-name' 'basicSessionlessRememberMeFilter'
				'filter-class' DelegatingFilterProxy.name
            }
        }
        xml.'filter-mapping'[0] + {
            'filter-mapping' {
                'filter-name' 'basicSessionlessRememberMeFilter'
                'url-pattern' '/*'
            }
        }
    }

    def doWithSpring = {
        basicSessionlessRememberMeFilter(BasicSessionlessRememberMeFilter) {
            basicSessionlessRememberMeService = ref('basicSessionlessRememberMeService')
        }
    }

}
