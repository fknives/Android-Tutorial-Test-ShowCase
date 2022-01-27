package org.fnives.test.showcase.core.integration

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.fnives.test.showcase.core.content.AddContentToFavouriteUseCase
import org.fnives.test.showcase.core.content.FetchContentUseCase
import org.fnives.test.showcase.core.content.GetAllContentUseCase
import org.fnives.test.showcase.core.content.RemoveContentFromFavouritesUseCase
import org.fnives.test.showcase.core.di.createCoreModule
import org.fnives.test.showcase.core.integration.fake.FakeFavouriteContentLocalStorage
import org.fnives.test.showcase.core.integration.fake.FakeUserDataLocalStorage
import org.fnives.test.showcase.core.session.SessionExpirationListener
import org.fnives.test.showcase.core.testutil.AwaitElementEmitCount
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.model.content.FavouriteContent
import org.fnives.test.showcase.model.network.BaseUrl
import org.fnives.test.showcase.model.session.Session
import org.fnives.test.showcase.model.shared.Resource
import org.fnives.test.showcase.network.mockserver.ContentData
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
import org.mockito.kotlin.verifyZeroInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class ContentIntegrationTest : KoinTest {

    @RegisterExtension
    @JvmField
    val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
    private val mockServerScenarioSetup get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup
    private lateinit var fakeFavouriteContentLocalStorage: FakeFavouriteContentLocalStorage
    private lateinit var mockSessionExpirationListener: SessionExpirationListener
    private lateinit var fakeUserDataLocalStorage: FakeUserDataLocalStorage
    private val addContentToFavouriteUseCase by inject<AddContentToFavouriteUseCase>()
    private val fetchContentUseCase by inject<FetchContentUseCase>()
    private val getAllContentUseCase by inject<GetAllContentUseCase>()
    private val removeContentFromFavouritesUseCase by inject<RemoveContentFromFavouritesUseCase>()
    private val session = Session(accessToken = "login-access", refreshToken = "login-refresh")

    @BeforeEach
    fun setup() {
        mockSessionExpirationListener = mock()
        fakeFavouriteContentLocalStorage = FakeFavouriteContentLocalStorage()
        fakeUserDataLocalStorage = FakeUserDataLocalStorage(session)

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

    @DisplayName("GIVEN normal response without favourites WHEN observed THEN data is returned")
    @Test
    fun withoutFavouritesDataIsReturned() = runTest {
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
        val contentData = ContentData.contentSuccess.map { FavouriteContent(it, false) }
        val expected = listOf(
            Resource.Loading(),
            Resource.Success(contentData)
        )

        val actual = async {
            getAllContentUseCase.get()
                .take(2)
                .toList()
        }

        Assertions.assertEquals(expected, actual.await())
        verifyZeroInteractions(mockSessionExpirationListener)
        Assertions.assertSame(session, fakeUserDataLocalStorage.session)
    }

    @DisplayName("GIVEN normal response without favourites matching WHEN observed THEN data is returned")
    @Test
    fun withoutFavouritesMatchingDataIsReturned() = runTest {
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
        addContentToFavouriteUseCase.invoke(ContentId("non-existent-content-id"))
        advanceUntilIdle()
        val contentData = ContentData.contentSuccess.map { FavouriteContent(it, false) }
        val expected = listOf(
            Resource.Loading(),
            Resource.Success(contentData)
        )

        val actual = async {
            getAllContentUseCase.get()
                .take(2)
                .toList()
        }

        Assertions.assertEquals(expected, actual.await())
        verifyZeroInteractions(mockSessionExpirationListener)
        Assertions.assertSame(session, fakeUserDataLocalStorage.session)
    }

    @DisplayName("GIVEN normal response without favourites matching WHEN observed loading and modifying favourites THEN no extra loading is emitted")
    @Test
    fun modifyingFavouritesWhileLoadingDoesntEmitNewValue() = runTest {
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
        addContentToFavouriteUseCase.invoke(ContentId("non-existent-content-id"))
        advanceUntilIdle()
        val contentData = ContentData.contentSuccess.mapIndexed { index, it ->
            FavouriteContent(it, index == 0)
        }
        val expected = listOf(
            Resource.Loading(),
            Resource.Success(contentData)
        )

        val actual = async {
            getAllContentUseCase.get()
                .onEach {
                    if (it is Resource.Loading) {
                        addContentToFavouriteUseCase.invoke(contentData.first().content.id)
                    }
                }
                .take(2)
                .toList()
        }

        Assertions.assertEquals(expected, actual.await())
        verifyZeroInteractions(mockSessionExpirationListener)
        Assertions.assertSame(session, fakeUserDataLocalStorage.session)
    }

    @DisplayName("GIVEN normal response without favourites WHEN adding favourite and removing THEN we get proper updates")
    @Test
    fun addingRemoving() = runTest {
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))

        val startContentData = ContentData.contentSuccess.map {
            FavouriteContent(it, isFavourite = false)
        }
        val addedFavouriteData = startContentData.mapIndexed { index, it ->
            if (index == 0) it.copy(isFavourite = true) else it
        }
        val added2ndFavouriteData = addedFavouriteData.mapIndexed { index, it ->
            if (index == 1) it.copy(isFavourite = true) else it
        }
        val removedFirstFavouriteData = added2ndFavouriteData.mapIndexed { index, it ->
            if (index == 0) it.copy(isFavourite = false) else it
        }
        val expected = listOf(
            Resource.Loading(),
            Resource.Success(startContentData),
            Resource.Success(addedFavouriteData),
            Resource.Success(added2ndFavouriteData),
            Resource.Success(removedFirstFavouriteData)
        )

        val actual = async {
            getAllContentUseCase.get()
                .take(5)
                .toList()
        }
        getAllContentUseCase.get().take(2).toList() // let's await success request

        addContentToFavouriteUseCase.invoke(startContentData.first().content.id)
        advanceUntilIdle()
        addContentToFavouriteUseCase.invoke(startContentData.drop(1).first().content.id)
        advanceUntilIdle()
        removeContentFromFavouritesUseCase.invoke(startContentData.first().content.id)
        advanceUntilIdle()

        val verifyCaching = async {
            getAllContentUseCase.get().take(1).first()
        }

        Assertions.assertIterableEquals(expected, actual.await())
        Assertions.assertEquals(expected.last(), verifyCaching.await())
        verifyZeroInteractions(mockSessionExpirationListener)
        Assertions.assertSame(session, fakeUserDataLocalStorage.session)
    }

    @DisplayName("GIVEN normal response with favourites WHEN getting the data THEN we get proper updates")
    @Test
    fun alreadySavedFavourites() = runTest {
        mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
        addContentToFavouriteUseCase.invoke(ContentData.contentSuccess.first().id)
        addContentToFavouriteUseCase.invoke(ContentData.contentSuccess.takeLast(1).first().id)
        val favouritedIndexes = listOf(0, ContentData.contentSuccess.size - 1)

        val expectedContents = ContentData.contentSuccess.mapIndexed { index, content ->
            FavouriteContent(content, favouritedIndexes.contains(index))
        }

        val expected = listOf(
            Resource.Loading(),
            Resource.Success(expectedContents),
        )

        val actual = async {
            getAllContentUseCase.get()
                .take(2)
                .toList()
        }

        Assertions.assertIterableEquals(expected, actual.await())
        verifyZeroInteractions(mockSessionExpirationListener)
        Assertions.assertSame(session, fakeUserDataLocalStorage.session)
    }

    @DisplayName("GIVEN error response WHEN fetching THEN the data is received")
    @Test
    fun errorFetch() = runTest {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Error(usingRefreshedToken = false)
                .then(ContentScenario.Success(usingRefreshedToken = false))
        )

        val expectedContents = ContentData.contentSuccess.map { content ->
            FavouriteContent(content, false)
        }
        val expected = listOf(
            Resource.Loading(),
            Resource.Error(mock()),
            Resource.Loading(),
            Resource.Success(expectedContents),
        )

        val awaitElementEmitionCount = AwaitElementEmitCount(2)
        val actual = async {
            getAllContentUseCase.get()
                .take(4)
                .let(awaitElementEmitionCount::attach)
                .toList()
        }
        awaitElementEmitionCount.await() // await 2 emissions, aka the request to finish

        fetchContentUseCase.invoke()

        val actualValues = actual.await()
        Assertions.assertEquals(expected[0], actualValues[0])
        Assertions.assertTrue(actualValues[1] is Resource.Error, "Resource is Error")
        Assertions.assertTrue((actualValues[1] as Resource.Error).error is NetworkException, "Resource is Network Error")
        Assertions.assertEquals(expected[2], actualValues[2])
        Assertions.assertEquals(expected[3], actualValues[3])
        verifyZeroInteractions(mockSessionExpirationListener)
        Assertions.assertSame(session, fakeUserDataLocalStorage.session)
    }

    @DisplayName("GIVEN proper response WHEN fetching THEN the data is received")
    @Test
    fun fetchingAgain() = runTest {
        mockServerScenarioSetup.setScenario(
            ContentScenario.Success(usingRefreshedToken = false)
                .then(ContentScenario.SuccessWithMissingFields(usingRefreshedToken = false))
        )

        val expectedContents = ContentData.contentSuccess.map { content ->
            FavouriteContent(content, false)
        }
        val expectedContents2 = ContentData.contentSuccessWithMissingFields.map { content ->
            FavouriteContent(content, false)
        }
        val expected = listOf(
            Resource.Loading(),
            Resource.Success(expectedContents),
            Resource.Loading(),
            Resource.Success(expectedContents2),
        )

        val awaitElementEmitionCount = AwaitElementEmitCount(2)
        val actual = async {
            getAllContentUseCase.get()
                .take(4)
                .let(awaitElementEmitionCount::attach)
                .toList()
        }
        awaitElementEmitionCount.await() // await 2 emissions, aka the request to finish

        fetchContentUseCase.invoke()

        Assertions.assertIterableEquals(expected, actual.await())
    }

    @DisplayName("GIVEN session expiration then proper response WHEN observing THEN the data is received")
    @Test
    fun sessionRefreshing() = runTest {
        mockServerScenarioSetup.setScenario(RefreshTokenScenario.Success)
            .setScenario(
                ContentScenario.Unauthorized(usingRefreshedToken = false)
                    .then(ContentScenario.Success(usingRefreshedToken = true))
            )

        val expectedContents = ContentData.contentSuccess.map { content ->
            FavouriteContent(content, false)
        }
        val expected = listOf(
            Resource.Loading(),
            Resource.Success(expectedContents)
        )

        val actual = async {
            getAllContentUseCase.get()
                .take(2)
                .toList()
        }

        Assertions.assertIterableEquals(expected, actual.await())
        verifyZeroInteractions(mockSessionExpirationListener)
        val expectedSession = Session(accessToken = "refreshed-access", refreshToken = "refreshed-refresh")
        Assertions.assertEquals(expectedSession, fakeUserDataLocalStorage.session)
    }
}
