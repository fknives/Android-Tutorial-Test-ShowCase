package org.fnives.test.showcase.core.integration

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.core.content.GetAllContentUseCase
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
import org.fnives.test.showcase.model.shared.Answer
import org.fnives.test.showcase.model.shared.Resource
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.auth.AuthScenario
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.testutil.MockServerScenarioSetupExtensions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyZeroInteractions
import java.util.stream.Stream

@OptIn(ExperimentalCoroutinesApi::class)
class AuthIntegrationTest : KoinTest {

    @RegisterExtension
    @JvmField
    val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
    private val mockServerScenarioSetup get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup
    private lateinit var fakeFavouriteContentLocalStorage: FavouriteContentLocalStorage
    private lateinit var mockSessionExpirationListener: SessionExpirationListener
    private lateinit var fakeUserDataLocalStorage: UserDataLocalStorage
    private val isUserLoggedInUseCase by inject<IsUserLoggedInUseCase>()
    private val loginUseCase by inject<LoginUseCase>()
    private val logoutUseCase by inject<LogoutUseCase>()

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

    @DisplayName("GIVEN no session saved WHEN checking if user is logged in THEN they are not")
    @Test
    fun withoutSessionTheUserIsNotLoggedIn() = runTest {
        fakeUserDataLocalStorage.session = null
        val actual = isUserLoggedInUseCase.invoke()

        Assertions.assertFalse(actual, "User is expected to be not logged in")
        verifyZeroInteractions(mockSessionExpirationListener)
    }

    @DisplayName("GIVEN no session WHEN user is logging in THEN they get session")
    @Test
    fun loginSuccess() = runTest {
        mockServerScenarioSetup.setScenario(AuthScenario.Success(username = "usr", password = "sEc"), validateArguments = true)
        val expectedSession = ContentData.loginSuccessResponse

        val answer = loginUseCase.invoke(LoginCredentials(username = "usr", password = "sEc"))
        val actual = isUserLoggedInUseCase.invoke()

        Assertions.assertEquals(Answer.Success(LoginStatus.SUCCESS), answer)
        Assertions.assertTrue(actual, "User is expected to be logged in")
        Assertions.assertEquals(expectedSession, fakeUserDataLocalStorage.session)
        verifyZeroInteractions(mockSessionExpirationListener)
    }

    @MethodSource("localInputErrorArguments")
    @ParameterizedTest(name = "GIVEN {0} credentials WHEN login called THEN error {1} is shown")
    fun localInputError(credentials: LoginCredentials, loginError: LoginStatus) = runTest {
        val answer = loginUseCase.invoke(credentials)
        val actual = isUserLoggedInUseCase.invoke()

        Assertions.assertEquals(Answer.Success(loginError), answer)
        Assertions.assertFalse(actual, "User is expected to be not logged in")
        Assertions.assertEquals(null, fakeUserDataLocalStorage.session)
        verifyZeroInteractions(mockSessionExpirationListener)
    }

    @MethodSource("networkErrorArguments")
    @ParameterizedTest(name = "GIVEN {0} network response WHEN login called THEN error is shown")
    fun networkInputError(authScenario: AuthScenario) = runTest {
        mockServerScenarioSetup.setScenario(authScenario, validateArguments = true)
        val credentials = LoginCredentials(username = authScenario.username, password = authScenario.password)
        val answer = loginUseCase.invoke(credentials)
        val actual = isUserLoggedInUseCase.invoke()

        Assertions.assertTrue(answer is Answer.Error, "Answer is expected to be an Error")
        Assertions.assertFalse(actual, "User is expected to be not logged in")
        Assertions.assertEquals(null, fakeUserDataLocalStorage.session)
        verifyZeroInteractions(mockSessionExpirationListener)
    }

    @DisplayName("GIVEN no session WHEN user is logging in THEN they get session")
    @Test
    fun loginInvalidCredentials() = runTest {
        mockServerScenarioSetup.setScenario(AuthScenario.InvalidCredentials(username = "usr", password = "sEc"), validateArguments = true)

        val answer = loginUseCase.invoke(LoginCredentials(username = "usr", password = "sEc"))
        val actual = isUserLoggedInUseCase.invoke()

        Assertions.assertEquals(Answer.Success(LoginStatus.INVALID_CREDENTIALS), answer)
        Assertions.assertFalse(actual, "User is expected to be not logged in")
        Assertions.assertEquals(null, fakeUserDataLocalStorage.session)
        verifyZeroInteractions(mockSessionExpirationListener)
    }

    @DisplayName("GIVEN logged in user WHEN user is login out THEN they no longer have a session")
    @Test
    fun logout() = runTest {
        mockServerScenarioSetup.setScenario(AuthScenario.Success(username = "usr", password = "sEc"), validateArguments = true)
        loginUseCase.invoke(LoginCredentials(username = "usr", password = "sEc"))

        logoutUseCase.invoke()
        val actual = isUserLoggedInUseCase.invoke()

        Assertions.assertFalse(actual, "User is expected to be logged out")
        Assertions.assertEquals(null, fakeUserDataLocalStorage.session)
        verifyZeroInteractions(mockSessionExpirationListener)
    }

    @DisplayName("GIVEN logged in user WHEN user is login out THEN content is cleared")
    @Test
    fun logoutReleasesContent() = runTest {
        mockServerScenarioSetup.setScenario(AuthScenario.Success(username = "usr", password = "sEc"), validateArguments = true)
            .setScenario(ContentScenario.Success(usingRefreshedToken = false), validateArguments = true)
        loginUseCase.invoke(LoginCredentials(username = "usr", password = "sEc"))

        val valuesBeforeLogout = get<GetAllContentUseCase>().get().take(2).last()
        logoutUseCase.invoke()
        val valuesAfterLogout = get<GetAllContentUseCase>().get().take(2).last()

        Assertions.assertTrue(valuesBeforeLogout is Resource.Success, "Before we expect a cached Success")
        Assertions.assertTrue(valuesAfterLogout is Resource.Error, "After we expect an error, since our request no longer is authenticated")
    }

    companion object {

        @JvmStatic
        fun localInputErrorArguments() = Stream.of(
            Arguments.of(LoginCredentials("", "password"), LoginStatus.INVALID_USERNAME),
            Arguments.of(LoginCredentials("username", ""), LoginStatus.INVALID_PASSWORD)
        )

        @JvmStatic
        fun networkErrorArguments() = Stream.of(
            Arguments.of(AuthScenario.GenericError(username = "a", password = "b")),
            Arguments.of(AuthScenario.UnexpectedJsonAsSuccessResponse(username = "a", password = "b")),
            Arguments.of(AuthScenario.MalformedJsonAsSuccessResponse(username = "a", password = "b")),
            Arguments.of(AuthScenario.MissingFieldJson(username = "a", password = "b"))
        )
    }
}
