package org.fnives.test.showcase.network.content.koin

import kotlinx.coroutines.runBlocking
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.network.content.ContentRemoteSourceImpl
import org.fnives.test.showcase.network.di.koin.createNetworkModules
import org.fnives.test.showcase.network.mockserver.ContentData
import org.fnives.test.showcase.network.mockserver.scenario.content.ContentScenario
import org.fnives.test.showcase.network.session.NetworkSessionLocalStorage
import org.fnives.test.showcase.network.shared.MockServerScenarioSetupExtensions
import org.fnives.test.showcase.network.shared.exceptions.NetworkException
import org.fnives.test.showcase.network.shared.exceptions.ParsingException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Suppress("TestFunctionName")
class ContentRemoteSourceImplTest : KoinTest {

    private val sut: ContentRemoteSourceImpl by inject()

    @RegisterExtension
    @JvmField
    val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
    private lateinit var mockNetworkSessionLocalStorage: NetworkSessionLocalStorage
    private val mockServerScenarioSetup
        get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup

    @BeforeEach
    fun setUp() {
        mockNetworkSessionLocalStorage = mock()
        startKoin {
            modules(
                createNetworkModules(
                    baseUrl = BaseUrl(mockServerScenarioSetupExtensions.url),
                    enableLogging = true,
                    networkSessionExpirationListenerProvider = { mock() },
                    networkSessionLocalStorageProvider = { mockNetworkSessionLocalStorage }
                ).toList()
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun GIVEN_successful_response_WHEN_getting_content_THEN_its_parsed_and_returned_correctly() = runBlocking {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.Success(false))
        val expected = ContentData.contentSuccess

        val actual = sut.get()

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_successful_response_WHEN_getting_content_THEN_the_request_is_setup_properly() = runBlocking {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.Success(false), false)

        sut.get()
        val request = mockServerScenarioSetup.takeRequest()

        Assertions.assertEquals("GET", request.method)
        Assertions.assertEquals("Android", request.getHeader("Platform"))
        Assertions.assertEquals(ContentData.loginSuccessResponse.accessToken, request.getHeader("Authorization"))
        Assertions.assertEquals("/content", request.path)
        Assertions.assertEquals("", request.body.readUtf8())
    }

    @Test
    fun GIVEN_response_with_missing_Field_WHEN_getting_content_THEN_invalid_is_ignored_others_are_returned() = runBlocking {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.SuccessWithMissingFields(false))

        val expected = ContentData.contentSuccessWithMissingFields

        val actual = sut.get()

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun GIVEN_error_response_WHEN_getting_content_THEN_network_request_is_thrown() {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.Error(false))

        Assertions.assertThrows(NetworkException::class.java) {
            runBlocking { sut.get() }
        }
    }

    @Test
    fun GIVEN_unexpected_json_response_WHEN_getting_content_THEN_parsing_request_is_thrown() {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.UnexpectedJsonAsSuccessResponse(false))

        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.get() }
        }
    }

    @Test
    fun GIVEN_malformed_json_response_WHEN_getting_content_THEN_parsing_request_is_thrown() {
        whenever(mockNetworkSessionLocalStorage.session).doReturn(ContentData.loginSuccessResponse)
        mockServerScenarioSetup.setScenario(ContentScenario.MalformedJsonAsSuccessResponse(false))

        Assertions.assertThrows(ParsingException::class.java) {
            runBlocking { sut.get() }
        }
    }
}
