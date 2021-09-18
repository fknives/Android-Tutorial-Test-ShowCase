//package org.fnives.test.showcase.testutils.statesetup
//
//import kotlinx.coroutines.runBlocking
//import org.fnives.test.showcase.core.login.IsUserLoggedInUseCase
//import org.fnives.test.showcase.core.login.LoginUseCase
//import org.fnives.test.showcase.core.login.LogoutUseCase
//import org.fnives.test.showcase.model.auth.LoginCredentials
//import org.fnives.test.showcase.network.mockserver.MockServerScenarioSetup
//import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
//import org.koin.test.KoinTest
//import org.koin.test.get
//
//object SetupLoggedInState : KoinTest {
//
//    private val logoutUseCase get() = get<LogoutUseCase>()
//    private val loginUseCase get() = get<LoginUseCase>()
//    private val isUserLoggedInUseCase get() = get<IsUserLoggedInUseCase>()
//
//    fun setupLogin(mockServerScenarioSetup: MockServerScenarioSetup) {
//        mockServerScenarioSetup.setScenario(AuthScenario.Success("a", "b"))
//        runBlocking {
//            loginUseCase.invoke(LoginCredentials("a", "b"))
//        }
//    }
//
//    fun isLoggedIn() = isUserLoggedInUseCase.invoke()
//
//    fun setupLogout() {
//        runBlocking { logoutUseCase.invoke() }
//    }
//}
