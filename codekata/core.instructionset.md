# 1. Starting of testing

In this testing instruction set you will learn how to write simple tests using mockito.

Every test will be around one class and all of its dependencies will be mocked out.
Also suspend functions will be tested so you will see how to do that as well.

I would suggest to open this document in your browser, while working in Android Studio.

## Our First Class Test with basic mocking

- First let's check out the class we will test:

    ```kotlin
    org.fnives.test.showcase.core.session.SessionExpirationAdapter
    ```

    As you can see it's a simple adapter between an interface and its received parameter.

- Now navigate to the test class:

    ```kotlin
    org.fnives.test.showcase.core.session.CodeKataFirstSessionExpirationAdapterTest
    ```

### 1. Setup

As you can see the test is empty, so let's declare our System Under Testing (`sut`) and our mocked dependency:

```kotlin
private lateinit var sut: SessionExpirationAdapter // System Under Testing
private lateinit var mockSessionExpirationListener: SessionExpirationListener
```

Now we need to initialize it. Create a method named `setUp` and annotate it with `@BeforeEach`
and initialize the `sut` variable. We will see that the adapter expects a constructor argument.

```kotlin
@BeforeEach // this means this method will be invoked before each test in this class
fun setUp() {
    mockSessionExpirationListener = mock() // this creates a mock instance of the interface
    sut = SessionExpirationAdapter(mockSessionExpirationListener)
}
```

Great, now what is that mock? Simply put, it's an empty implementation of the interface. We can manipulate
that mock object to return what we want and verify its method calls.

### 2. First simple test

So now you need to write your first test. When testing, first you should start with the simplest case, so let's just do that.

When the class is created, the delegate should not yet be touched, so we start there:

```kotlin
@DisplayName("WHEN nothing is changed THEN delegate is not touched") // this will show up when running our tests and is a great way to document what we are testing
@Test // this defines that this method is a test, needs to be org.junit.jupiter.api.Test
fun verifyNoInteractionsIfNoInvocations() {
    verifyZeroInteractions(mockSessionExpirationListener) // we verify that our mock object's functions / properties have not been touched
}
```

Now let's run out Test, to do this:
 - Remove the `@Disabled` annotation if any
 - on project overview right click on FirstSessionExpirationAdapterTest
 - click run
 - => At this point we should see Tests passed: 1 of 1 test.

### 3. Test verifying actual method call

Now let's add an actual method test, we will call the `onSessionExpired` and verify that the delegate is called exactly once:

```kotlin
@DisplayName("WHEN onSessionExpired is called THEN delegated is also called")
@Test
fun verifyOnSessionExpirationIsDelegated() {
    sut.onSessionExpired() // the action we do on our sut

    // verifications
    verify(mockSessionExpirationListener, times(1)).onSessionExpired() // onSessionExpired was called exactly once
    verifyNoMoreInteractions(mockSessionExpirationListener) // there were no more additional touches to this mock object
}
```

Now let's run our tests with coverage, to do this:
 - right click on the file
 - click "Run with coverage".
 - navigate in the result to it's package
 - => We can see the SessionExpirationAdapter is fully covered.

If we did everything right, our test should be identical to SessionExpirationAdapterTest.

## Second Class test with suspend functions and mocking

Our System Under Test will be `org.fnives.test.showcase.core.login.LoginUseCase`.

What it does is:
- verifies parameters,
- if they are invalid then it returns an Error Answer with the error
- if valid then it calls the remote source
  - if that's successful it saves the received data and returns Success Answer
  - if the request fails Error Answer is returned

Now this is a bit more complicated, let's open our test file:

```kotlin
org.fnives.test.showcase.core.login.CodeKataSecondLoginUseCaseTest
```

### 0. Setup

- declare the `sut` variable and its dependencies, you should be familiar how to do this by now.

### 1. `emptyUserNameReturnsLoginStatusError`

Now let's write our first test: `emptyUserNameReturnsLoginStatusError`

First we declare what kind of result we expect:

```kotlin
val expected = Answer.Success(LoginStatus.INVALID_USERNAME)
```

Next we do the actual invocation:

```kotlin
val actual = sut.invoke(LoginCredentials("", "a"))
```

Lastly we add verification:

```kotlin
Assertions.assertEquals(expected, actual) // assert the result is what we expected
verifyZeroInteractions(mockLoginRemoteSource) // assert no request was called
verifyZeroInteractions(mockUserDataLocalStorage) // assert we didn't modify our storage
```

But something is wrong, the invoke method cannot be executed since it's a suspending function.

To test coroutines we will use `runTest`, this creates a test coroutine scope for us to test suspend functions, together it will look like:

```kotlin
@DisplayName("GIVEN empty username WHEN trying to login THEN invalid username is returned")
@Test
fun emptyUserNameReturnsLoginStatusError() = runTest {
    val expected = Answer.Success(LoginStatus.INVALID_USERNAME)

    val actual = sut.invoke(LoginCredentials("", "a"))

    Assertions.assertEquals(expected, actual)
    verifyZeroInteractions(mockLoginRemoteSource)
    verifyZeroInteractions(mockUserDataLocalStorage)
}
```

`Assertions.assertEquals` throws an exception if the `expected` is not equal to the `actual` value. The first parameter is the expected in all assertion methods.

Before running the test don't forget to remove the `@Disabled` annotation.

### 2. `emptyPasswordNameReturnsLoginStatusError`

Next do the same thing for `emptyPasswordNameReturnsLoginStatusError`

This is really similar, so try to write it on your own, but if you get stuck, the code is here:

```kotlin
@DisplayName("GIVEN empty password WHEN trying to login THEN invalid password is returned")
@Test
fun emptyPasswordNameReturnsLoginStatusError() = runTest {
    val expected = Answer.Success(LoginStatus.INVALID_PASSWORD)

    val actual = sut.invoke(LoginCredentials("a", ""))

    Assertions.assertEquals(expected, actual)
    verifyZeroInteractions(mockLoginRemoteSource)
    verifyZeroInteractions(mockUserDataLocalStorage)
}
```

You may think that's bad to duplicate code in such a way, but you need to remember in testing it's not as important to not duplicate code.
Also we have the possibility to reduce this duplication, we will touch on this later in the app module tests.

### 3. `invalidLoginResponseReturnInvalidCredentials`

Let's continue with `invalidLoginResponseReturnInvalidCredentials`

As before we declare what we expect:

```kotlin
val expected = Answer.Success(LoginStatus.INVALID_CREDENTIALS)
```

Now we need to mock the response on our RemoteSource, since we actually expect some kind of response from it. To do this we add the following line:

```kotlin
whenever(mockLoginRemoteSource.login(LoginCredentials("a", "b"))).doReturn(LoginStatusResponses.InvalidCredentials)
```

This means whenever our `mockLoginRemoteSource` login function is called with an argument equal to `LoginCredentials("a", "b")`, then `LoginStatusResponses.InvalidCredentials` is returned.
Otherwise by default usually null is returned.

It reads nicely in my opinion.

Next our invocation:

```kotlin
val actual = sut.invoke(LoginCredentials("a", "b"))
```

And finally verification:

```kotlin
Assertions.assertEquals(expected, actual)
verifyZeroInteractions(mockUserDataLocalStorage)
```

Together:
```kotlin
@DisplayName("GIVEN invalid credentials response WHEN trying to login THEN invalid credentials is returned")
@Test
fun invalidLoginResponseReturnInvalidCredentials() = runTest {
    val expected = Answer.Success(LoginStatus.INVALID_CREDENTIALS)
    whenever(mockLoginRemoteSource.login(LoginCredentials("a", "b")))
        .doReturn(LoginStatusResponses.InvalidCredentials)

    val actual = sut.invoke(LoginCredentials("a", "b"))

    Assertions.assertEquals(expected, actual)
    verifyZeroInteractions(mockUserDataLocalStorage)
}
```

With that we saw how we can mock responses.

### 4. `validResponseResultsInSavingSessionAndSuccessReturned`,

Now continue with `validResponseResultsInSavingSessionAndSuccessReturned`. You should have almost every tool to do this test:
- declare the expected value
- define the mock response
- call the System Under Test
- verify the actual result to the expected
- verify the localStorage's session was saved once, and only once: `verify(mockUserDataLocalStorage, times(1)).session = Session("c", "d")`
- verify the localStorage was not touched anymore: `verifyNoMoreInteractions(mockUserDataLocalStorage)`

The full code:
```kotlin
@DisplayName("GIVEN success response WHEN trying to login THEN session is saved and success is returned")
@Test
fun validResponseResultsInSavingSessionAndSuccessReturned() = runTest {
    val expected = Answer.Success(LoginStatus.SUCCESS)
    whenever(mockLoginRemoteSource.login(LoginCredentials("a", "b")))
        .doReturn(LoginStatusResponses.Success(Session("c", "d")))

    val actual = sut.invoke(LoginCredentials("a", "b"))

    Assertions.assertEquals(expected, actual)
    verify(mockUserDataLocalStorage, times(1)).session = Session("c", "d")
    verifyNoMoreInteractions(mockUserDataLocalStorage)
}
```

### 5. `invalidResponseResultsInErrorReturned`

This is really similar to our previous test, however now somehow we have to mock throwing an exception

To do this let's create an exception:

```kotlin
val exception = RuntimeException()
```

Declare our expected value:

```kotlin
val expected = Answer.Error<LoginStatus>(UnexpectedException(exception))
```

Do the mocking:

```kotlin
whenever(mockLoginRemoteSource.login(LoginCredentials("a", "b"))).doThrow(exception)
```

Invocation:

```kotlin
val actual = sut.invoke(LoginCredentials("a", "b"))
```

Verification:

```kotlin
Assertions.assertEquals(expected, actual)
verifyZeroInteractions(mockUserDataLocalStorage)
```

Together:

```kotlin
@DisplayName("GIVEN error resposne WHEN trying to login THEN session is not touched and error is returned")
@Test
fun invalidResponseResultsInErrorReturned() = runTest {
    val exception = RuntimeException()
    val expected = Answer.Error<LoginStatus>(UnexpectedException(exception))
    whenever(mockLoginRemoteSource.login(LoginCredentials("a", "b")))
        .doThrow(exception)

    val actual = sut.invoke(LoginCredentials("a", "b"))

    Assertions.assertEquals(expected, actual)
    verifyZeroInteractions(mockUserDataLocalStorage)
}
```
#### Lessons learned
- Now we saw how to mock invocations on our mock objects
- How to run our tests
- How to test suspend functions
- and the pattern of GIVEN-WHEN-THEN description.

## Our third Class Test with flows

Our system under test will be
```kotlin
org.fnives.test.showcase.core.content.ContentRepository
```

It has two methods:
- getContents: that returns a Flow, which emits loading, error and content data
- fetch: which suppose to clear cache and if the flow is observed then start loading

The content data come from a RemoteSource class.
Additionally the Content is cached. So observing again should not yield loading.

The inner workings of the class shouldn't matter, just the public apis, since that's what we want to test, always.

Our Test class will be
```kotlin
org.fnives.test.showcase.core.content.CodeKataContentRepositoryTest
```

For setup we declare the system under test and its mock argument as usual.

```kotlin
private lateinit var sut: ContentRepository
private lateinit var mockContentRemoteSource: ContentRemoteSource

@BeforeEach
fun setUp() {
    mockContentRemoteSource = mock()
    sut = ContentRepository(mockContentRemoteSource)
}
```

### 1. `fetchingIsLazy`

As usual we are staring with the easiest test. We verify that the request is not called until the flow is not touched.

So just verify the request is not called yet:

```kotlin
@DisplayName("GIVEN no interaction THEN remote source is not called")
@Test
fun fetchingIsLazy() {
   verifyNoMoreInteractions(mockContentRemoteSource)
}
```

### 2. `happyFlow`

Next logical step is to verify the Happy flow. We setup the request to succeed and expect a Loading and Success state to be returned.

```kotlin
val expected = listOf(
    Resource.Loading(),
    Resource.Success(listOf(Content(ContentId("a"), "", "", ImageUrl(""))))
)
whenever(mockContentRemoteSource.get()).doReturn(listOf(Content(ContentId("a"), "", "", ImageUrl(""))))
```

Next the action:

```kotlin
val actual = sut.contents.take(2).toList()
```

Now just the verifications:

```kotlin
Assertions.assertEquals(expected, actual)
````

Notice we don't verify the request has been called, since it's implied. It returns the same data we returned from the request, so it must have been called.

### 3. ```errorFlow```

This is really similar to the happy flow, only we throw and expect specific errors:

```kotlin
val exception = RuntimeException()
val expected = listOf(
    Resource.Loading(),
    Resource.Error<List<Content>>(UnexpectedException(exception)) // Note since RuntimeException is not usually sent from NetworkRequest we expect an UnexpectedException.
)
whenever(mockContentRemoteSource.get()).doThrow(exception)
```

The action and verification stays the same:
```koltin
val actual = sut.contents.take(2).toList()

Assertions.assertEquals(expected, actual)
```

### 4. `verifyCaching`

Still sticking to just that function, we should verify its caching behaviour, aka if a data was loaded once the next time we observe the flow that data is returned:

The setup is similar to the happy flow, but take a look at the last line closely
```kotlin
val content = Content(ContentId("1"), "", "", ImageUrl(""))
val expected = listOf(Resource.Success(listOf(content)))
whenever(mockContentRemoteSource.get()).doReturn(listOf(content))
sut.contents.take(2).toList() // note this is part of the setup since we want the class in a state where it has a cache!
```

The action will only take one element which we expect to be the cache
```kotlin
val actual = sut.contents.take(1).toList()
```

In the verification state, we will also make sure the request indeed was called only once:
```kotlin
verify(mockContentRemoteSource, times(1)).get()
Assertions.assertEquals(expected, actual)
```

### 5. `loadingIsShownBeforeTheRequestIsReturned`

So far we just expected the first element is "loading", but it could easily happen that the flow is set up in such a way that the loading is not emitted before the request already finished.

This can be an easy mistake with such flows, but would be really bad UX, so let's see how we can verify something like that:

We need to suspend the request calling. Verify that before the request call is finished the Loading is already emitted.
So the issue becomes how can we suspend the mock until a signal is given.

Generally we could still use mockito mocks OR we could create our own Mock (Fake).

#### Creating our own mock.

We can simply implement the interface of ContentRemoteSource. Have it's method suspend until a signal.

Something along the way of:

```kotlin
class SuspendingContentRemoteSource {

    private var completableDeferred = CompletableDeferred<Unit>()

    @Throws(NetworkException::class, ParsingException::class)
    suspend fun get(): List<Content> {
       completableDeferred = CompletableDeferred()
       completableDeferred.await()
       return emptyList()
    }

    fun signal() = completableDeferred.complete(Unit)
}
```

In this case we should recreate our sut in the test and feed it our own remote source for this test.

#### Still using mockito.

To mock such behaviour with mockito with our current tool set is not as straight forward as creating our own.
That's because how we used mockito so far it is not aware of the nature of suspend functions, like our code is in the custom mock.

However mockito gives us the arguments passed into the function.
And since we know the Continuation object is passed as a last argument in suspend functions we can take advantage of that.
This then can be abstracted away and used wherever without needing to create Custom Mocks for every such case.

To get arguments when creating a response for the mock you need to use thenAnswer { } and this lambda will receive InvocationOnMock containing the arguments.

Luckily this has already been done in "org.mockito.kotlin" and it's called `doSuspendableAnswer`

The point here is that we can get arguments while mocking with mockito, and we are able to extend it in a way that helps us in common patterns.

This `doSuspendableAnswer` wasn't available for a while, but we could still create it on our own before, if it was needed.

#### Back to the actual test

Our setup as mentioned will suspend the request answer but expect a Loading state regardless:

```kotlin
val expected = Resource.Loading<List<Content>>()
val suspendedRequest = CompletableDeferred<Unit>()
whenever(mockContentRemoteSource.get()).doSuspendableAnswer {
    suspendedRequest.await()
    emptyList()
}
```

Our action simply takes the first element:

```kotlin
val actual = sut.contents.take(1).toList()
```

In verification we verify that value is as expected and clean up the suspension of the request (just so it's explicit what we are testing)

```kotlin
Assertions.assertEquals(listOf(expected), actual)
suspendedRequest.complete(Unit)
```

### 6. `whenFetchingRequestIsCalledAgain`

We still didn't even touch the fetch method so let's test that behaviour next:

We want to get the first result triggered by the subscription to the flow, and then again another loading and result after a call to `fetch`, so the setup would be:
```kotlin
val exception = RuntimeException()
val expected = listOf(
    Resource.Loading(),
    Resource.Success(emptyList()),
    Resource.Loading(),
    Resource.Error<List<Content>>(UnexpectedException(exception))
)
    var first = true
whenever(mockContentRemoteSource.get()).doAnswer {
    if (first) emptyList<Content>().also { first = false } else throw exception // notice first time we return success next we return error
}
```

However the main issue here is, when to call fetch? If we call after `take()` we will never reach it since we are suspended by take. But if we call it before then it doesn't test the right behaviour.
We need to do it async:

```kotlin
val actual = async { sut.contents.take(4).toList() }
sut.fetch()
```

And the verification as usual is really simple
```kotlin
Assertions.assertEquals(expected, actual.await())
```

However this test will hang. This is because `runTest` uses by default `StandardTestDispatcher` which doesn't enter child coroutines immediately and the async block will only be executed after the call to fetch.
This is a good thing because it gives us more control over the order of execution and as a result our tests are not shaky.
To make sure that `fetch` is called only when `take` suspends, we can call `advanceUntilIdle` which will give the opportunity of the async block to execute.
So our test becomes:
```kotlin
val actual = async { sut.contents.take(4).toList() }
advanceUntilIdle()
sut.fetch()
```

If we run this test, now it will pass. Let's break down exactly what happens now:
 - The test creates the exception, expected, mocking and create the async but doesn't start it
 - advanceUntilIdle will run the async until it's suspended, aka it receives two elements
 - Now we get back to advanceUntilIdle and call sut.fetch()
 - Note: at this point the async is still suspended
 - Then actual.await() will suspend so the async continues until it finishes
 - async received all the elements, by continuing the flow
 - async finishes so we compare values
 - => This shows us that we have full control over the execution order which makes `runTest` a great utility for us.

Alternatively we can make `runTest` use `UnconfinedTestDispatcher` which will enter child coroutines eagerly, so our `async` will be executed until it suspends and only after the main execution path will continue with the call to `fetch` and we don't need `advanceUntilIdle` anymore.
```kotlin
@Test
fun whenFetchingRequestIsCalledAgain() = runTest(UnconfinedTestDispatcher()) {
    ... // setup here

    val actual = async { sut.contents.take(4).toList() }
    sut.fetch()

    Assertions.assertEquals(expected, actual.await())
}
```
Let's break down what changed with `UnconfinedTestDispatcher`
 - The test still creates the exception, expected, mocking and create the async but doesn't start it
 - The test creates the async and starts to execute it
 - async suspends after the 2nd element received
 - at this point the next execution is `sut.fetch()` since async got suspended
 - Then actual.await() will suspend so the async continues until it finishes
 - async received all the elements, by continuing the flow
 - async finishes so we compare values
 - => This shows us `UnconfinedTestDispatcher` basically gave us the same execution order except the manual declaration of `advanceUntilIdle`

##### Now we can test even complicated interactions between methods and classes with test dispatchers.

### 7. `noAdditionalItemsEmitted`

Lastly so far we always assumed that we are getting the exact number of values take(4), take(2). However it's possible our flow may send out additional unexpected data.
So we also need to test that this assumption is correct.

I think the best place to start from is our most complicated test `whenFetchingRequestIsCalledAgain` since this is the one most likely add additional unexpected values.

Luckily `async.isCompleted` is helpful here: We can check if the async actually finished, aka if it still suspended or complete.
Alternatively when checking with values, we may use `async.getCompleted()` as well, since if a coroutine didn't finish properly it will throw an `IllegalStateException("This job has not completed yet")`.

So all we need to do is verify that the actual deferred is completed at the end.
With this we no longer need the expected values.

So our method looks similar to `whenFetchingRequestIsCalledAgain` except:
- We no longer have expected values
- We check if the async is completed
- We need an additional `advanceUntilIdle` after fetch so the async has a possibility to actually complete
- And requesting 5 elements instead of 4.
- And cancel the async since we no longer need it

Note: if it confuses you why we need the additional `advanceUntilIdle` refer to the execution order descried above. The async got their 3rd and 4th values because we were using await.
```kotlin
@DisplayName("GIVEN content response THEN error WHEN fetched THEN only 4 items are emitted")
@Test
fun noAdditionalItemsEmitted() = runTest {
    val exception = RuntimeException()
    var first = true
    whenever(mockContentRemoteSource.get()).doAnswer {
        if (first) emptyList<Content>().also { first = false } else throw exception // notice first time we return success next we return error
    }

    val actual = async {
        sut.contents.take(5).toList()
    }
    advanceUntilIdle()
    sut.fetch()
    advanceUntilIdle()

    Assertions.assertFalse(actual.isCompleted)
    actual.cancel()
}
```

###### Now just to verify our test tests what we want, switch the 5 to a 4 and run the test again. If our test setup is correct, now it should fail, since we expect that the async doesn't complete.

### 8. Turbine `noAdditionalItemsEmittedWithTurbine`

Until now we were testing with async and taking values, this can be tidious for some, so here is an alternative:

[Turbine](https://github.com/cashapp/turbine) is library that provides some testing utilities for Flow.
The entrypoint is the `test` extension which collects the flow and gives you the opportunity to
assert the collected events.

To receive a new item from the flow we call `awaitItem()`, and to verify that no more items are
emitted we expect the result of `cancelAndConsumeRemainingEvents()` to be an empty list.

Keeping the same setup as in `whenFetchingRequestIsCalledAgain` we can use turbine to test `contents` as follows:
```kotlin
sut.contents.test {
    Assertions.assertEquals(expected[0], awaitItem())
    Assertions.assertEquals(expected[1], awaitItem())
    sut.fetch()
    Assertions.assertEquals(expected[2], awaitItem())
    Assertions.assertEquals(expected[3], awaitItem())
    Assertions.assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
}
```

The code seems pretty recognizable, the execution order follows what we have been doing before.
We can move the `fetch` before the first `awaitItem`, because `test` will immediately collect and buffer the first Loading and Success, so we can assert the items in a for loop like this:
```kotlin
sut.contents.test {
    sut.fetch()
    expected.forEach { expectedItem ->
        Assertions.assertEquals(expectedItem, awaitItem())
    }
    Assertions.assertTrue(cancelAndConsumeRemainingEvents().isEmpty())
}
```

## Conclusion

Here we went over most common cases when you need to test simple java / kotlin files with no reference to networking or android:

- how to setup and structure your test
- how to run your tests
- a convention to naming your tests
- how to use mockito to mock dependencies of your System Under Test objects
- how to test suspend functions
- how to test flows
- how to verify your mock usage
- how to assert responses
