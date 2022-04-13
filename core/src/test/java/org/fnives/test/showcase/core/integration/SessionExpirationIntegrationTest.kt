package org.fnives.test.showcase.core.integration

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.core.content.FetchContentUseCase
import org.fnives.test.showcase.core.content.GetAllContentUseCase
import org.fnives.test.showcase.core.di.createCoreModule
import org.fnives.test.showcase.core.integration.fake.FakeFavouriteContentLocalStorage
import org.fnives.test.showcase.core.integration.fake.FakeUserDataLocalStorage
import org.fnives.test.showcase.core.login.IsUserLoggedInUseCase
import org.fnives.test.showcase.core.login.LoginUseCase
import org.fnives.test.showcase.core.session.SessionExpirationListener
import org.fnives.test.showcase.model.auth.LoginCredentials
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.model.shared.Resource
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.mockserver.scenario.refresh.RefreshTokenScenario
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.testutil.MockServerScenarioSetupExtensions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class SessionExpirationIntegrationTest : KoinTest {

    @RegisterExtension
    @JvmField
    val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
    private val mockServerScenarioSetup get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup
    private lateinit var fakeFavouriteContentLocalStorage: FakeFavouriteContentLocalStorage
    private lateinit var mockSessionExpirationListener: SessionExpirationListener
    private lateinit var fakeUserDataLocalStorage: FakeUserDataLocalStorage
    private val isUserLoggedInUseCase by inject<IsUserLoggedInUseCase>()
    private val getAllContentUseCase by inject<GetAllContentUseCase>()
    private val loginUseCase by inject<LoginUseCase>()
    private val fetchContentUseCase by inject<FetchContentUseCase>()

    @BeforeEach
    fun setup() {
        mockSessionExpirationListener = mock()
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

    @DisplayName("GIVEN logged in user WHEN fetching but expired THEN user is logged out")
    @Test
    fun sessionResultsInErrorAndClearsContent() = runTest {
        mockServerScenarioSetup.setScenario(AuthScenario.Success(username = "a", password = "b"), validateArguments = true)
        loginUseCase.invoke(LoginCredentials(username = "a", password = "b"))
        Assertions.assertTrue(isUserLoggedInUseCase.invoke())
        verifyNoInteractions(mockSessionExpirationListener)

        mockServerScenarioSetup.setScenario(ContentScenario.Unauthorized(usingRefreshedToken = false))
            .setScenario(RefreshTokenScenario.Error)

        getAllContentUseCase.get().take(2).toList() // getting session expiration

        verify(mockSessionExpirationListener, times(1)).onSessionExpired()
        verifyNoMoreInteractions(mockSessionExpirationListener)
        Assertions.assertFalse(isUserLoggedInUseCase.invoke(), "User is expected to be logged out")
    }

    @DisplayName("GIVEN session expiration and failing token-refresh response WHEN requiring data THEN error is returned and data is cleared")
    @Test
    fun sessionExpirationResultsInLogout() = runTest {
        mockServerScenarioSetup.setScenario(AuthScenario.Success(username = "", password = ""), validateArguments = true)
        loginUseCase.invoke(LoginCredentials(username = "", password = ""))

        mockServerScenarioSetup.setScenario(RefreshTokenScenario.Error)
            .setScenario(
                ContentScenario.Success(usingRefreshedToken = true)
                    .then(ContentScenario.Unauthorized(usingRefreshedToken = false))
                    .then(ContentScenario.Success(usingRefreshedToken = true))
            )

        getAllContentUseCase.get().take(2).toList() // cachedData

        fetchContentUseCase.invoke()
        val unauthorizedData = getAllContentUseCase.get().take(2).last()

        Assertions.assertTrue(unauthorizedData is Resource.Error, "Resource is Error")
        Assertions.assertTrue((unauthorizedData as Resource.Error).error is NetworkException, "Resource is Network Error")
    }
}
