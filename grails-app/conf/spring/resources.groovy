import com.pitchstone.plugin.rememberme.BasicSessionlessRememberMeService
import com.pitchstone.plugin.rememberme.rotating.RotatingTokenUserManager
import com.pitchstone.plugin.rememberme.rotating.inmemory.InMemoryUserStore

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
