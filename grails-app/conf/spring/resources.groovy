import org.c02e.plugin.rememberme.BasicSessionlessRememberMeService
import org.c02e.plugin.rememberme.rotating.RotatingTokenUserManager
import org.c02e.plugin.rememberme.rotating.inmemory.InMemoryUserStore

beans = {
    springConfig.addAlias 'basicSessionlessRememberMeService', 'rememberMe'

    userStore(InMemoryUserStore) {
    }

    rememberMeUsers(RotatingTokenUserManager) {
        basicSessionlessRememberMeService = ref('rememberMe')
        basicSessionlessRememberMeUserStore = ref('userStore')
        grailsApplication = ref('grailsApplication')
    }

    rememberMe(BasicSessionlessRememberMeService) {
        basicSessionlessRememberMeUserManagerService = ref('rememberMeUsers')
        grailsApplication = ref('grailsApplication')
    }
}
