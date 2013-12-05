
import com.pitchstone.plugin.rememberme.BasicSessionlessRememberMeFilter
import org.springframework.web.filter.DelegatingFilterProxy

class BasicSessionlessRememberMeGrailsPlugin {
    // the plugin version
    def version = "0.1-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        'grails-app/views/error.gsp',
        'grails-app/*/test/*.*',
        'src/*/test/*.*',
        'web-app/*/test/*.*',
    ]

    // TODO Fill in these fields
    def title = "Basic Sessionless Remember Me Plugin" // Headline display name of the plugin
    def author = "Justin Ludwig"
    def authorEmail = "justin@pitchstonechnology.com"
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
        name: "PitchStone Technology",
        url: "http://www.pitchstonetechnology.com/",
    ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

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

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
