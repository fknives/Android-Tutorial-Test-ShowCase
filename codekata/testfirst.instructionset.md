# Writing Tests first

In this section it will be shown how to approach Testing First before implementation when we find some kind of defect.

## Manual Testing

You might have already noticed but we have an issue in the current implementation.
Install the app and walk through the following steps to see what's the issue is:
- Log in to the app
- Favourite the first picture
- Logout
- Log in to the app with same or different user
=> Notice the first picture is still favourited


So this is the issue we are trying to fix.
Let's say, for simplicity, the expected behaviour is simply that the user should have no favourites when they log in.

Now, your mind might is rushing through solutions:
- Clear the database when logging in
- Clear the database when logging out
- Or something else

But instead let's pause for a second and instead make sure this issue will never happen again.

## Starting our Test

We have the behaviour not working and how it should work so let's create our Test First before trying to fix the issue and
try to verify it is solved.
The issue can be clearly seen on the UI, so we can start with one simple UI test.

Let's create our test clas in app/androidTest/ui and name it `FavouritesAreClearedOnSessionChangeTest`.
You may name it other way if it fits better.
Let's add the Manual test steps into it's documentation and ticket number for later reference.

```kotlin
/**
 * - Log in to the app
 * - Favourite the first picture
 * - Logout
 * - Log in to the app with same or different user
 * => Notice the first picture is still favourited
 * Expected: When just logging in nothing should be favourited.
 */
@RunWith(AndroidJUnit4::class)
class FavouritesAreClearedOnSessionChangeTest {
}
```

### Setup

Now, we need to add our rule and setup. We can reuse our previous Robots to interact and reproduce the bug, so it will be:
```kotlin
private lateinit var activityScenario: ActivityScenario<AuthActivity>

private val mockServerScenarioSetupTestRule = MockServerScenarioSetupResetingTestRule()
private val mockServerScenarioSetup get() = mockServerScenarioSetupTestRule.mockServerScenarioSetup
private val mainDispatcherTestRule = MainDispatcherTestRule()
private lateinit var authRobot: LoginRobot
private lateinit var homeRobot: HomeRobot

@Rule
@JvmField
val ruleOrder: RuleChain = RuleChain.outerRule(DismissSystemDialogsRule())
    .around(mockServerScenarioSetupTestRule)
    .around(mainDispatcherTestRule)
    .around(AsyncDiffUtilInstantTestRule())
    .around(SafeCloseActivityRule { activityScenario })
    .around(ScreenshotRule("test-showcase"))

@Before
fun setup() {
    authRobot = LoginRobot()
    homeRobot = HomeRobot()
}
```

### Writing the test itself.

First we need to login as described in the manual test:
```kotlin
@Test
fun verifyFavouritesAreClear() {
    mockServerScenarioSetup
        .setScenario(AuthScenario.Success(password = "alma", username = "banan"))
    activityScenario = ActivityScenario.launch(AuthActivity::class.java)

    authRobot
        .setPassword("alma")
        .setUsername("banan")
        .clickOnLogin()
    mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
```

With that, if we run this test we can see it Logs In and shows and empty home screen before completing.

So let's load the data on the home screen:
```kotlin
@Test
fun verifyFavouritesAreClear() {
    mockServerScenarioSetup
        .setScenario(AuthScenario.Success(password = "alma", username = "banan"))
        .setScenario(ContentScenario.Success(usingRefreshedToken = false))
    // ...

    homeRobot.assertToolbarIsShown() // we need to await the new activity
        .apply { mainDispatcherTestRule.advanceUntilIdleWithIdlingResources() } // then we need to load the data
        .assertContainsItem(0, FavouriteContent(ContentData.contentSuccess.first(), false))
```

With that, if we run our test we can see it is showing the Content before completing.
Now, we can do the next steps: click to favourite then log out.
```kotlin
    .clickOnContentItem(0, ContentData.contentSuccess.first())
    .apply { mainDispatcherTestRule.advanceUntilIdleWithIdlingResources() }
    .assertContainsItem(0, FavouriteContent(ContentData.contentSuccess.first(), true))
    .clickSignOut(setupIntentResults = false)
    mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
```

Let's log in again:
```kotlin
authRobot.setPassword("alma")
    .setUsername("banan")
    .clickOnLogin()
mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()
```

Lastly check that the first item is no longer favourite:
```kotlin
homeRobot.assertToolbarIsShown()
    .apply { mainDispatcherTestRule.advanceUntilIdleWithIdlingResources() }
    .assertContainsItem(0, FavouriteContent(ContentData.contentSuccess.first(), false))
```

If we run our test it fails as expected and with that we have an automated reproducation of the Manual Test.

### Spliting-up (Optional)

Our test is really big and it might be a bit hard to read. Since this is the only test method inside our Test Class, it might worth split it up to separate chunks.

We can do something like this:
```kotlin
 @Test
fun verifyFavouritesAreClear() {
    mockServerScenarioSetup
        .setScenario(AuthScenario.Success(password = "alma", username = "banan"))
        .setScenario(ContentScenario.Success(usingRefreshedToken = false))
    activityScenario = ActivityScenario.launch(AuthActivity::class.java)

    loginToTheApp(password = "alma", username = "banan")
    setFirstPictureFavourite()
    signOut()
    loginToTheApp(password = "alma", username = "banan")
    assertFirstPictureIsNotFavourite()
}

private fun loginToTheApp(username: String, password: String) {
    authRobot
        .setPassword(password)
        .setUsername(username)
        .clickOnLogin()
    mainDispatcherTestRule.advanceUntilIdleWithIdlingResources()

    // await home screen loading
    homeRobot.assertToolbarIsShown()
        .apply { mainDispatcherTestRule.advanceUntilIdleWithIdlingResources() }
    }

private fun setFirstPictureFavourite() {
    homeRobot.assertContainsItem(0, FavouriteContent(ContentData.contentSuccess.first(), false))
        .clickOnContentItem(0, ContentData.contentSuccess.first())
        .apply { mainDispatcherTestRule.advanceUntilIdleWithIdlingResources() }
        .assertContainsItem(0, FavouriteContent(ContentData.contentSuccess.first(), true))
}

private fun signOut() {
    homeRobot.clickSignOut(setupIntentResults = false)
    mainDispatcherTestRule.advanceUntilIdleWithIdlingResources() // await auth screen loading
}

private fun assertFirstPictureIsNotFavourite() {
    homeRobot.assertContainsItem(0, FavouriteContent(ContentData.contentSuccess.first(), false))
}
```

If we do it like that, it looks much like the Manual Test Repro Steps.

## Next steps
Okay, we have our UI Test, it fails as expected so we can be sure the issue fixed it once our test goes green.
However digging through the code, we can be sure it's not really an UI issue, it is most likely inside `core`.
Based on this we can decide that what we want to do is clearing the Database when the user logs out. This seems to make the most sense, since it frees up the data the earliest.
So, let's create a test for that:

- save a favourite
- logout
- assert the favourites is cleared

Let's create a faster JVM test for this.
> Note we could have started with this test as well, or keep only the UI test, but I am trying to show you a thinking process.
> In real life scenario you may choose whichever makes more sense to you and your project. You may also would need additional tests if only keeping the core one to verify your Storage separately.

Let's create our Test class, core/test/integration `VerifyFavouritesAreClearedAfterLogoutIntegrationTest`
```kotlin
class VerifyFavouritesAreClearedAfterLogoutIntegrationTest: KoinTest {
}
```
### Setup

We set up our Core IntegrationTest with the necessary data.
```kotlin
@RegisterExtension
@JvmField
val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
private val mockServerScenarioSetup get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup
private val logoutUseCase by inject<LogoutUseCase>()
private val getAllContentUseCase by inject<GetAllContentUseCase>()
private val addContentToFavouriteUseCase by inject<AddContentToFavouriteUseCase>()

@BeforeEach
fun setup() {
    val session = Session(accessToken = "login-access", refreshToken = "login-refresh")
    val mockSessionExpirationListener = mock<SessionExpirationListener>()
    val fakeFavouriteContentLocalStorage = FakeFavouriteContentLocalStorage()
    val fakeUserDataLocalStorage = FakeUserDataLocalStorage(session) // we are logged in

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
```

We are only using the Use Cases to verify the behaviour.

### The Test itself

For the test we first setup the request response and load it.
```kotlin
@Test
fun whenLoggingOutFavouritesAreCleared() = runTest {
    mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
    val firstItemBeforeLogout = getAllContentUseCase.get()
        .filterIsInstance<Resource.Success<List<FavouriteContent>>>()
        .first()
        .data
        .first()
}
```

Then we add this First Item as favourite, and best is if we also ensure it is favourited afterwards:
```kotlin
addContentToFavouriteUseCase.invoke(firstItemBeforeLogout.content.id)
    val firstItemAfterFavourited = getAllContentUseCase.get()
        .filterIsInstance<Resource.Success<List<FavouriteContent>>>()
        .first()
        .data
        .first()
    Assertions.assertTrue(firstItemAfterFavourited.isFavourite) {
        "First Item After Adding To Favourite is NOT Favourite!"
    }
```

We are reusing how to get the first element of the data. Since we will only have one test method, best to extract it into a function for readability:
```kotlin
private suspend fun getFirstContentItem() = getAllContentUseCase.get()
    .filterIsInstance<Resource.Success<List<FavouriteContent>>>()
    .first()
    .data
    .first()
```

Now, or test function is a bit cleaner:
```kotlin
mockServerScenarioSetup.setScenario(ContentScenario.Success(usingRefreshedToken = false))
val firstItemBeforeLogout = getFirstContentItem()

addContentToFavouriteUseCase.invoke(firstItemBeforeLogout.content.id)
val firstItemAfterFavourited = getFirstContentItem()
Assertions.assertTrue(firstItemAfterFavourited.isFavourite) {
    "First Item After Adding To Favourite is NOT Favourite!"
}
```

Now, call logout and assert that the First ITem is no longer favourited:
```kotlin
logoutUseCase.invoke()
val firstItemAfterLogout = getFirstContentItem()

Assertions.assertFalse(firstItemAfterLogout.isFavourite) {
    "First Item After logout is still Favourite!"
}
```

With this we have a clear opinionated test on what should be fixed and how. We ensured to clear the database at the logout step.

## Let's make it green

We have our tests, let's make them green as fast as we can.
Let's open up our `LogoutUseCase` and add the following dependency:
```kotlin
class LogoutUseCase(
    private val storage: UserDataLocalStorage,
    private val favouriteContentLocalStorage: FavouriteContentLocalStorage
)
```

With access to the `FavouriteContentLocalStorage` we can clear fast the favourites via the following:
```kotlin
suspend fun invoke() {
    favouriteContentLocalStorage.observeFavourites()
        .first()
        .forEach {
            favouriteContentLocalStorage.deleteAsFavourite(it)
        }
    loadKoinModules(repositoryModule())
    storage.session = null
}
```

We should also add the new dependency wherever we create the `LogoutUseCase`, so the following places:
- createCoreModule/useCaseModule: `factory { LogoutUseCase(get(), get()) }`
- LogoutUseCaseTest: `sut = LogoutUseCase(mockUserDataLocalStorage, mock())` // you could also use the Fake

With this we can run our test and it reports green. We can also run our UI test which should do the same.
Now we can be sure we fixed the issue. It's time for the last step.

## Refactor
Even tho we have fixed the issue, you are likely dissatisfied with the solution. Getting all the favourites from the Storage just to clear them by using the Storage's method is odd.

However, with our test in place we can safely refactor:
Since everything is done via the Storage, it should have a method to take care of the behaviour itself. So let's add a function to the `FavouriteContentLocalStorage` interface:
```kotlin
interface FavouriteContentLocalStorage {

    fun observeFavourites(): Flow<List<ContentId>>

    suspend fun markAsFavourite(contentId: ContentId)

    suspend fun deleteAsFavourite(contentId: ContentId)

    // newly added
    suspend fun clearFavourites()
}
```

And let's use that in our implementation (`LogoutUseCase`):
```kotlin
suspend fun invoke() {
    favouriteContentLocalStorage.clearFavourites()
    loadKoinModules(repositoryModule())
    storage.session = null
}
```

### VerifyFavouritesAreClearedAfterLogoutIntegrationTest

Okay, now to make our integration test work we can update our `FakeFavouriteContentLocalStorage`:
```kotlin
override suspend fun clearFavourites() {
    dataFlow.emit(emptyList())
}
```

For the Real `FavouriteContentLocalStorageImpl` implementation, let's just keep it as empty for now:
```kotlin
override suspend fun clearFavourites() {
}
```

Now if we run our `VerifyFavouritesAreClearedAfterLogoutIntegrationTest` it shows us green, so we are fine on that front.

### FavouritesAreClearedOnSessionChangeTest
However running our `FavouritesAreClearedOnSessionChangeTest` shows that we are not finished, since the Real Storage implementation is not finished.
We know that's all we have to do so let's open up the `FavouriteContentLocalStorageImpl`:
It's best to do the clearing via SQL, so let's just pass the function down to the DAO and implement it there.
```kotlin
override suspend fun clearFavourites() {
    favouriteDao.clear()
}
```
In `FavouriteDao`, let's just add the SQL to clear:
```kotlin
@Query("DELETE FROM FavouriteEntity")
suspend fun clear()
```
With this our refactoring should be complete, running `FavouritesAreClearedOnSessionChangeTest` should be green now as well.

## Conclusions
We have a proper solution for the reported issue, the Automated Test to ensure it works correctly. That enabled us a fast dirty solution which we could then improve uppon via refactoring to make it a proper final solution.

Now, if you choose you can Update your Unit tests, like removing the mock and using Fake instead or testing the methods separately, but that's not that important for this exercise.

Hope you can use this the next time you get a Bug Report.