# 5. Starting of Robolectric testing

So we are finally here, so far we didn't had to touch any kind of context or resources, activities, fragments or anything Android. This is where we have to get back to reality and actually deal with Android.

In this testing instruction set you will learn how to write simple tests using Robolectric.

- We will learn why Robolectric is useful
- Learn how to test Room daos
- Learn how to test Room Migrations
- Learn what a Robolectric Shadow is
- And Learn how to write basic UI tests

## `CodeKataUserDataLocalStorageTest`

Let's start with something easy:
Our System Under Test will be `org.fnives.test.showcase.storage.SharedPreferencesManagerImpl`
But we only test their interface functions.

We don't add anything Robolectric just yet, let's try to do this without it first.

Let's setup or System Under Test as usual:
```kotlin
private lateinit var sut: UserDataLocalStorage

@Before
fun setup() {
    sut = SharedPreferencesManagerImpl.create(mock())
}
```

And if we run our test class we already get an exception:

> sharedPreferences must not be null
> java.lang.NullPointerException: sharedPreferences must not be null
	at org.fnives.test.showcase.storage.SharedPreferencesManagerImpl$Companion.create(SharedPreferencesManagerImpl.kt:65)

So we need to mock the creation of `SharedPreferences`, then the `SharedPreferences` as well.
Since our classes main purpose is to handle `SharedPreferences`, that doesn't really make sense.

Well, I would rather not do that. So then we need to test on a Real Device or Emulator. Well we could, but then we need to integrate a Testing Farm with our CI. It would be good to do that, but sometimes that's just not possible, here is where [Robolectric](http://robolectric.org/) comes in.

>Robolectric is the industry-standard unit testing framework for Android. With Robolectric, your tests run in a simulated Android environment inside a JVM, without the overhead and flakiness of an emulator. Robolectric tests routinely run 10x faster than those on cold-started emulators.

### Setup with Robolectric

We already have the dependencies in the project.
We need to annotate our class with `@RunWith(AndroidJUnit4::class)`
With this Robolectric actually starts our `TestShowcaseApplication` so we need to stop Koin after our tests:
```kotlin
@RunWith(AndroidJUnit4::class)
class CodeKataUserDataLocalStorageTest: KoinTest {

    //...
    @After
    fun tearDown() {
        stopKoin()
    }
```

Okay, now we just need to get a context. With Robolectric we can get our application class the following way:

```kotlin
val application = ApplicationProvider.getApplicationContext<Application>()
sut = SharedPreferencesManagerImpl.create(application)
```

With that, we can start testing:

### 1. `sessionSetWillStayBeKept`

Well, our tests will be pretty  simple since the interface itsell will be pretty simple.
We set a value and we just verify its kept:

```kotlin
val session = Session(accessToken = "a", refreshToken = "b")
sut.session = session

val actual = sut.session

Assert.assertEquals(session, actual)
```

With that our first test is already done,

### 2. `sessionSetToNullWillStayNull`

Here we almost have the same test, we just use null. Personally I also set the value beforehand.
But you should be able to do this easily on your own. For completeness sake:
```kotlin
sut.session = Session(accessToken = "a", refreshToken = "b")

sut.session = null
val actual = sut.session

Assert.assertEquals(null, actual)
```

### 3. Fake

So if you are doing these instructions in order, you may remember that in our core integration tests, namely `org.fnives.test.showcase.core.integration.CodeKataAuthIntegrationTest` we actually had a Fake implementation of this class.
But we never verified that the Fake behaves exactly as will the real thing, so let's do that.
Sadly we can't depend on the `org.fnives.test.showcase.core.integration.fake.CodeKataUserDataLocalStorage` since it's in a test module.
However with usage of testFixtures we are able to share test classes as we had previously shared an Extension.
Take a look at `core/src/testFixtures/java`, in package `org.fnives.test.showcase.core.integration.fake` We have a `FakeUserDataLocalStorage`. We can use that since it's in the testFixture.

> Reminder: Test fixture plugin creates a new testFixture sourceset where main <- testFixture <- test dependency is created.
> Also one can depend on another module's testFixtures via testImplementation testFixtures(project('<moduleName>'))

So what's a better way to verify the `Fake` than testing it with the `Real` implementation's test case?

To do that we will parametrize our test. Note, it will be different than previous, since it's junit4 and Robolectric.

Let's modify our annotation and Test Class constructor:
```kotlin
@RunWith(ParameterizedRobolectricTestRunner::class)
class CodeKataUserDataLocalStorageTest(val userDataLocalStorageFactory: () -> UserDataLocalStorage) : TestKoin {
    //...
}
```

Then we create our parameters:
```kotlin
companion object {

    private fun createFake(): UserDataLocalStorage = FakeUserDataLocalStorage()

    private fun createReal(): UserDataLocalStorage {
        val context = ApplicationProvider.getApplicationContext<Context>()

        return SharedPreferencesManagerImpl.create(context)
    }

    @JvmStatic // notice it needs to be static
    @ParameterizedRobolectricTestRunner.Parameters // notice the annotation
    // notice the return List's type parameter matches the constructor of CodeKataUserDataLocalStorageTest
    fun userDataLocalStorageFactories(): List<() -> UserDataLocalStorage> = listOf(
        ::createFake,
        ::createReal
    )
}
```

Now we just change how we create our SUT:
```kotlin
@Before
fun setup() {
    sut = userDataLocalStorageFactory.invoke()
}
```

Now we validated our fake implementation as well. With this we can be sure our previous integration tests were indeed correct.

## FavouriteContentLocalStorage test

Our System Under Test will be `org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage` or more precisely its implementation: `org.fnives.test.showcase.storage.favourite.FavouriteContentLocalStorageImpl`

What it does is:
- it's an abstraction over the Room DAO
- has 3 methods: observe, add and delete
- it gets the data from Room and updates Room

### Setup

So let's start with the setup.

Our test class is `org.fnives.test.showcase.storage.favourite.CodeKataFavouriteContentLocalStorageInstrumentedTest`

> Question: Why don't we test the DAO and Storage separately using mocking?

>Answer: The same logic applies how we didn't test the RetrofitServices just the RemoteSources. The Service just like the DAO is an implementation detail, our code only accesses them through the RemoteSource / LocalStorage abstraction. With this in mind now we only want to test that we interact with the database properly, we don't really care how many DAOs are used.

We again need Robolectric to create a Room Database.

We need to annotate our class with `@RunWith(AndroidJUnit4::class)`.
With this Robolectric actually starts our `TestShowcaseApplication` so instead of creating our SUT, we just inject it. However to easily inject with Koin, we extend `KoinTest`:
```kotlin
@RunWith(AndroidJUnit4::class)
class CodeKataFavouriteContentLocalStorage: KoinTest
```

- we inject our SUT
- we stop koin in tearDown
- we add a testDispatcher to Room
- we switch to runTest(testDispatcher)

Since Room has their own exercutors, that could make our tests flaky, since it might get out of sync. Luckily we can switch out these executors, so we do that to make sure our tests run just as we would like them to.

```
private val sut by inject<FavouriteContentLocalStorage>()
private lateinit var testDispatcher: TestDispatcher

@Before
fun setUp() {
    testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())
    DatabaseInitialization.dispatcher = testDispatcher
}

@After
fun tearDown() {
    stopKoin()
}

@Test
fun atTheStartOurDatabaseIsEmpty()= runTest(testDispatcher) {
    sut.observeFavourites().first()
}
```

The line `DatabaseInitialization.dispatcher = testDispatcher` may look a bit mysterious, but all we do with it is to overwrite our original DatabaseInitialization in tests, and use the given Dispatcher as an executor for Room setup.

> DatabaseInitialization is overwritten in the Test module, by declaring the same class in the same package with the same methods. This is an easy way to switch out an implementation.
> This might not look the cleanest, so an alternative way is to switch out the koin-module of how to create the database. For this we could use loadKoinModules. In other dependency injection / service locator frameworks this should also be possible.

### 1. `atTheStartOurDatabaseIsEmpty`

Our test is as simple as it gets. We get the observable and it's first element. Then we assert that it is an empty list.

```kotlin
@Test
fun atTheStartOurDatabaseIsEmpty() = runTest(testDispatcher) {
    val actual = sut.observeFavourites().first()

    Assert.assertEquals(emptyList<ContentId>(), actual)
    // note we are using Assert instead of Assertions, that's because Robolectric and AndroidTest support JUnit4 and not JUnit5 we used previously. The @Test @Before etc annotations are also different.
}
```

### 2. `addingContentIdToFavouriteCanBeLaterReadOut`

Time to test some actual logic. Let's see if we add an element to the Database, we indead can query it back.
First we declare what we expect:
```kotlin
val expected = listOf(ContentId("a"))
```

We do the action:
```kotlin
sut.markAsFavourite(ContentId("a"))
val actual = sut.observeFavourites().first()
```

And at the end verify:
```kotlin
Assert.assertEquals(expected, actual)
```

It is as simple as that.

### 3. `contentIdAddedThenRemovedCanNoLongerBeReadOut`

So we can add to the Database, let's see if we can remove from it.
We expect nothing, and we add an element as a setup:
```kotlin
val expected = listOf<ContentId>()
sut.markAsFavourite(ContentId("b"))
```

We do the action:
```kotlin
sut.deleteAsFavourite(ContentId("b"))
val actual = sut.observeFavourites().first()
```

And just verify our expectation:
```kotlin
Assert.assertEquals(expected, actual)
```

So we can delete as well.

### 4. `addingFavouriteUpdatesExistingObservers`
Until now we just verified that afterwards we get the correct data, but what if we already subscribed? Do we still get the correct updates?

So we setup our expectations and our observer:
```kotlin
val expected = listOf(listOf(), listOf(ContentId("observe")))
val actual = async(coroutineContext) { sut.observeFavourites().take(2).toList() }
advanceUntilIdle() // we sync, so we get the first element that is in the database (which is the emptyList).
```

Now we do the action and synchronize again, so our observer is potentially updated:
```kotlin
sut.markAsFavourite(ContentId("a"))
advanceUntilIdle()
```

And let's assert that indeed we only get these two updates and no more things happening. To do this we won't wait for the async, but just get it's Completed value, aka ensure it is finished.

```kotlin
Assert.assertEquals(expected, actual.getCompleted())
```

##### Note: we can use turbine as well to verify our flows, just like we did previously

### 5. `removingFavouriteUpdatesExistingObservers`

Okay, this should be really similar to `addingFavouriteUpdatesExistingObservers` just with a hint of `contentIdAddedThenRemovedCanNoLongerBeReadOut` so try to write it on your own.

However for completness sake:
```kotlin
val expected = listOf(listOf(ContentId("a")), listOf())
sut.markAsFavourite(ContentId("a"))

val actual = async(coroutineContext) {
    sut.observeFavourites().take(2).toList()
}
advanceUntilIdle()

sut.deleteAsFavourite(ContentId("a"))
advanceUntilIdle()

Assert.assertEquals(expected, actual.getCompleted())
```

### 6.`noUnexpectedUpdates`
Until now, just like with Flow tests in core, we assumed the number of updates.
So it's time to verify that we don't get unexpected updates on our flow.

To do this we don't really care about the results, just that the number of updates are correct. So let's observe the database with the Correct Update Count + 1.
```kotlin
val actual = async(coroutineContext) { sut.observeFavourites().take(4).toList() }
advanceUntilIdle() // we expect to get our first result with emptyList()
```

We modify the database:
```kotlin
sut.markAsFavourite(ContentId("a"))
advanceUntilIdle() // we expect to get our second update with added ContentID
sut.deleteAsFavourite(ContentId("a"))
advanceUntilIdle() // we expect to get our third update with emptyList again
```

And now we verify that the observation did not complete, aka no 4th update was received:
```kotlin
Assert.assertFalse(actual.isCompleted)
actual.cancel()
```

With that we know how to verify our Database running on the JVM, without needing an emulator or device.

### Fake

We also have created a `FakeFavouriteContentLocalStorage` previously. We can verify that also using the same parameterization.
However this is an optional exercise.
> Hint: we can use KoinTest.() -> T lambdas as well. And KoinTest.get() function.

If you want to check it out, `FavouriteContentLocalStorageImplInstrumentedTest` does exactly that.

## Login UI Test

We can do much more with Robolectric than just test our Database or SharedPreferences.
We can write UI Tests as well. It is still not as good as Running tests on a Real Device. But depending on your need it might still be helpful.

> Note we get to the section where I am the least comfortable with, I don't think I have written enough UI Tests yet, so from now on take evrything with a big grain of salt. Feel free to modify your approach to your need. You may also correct me via issues on GitHub, would be a great pleasure to learn for me.

We can write UI tests that have mocked out UseCases and Business Logic, but I prefer to do a full screen Integration Tests, cause I think my UI changes enough as it is, wouldn't want to maintain one extra testing layer.
So this will be showcased here. But you should be able to write pure UI tests, if you can follow along this section as well if you choose to do so

### Setup

Our System Under Test will be mainly the `org.fnives.test.showcase.ui.codekata.CodeKataAuthActivityInstrumentedTest`.

First of all we will use [Espresso](https://developer.android.com/training/testing/espresso) to simulate user actions on our UI.
We need quite a bunch of setup, but first let's start with our Robot.

#### Robot Pattern
Robot Pattern presented by Jake Wharton here: https://academy.realm.io/posts/kau-jake-wharton-testing-robots/ and as described Kotlin specific here: https://medium.com/android-bits/espresso-robot-pattern-in-kotlin-fc820ce250f7

There is also a Kotlin specific article [here](https://medium.com/android-bits/espresso-robot-pattern-in-kotlin-fc820ce250f7).

The idea is to separate the logic of finding your views from the logic of the test.
So basically if for example a View Id changes, it doesn't make our behaviour change too, so in this case only our Robot will change, while the Test Class stays the same.

For now I will keep the synthetic sugar to the minimum, and just declare my actions and verifications there. Feel free to have as much customization there as you think is necessary to make your tests clearer.

Let's open our robot: `org.fnives.test.showcase.ui.codekata.CodeKataLoginRobot`

Here is a list of actions we want to do:
- we want to be able to type in the username
- we want to be able to type in the password
- we want to be able the username or password is indeed shows on the UI
- we want to be able to click on signin
- we want to be able verify if we are loading or not
- we want to verify if an error is shown or not
- we want to check if we navigated to Main or not

##### So here is the code for our the UI interactions

```kotlin
fun setUsername(username: String) = apply {
    onView(withId(R.id.user_edit_text))
        .perform(ViewActions.replaceText(username), ViewActions.closeSoftKeyboard())
}

fun setPassword(password: String) = apply {
    onView(withId(R.id.password_edit_text))
        .perform(ViewActions.replaceText(password), ViewActions.closeSoftKeyboard())
}

fun clickOnLogin() = apply {
    onView(withId(R.id.login_cta))
        .perform(ViewActions.click())
}

fun assertPassword(password: String) = apply {
    onView(withId((R.id.password_edit_text)))
        .check(ViewAssertions.matches(ViewMatchers.withText(password)))
}

fun assertUsername(username: String) = apply {
    onView(withId((R.id.user_edit_text)))
        .check(ViewAssertions.matches(ViewMatchers.withText(username)))
}

fun assertLoadingBeforeRequests() = apply {
    onView(withId(R.id.loading_indicator))
        .check(ViewAssertions.matches(isDisplayed()))
}

fun assertNotLoading() = apply {
    onView(withId(R.id.loading_indicator))
        .check(ViewAssertions.matches(not(isDisplayed())))
}
```

Here we took advantage of Espresso. It helps us by being able to perform action such as click, find Views, such as by ID, and assert View States such as withText.
To know what Espresso matchers, assertions are there you just have to use them. It's also easy to extend so if one of your views doesn't have that option, then you can create your own matcher.

##### Next up, we need to verify if we navigated:

```kotlin
fun assertNavigatedToHome() = apply {
    intended(hasComponent(MainActivity::class.java.canonicalName))
}

fun assertNotNavigatedToHome() = apply {
    notIntended(hasComponent(MainActivity::class.java.canonicalName))
}
```

Here we use Espresso's intents, with this we can verify if an Intent was sent out we can also Intercept it to send a result back.

##### Lastly let's verify Errors
For Snackbar we still gonna use Espresso, but we have a helper class for that because we may reuse it in other places.
So let's add that:
```kotlin
class CodeKataLoginRobot(
    private val snackbarVerificationHelper: SnackbarVerificationHelper = SnackbarVerificationHelper()
)
```

Add our functions as well:
```kotlin
fun assertErrorIsShown(@StringRes stringResID: Int) = apply {
    snackbarVerificationHelper.assertIsShownWithText(stringResID)
}

fun assertErrorIsNotShown() = apply {
    snackbarVerificationHelper.assertIsNotShown()
}
```

With that our Robot is done, we can almost start Testing. We still need setup in our Test class.

#### Test class setup

We open the `org.fnives.test.showcase.ui.codekata.CodeKataAuthActivityInstrumentedTest`.

We declare a couple of fields, it will be described later what exactly are those things.
```kotlin
private lateinit var activityScenario: ActivityScenario<AuthActivity>
private lateinit var robot: RobolectricLoginRobot
private lateinit var testDispatcher: TestDispatcher
private lateinit var mockServerScenarioSetup: MockServerScenarioSetup
private lateinit var disposable : Disposable
```

##### Espresso Intents
We add the intent initialization:
```kotlin
@Before
fun setup() {
    Intents.init()
}

@After
fun tearDown() {
    stopKoin()
    Intents.release()
}
```

##### Networking synchronization and mocking
We have a helper method for that, but the basic idea is that, we use our MockWebSetup and synchronize with Espresso using idling resources.
```kotlin
@Before
fun setup() {
    //...
    mockServerScenarioSetup = NetworkTestConfigurationHelper.startWithHTTPSMockWebServer()

    val idlingResources = NetworkTestConfigurationHelper.getOkHttpClients()
        .associateBy(keySelector = { it.toString() })
        .map { (key, client) -> OkHttp3IdlingResource.create(key, client) }
        .map(::IdlingResourceDisposable)
    disposable = CompositeDisposable(idlingResources)
}

@After
fun tearDown() {
    stopKoin()
    Intents.release()
    mockServerScenarioSetup.stop()
    disposable.dispose()
}
```

Idling Resources makes sure that Espresso awaits the Idling Resource before touching the UI components. Disposable is just a way to remove them from Espresso when we no longer need it.

##### Coroutine Test Setup
We use a TestDispatcher and initialize our database with it as well.

```kotlin
@Before
fun setup() {
    //...
    val dispatcher = StandardTestDispatcher(TestCoroutineScheduler())
    Dispatchers.setMain(dispatcher)
    testDispatcher = dispatcher
    DatabaseInitialization.dispatcher = dispatcher
}

@After
fun tearDown() {
    stopKoin()
    Dispatchers.resetMain()
    mockServerScenarioSetup.stop()
    disposable.dispose()
    Intents.release()
}
```

##### Finally we initialize our UI

We create our Robot. And we take advantage or `ActivityScenario` to handle the lifecycle of the Activity.
```kotlin
@Before
fun setup() {
    //...
    robot = RobolectricLoginRobot()
    activityScenario = ActivityScenario.launch(AuthActivity::class.java)
    activityScenario.moveToState(Lifecycle.State.RESUMED)
}

@After
fun tearDown() {
    //...
    activityScenario.safeClose()
}
```

`safeClose` is a workaround which ActivityScenario has, when an activity is finished from code.

Finally we are done with the setup, now we can start to test

### 1. `properLoginResultsInNavigationToHome`

With this setup our test should be pretty simple.

First we mock our request:

```kotlin
mockServerScenarioSetup.setScenario(
    AuthScenario.Success(password = "alma", username = "banan"),
    validateArguments = true)
)
```

Next via the Robot we input the data and click on the sign in:
```kotlin
robot.setPassword("alma")
    .setUsername("banan")
    .assertPassword("alma")
    .assertUsername("banan")
    .clickOnLogin()
    .assertLoadingBeforeRequests()
    .assertErrorIsNotShown()
```

Finally we sync Coroutines and Espresso then verify that we navigated:
```kotlin
testDispatcher.advanceUntilIdleWithIdlingResources()
robot.assertNavigatedToHome()
```

### 2. `emptyPasswordShowsProperErrorMessage`

Next up we verify what happens if the user doesn't set their password. We don't need a request in this case.

```kotlin
robot.setUsername("banan")
    .assertUsername("banan")
    .clickOnLogin()
    .assertLoadingBeforeRequests()
```

Finally we let coroutines go and verify the error is shown and we have not navigated:
```kotlin
testDispatcher.advanceUntilIdleWithIdlingResources()
robot.assertErrorIsShown(R.string.password_is_invalid)
    .assertNotNavigatedToHome()
    .assertNotLoading()
```

### 3. `emptyUserNameShowsProperErrorMessage`

This will be really similar as the previous test, so try to do it on your own. The error is `R.string.username_is_invalid`

Still, here is the complete code:
```kotlin
robot.setPassword("banan")
    .assertPassword("banan")
    .clickOnLogin()
    .assertLoadingBeforeRequests()

testDispatcher.advanceUntilIdleWithIdlingResources()
robot.assertErrorIsShown(R.string.username_is_invalid)
    .assertNotNavigatedToHome()
    .assertNotLoading()
```

### 4. `invalidCredentialsGivenShowsProperErrorMessage`

Now we verify network errors. First let's setup the response:
```kotlin
mockServerScenarioSetup.setScenario(
   AuthScenario.InvalidCredentials(username = "alma", password = "banan"),
    validateArguments = true
)
```

Now let's input the data like the user would:
```kotlin
robot
    .setUsername("alma")
    .setPassword("banan")
    .assertUsername("alma")
    .assertPassword("banan")
    .clickOnLogin()
    .assertLoadingBeforeRequests()
    .assertErrorIsNotShown()
```

Now at the end verify the error is shown properly:
```kotlin
testDispatcher.advanceUntilIdleWithIdlingResources()
robot.assertErrorIsShown(R.string.credentials_invalid)
    .assertNotNavigatedToHome()
    .assertNotLoading()
```

### 5. `networkErrorShowsProperErrorMessage`

Finally we verify the `AuthScenario.GenericError`. This will be really similar as the previous, except the error will be `R.string.something_went_wrong`.
You should try to do this on your own.

Here is the code for verification:
```kotlin
mockServerScenarioSetup.setScenario(
    AuthScenario.GenericError(username = "alma", password = "banan"),
    validateArguments = true
)
robot.setUsername("alma")
    .setPassword("banan")
    .assertUsername("alma")
    .assertPassword("banan")
    .clickOnLogin()
    .assertLoadingBeforeRequests()
    .assertErrorIsNotShown()

testDispatcher.advanceUntilIdleWithIdlingResources()
robot.assertErrorIsShown(R.string.something_went_wrong)
    .assertNotNavigatedToHome()
    .assertNotLoading()
```

## Conclusion

With that we finished our Robolectric tests, setup might be a bit tedious but we can use TestRules to make the setup reusable. In fact we will do that in the next session.

What we have learned:
- How to use Robolectric to verify context dependent classes
- We learned about verifying Fakes
- Robolectric starts an Application instance for each test
- We can write UI tests with Espresso
- We learned about the Robot Pattern and how it clears up our UI tests

