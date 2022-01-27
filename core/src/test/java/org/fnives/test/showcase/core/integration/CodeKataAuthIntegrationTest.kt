package org.fnives.test.showcase.core.integration

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.core.di.createCoreModule
import org.fnives.test.showcase.core.integration.fake.FakeFavouriteContentLocalStorage
import org.fnives.test.showcase.core.integration.fake.FakeUserDataLocalStorage
import org.fnives.test.showcase.core.login.IsUserLoggedInUseCase
import org.fnives.test.showcase.core.login.LoginUseCase
import org.fnives.test.showcase.core.login.LogoutUseCase
import org.fnives.test.showcase.core.session.SessionExpirationListener
import org.fnives.test.showcase.core.storage.UserDataLocalStorage
import org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.auth.LoginStatus
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.network.testutil.MockServerScenarioSetupExtensions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
@Disabled("CodeKata")
class CodeKataAuthIntegrationTest : KoinTest {

    @RegisterExtension
    @JvmField
    val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
    private val isUserLoggedInUseCase by inject<IsUserLoggedInUseCase>()
    private val loginUseCase by inject<LoginUseCase>()
    private val logoutUseCase by inject<LogoutUseCase>()
    private lateinit var fakeFavouriteContentLocalStorage: FavouriteContentLocalStorage
    private lateinit var mockSessionExpirationListener: SessionExpirationListener
    private lateinit var fakeUserDataLocalStorage: UserDataLocalStorage

    @BeforeEach
    fun setup() {
        mockSessionExpirationListener = mock() // we are using mock, since it only has 1 function so we just want to verify if it's called
        fakeFavouriteContentLocalStorage = FakeFavouriteContentLocalStorage()
        fakeUserDataLocalStorage = FakeUserDataLocalStorage(null)
        startKoin {
            modules(
                createCoreModule(
                    baseUrl = BaseUrl(mockServerScenarioSetupExtensions.url),
                    enableNetworkLogging = true,
                    favouriteContentLocalStorageProvider = { fakeFavouriteContentLocalStorage },
                    sessionExpirationListenerProvider = { mockSessionExpirationListener },
                    userDataLocalStorageProvider = { fakeUserDataLocalStorage }
                ).toList()
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @DisplayName("GIVEN no session saved WHEN checking if user is logged in THEN they are not")
    @Test
    fun withoutSessionTheUserIsNotLoggedIn() = runTest {
    }

    @DisplayName("GIVEN no session WHEN user is logging in THEN they get session")
    @Test
    fun loginSuccess() = runTest {
    }

    @DisplayName("GIVEN credentials WHEN login called THEN error is shown")
    @Test
    fun localInputError(credentials: LoginCredentials, loginError: LoginStatus) = runTest {
    }

    @DisplayName("GIVEN network response WHEN login called THEN error is shown")
    @Test
    fun networkInputError(authScenario: AuthScenario) = runTest {
    }

    @DisplayName("GIVEN no session WHEN user is logging in THEN they get session")
    @Test
    fun loginInvalidCredentials() = runTest {
    }

    @DisplayName("GIVEN logged in user WHEN user is login out THEN they no longer have a session and content is cleared")
    @Test
    fun logout() = runTest {
    }
    @DisplayName("GIVEN logged in user WHEN user is login out THEN content is cleared")
    @Test
    fun logoutReleasesContent() = runTest {
    }
}
