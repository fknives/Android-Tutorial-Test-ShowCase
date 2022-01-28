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

So if you are doing these instructions in order, you may remember that in our core integration tests, namely `org.fnives.test.showcase.core.integration.CodeKataAuthIntegrationTest` we actually had Fake implementation of this class.
But we never verified that the Fake behaves exactly as will the real thing, so let's do that.
Sadly we can't depend on the `org.fnives.test.showcase.core.integration.fake.CodeKataUserDataLocalStorage` since it's in a test module.
However with usage of testFixtures we are able to share test classes as we had previously shared an Extension.
Take a look `at code/src/testFixtures/java`, in package `org.fnives.test.showcase.core.integration.fake` We have a `FakeUserDataLocalStorage`. We can use that since it's in the testFixture.

> Reminder: Test fixture plugin creates a new testFixture sourceset where main <- testFixture <- test dependency is created.
> Also one can depend on another modules testFixtures via testImplementation testFixtures(project('<moduleName>'))

So what's better way is there to verify the `Fake` than testing it with the `Real` implementation's test case?

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

Our System Under Test will be `org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage` or more precisely it's implementation: `org.fnives.test.showcase.storage.favourite.FavouriteContentLocalStorageImpl`

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

We need to annotate our class with `@RunWith(AndroidJUnit4::class)`
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

The line `DatabaseInitialization.dispatcher = testDispatcher` may look a bit mysterious, but all we do her is overwrite our original DatabaseInitialization in tests, and use the given Dispatcher as an executor for Room setup.

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

## Conclusion

