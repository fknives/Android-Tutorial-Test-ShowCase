# 3. Starting of Robolectric testing

So we are finally here, so far we didn't had to touch any kind of context or resources, activities, fragments or anything android. This is where we have to get back to reality and actually deal with Android.

In this testing instruction set you will learn how to write simple tests using Robolectric.

- We will learn why Robolectric is useful
- Learn how to test Room daos
- Learn how to test Room Migrations
- Learn what a Robolectric Shadow is
- And Learn how to write basic UI tests

## FavouriteContentLocalStorage test

Our System Under Test will be `org.fnives.test.showcase.core.storage.content.FavouriteContentLocalStorage` or more precisely it's implementation: `org.fnives.test.showcase.storage.favourite.FavouriteContentLocalStorageImpl`

What it does is:
- it's an abstraction over the Room DAO
- has 3 methods: observe, add and delete
- it gets the data from Room and updates Room

### Setup

So let's start with the setup.

Our test class is `org.fnives.test.showcase.storage.favourite.CodeKataFavouriteContentLocalStorageInstrumentedTest`

Question: Why don't we test the DAO and Storage separately using mocking?
Answer: The same logic applies how we didn't test the RetrofitServices just the RemoteSources. The Service just like the DAO is an implementation detail, our code only accesses them through the RemoteSource / LocalStorage abstraction. With this in mind now we only want to test that we interact with the database properly, we don't really care how many DAOs are used.

We don't add anything Robolectric just yet, let's try to do this without it first.

Let's setup or System Under Test as usual:

```kotlin
private lateinit var sut: FavouriteContentLocalStorage // notice we only care about the interface

@Before
fun setup() {
    val room = Room.inMemoryDatabaseBuilder(mock(), LocalDatabase::class.java) // we are using inmemory, cause we don't really want to create files.
    .allowMainThreadQueries() // we don't really care about threading for now
    .build()

    sut = FavouriteContentLocalStorageImpl(room.favouriteDao)
}

@Test
fun atTheStartOurDatabaseIsEmpty() = runBlocking<Unit> {
    // we just verify our setup is correct
    sut.observeFavourites().first()
}
```

Let's run our test and see:

> Method getWritableDatabase in android.database.sqlite.SQLiteOpenHelper not mocked. See http://g.co/androidstudio/not-mocked for details.
> java.lang.RuntimeException: Method getWritableDatabase in android.database.sqlite.SQLiteOpenHelper not mocked. See http://g.co/androidstudio/not-mocked for details.
> 	at android.database.sqlite.SQLiteOpenHelper.getWritableDatabase(SQLiteOpenHelper.java)


So we need to mock something inside the `SQLiteOpenHelper` which is used inside the Dao and Room in order to test the Database.
Well, I would rather not do that. So then we need to test on a Real Device or Emulator. Well we could, but then we need to integrate a Testing Farm with our CI. It would be good to do that, but sometimes that's just not possible, here is where [Robolectric](http://robolectric.org/) comes in.

>Robolectric is the industry-standard unit testing framework for Android. With Robolectric, your tests run in a simulated Android environment inside a JVM, without the overhead and flakiness of an emulator. Robolectric tests routinely run 10x faster than those on cold-started emulators.

### Setup with Robolectric

We already have the dependencies in the project.
We need to annotate our class with `@RunWith(AndroidJUnit4::class)`
With this Robolectric actually starts our `TestShowcaseApplication` so instead of creating our SUT, we just inject it. However to easily inject with Koin, we extend `KoinTest`:
```kotlin
@RunWith(AndroidJUnit4::class)
class CodeKataFavouriteContentLocalStorage: KoinTest
```

So additional changes will be:
- remove our previous mocking attempt
- we inject our SUT
- we stop koin in tearDown
- we add a testDispatcher to Room
- we switch to runTest(testDispatcher)

Since Room has their own exercutors, that could make our tests flaky, since we might get out of sync. Luckily we can switch out these executors, so we do that to make sure our tests run just as we would like them to.

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

The line `DatabaseInitialization.dispatcher = testDispatcher` may look a bit mysterious, but all we do her is overwrite our iriginal DatabaseInitialization in tests, and use the given Dispatcher as an executor for Room setup. 

Now if we run our test we see we can indeed access the database. We can get down to actual testing.

### 1. `atTheStartOurDatabaseIsEmpty`

Since we used this test for our setup, we just need to finish it. We just verify the returned list is empty, so:
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

## Conclusion

