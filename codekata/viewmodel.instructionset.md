# 3. Starting of ViewModel testing

In this testing instruction set you will learn how to write simple tests for ViewModels.

- We will use TestDispatcher for time manipulation
- Learn how to use TestDispatcher in ViewModels
- How to test LiveData
- How to use extensions
- How to parametrize a test

## SplashViewModel test

Our System Under Test will be `org.fnives.test.showcase.ui.splash.SplashViewModel`

What it does is:
- waits 500 milliseconds
- checks if the user logged in
- sends navigated event based on the check

### Setup

So let's start with the setup.

Our test class is `org.fnives.test.showcase.ui.splash.CodeKataSplashViewModelTest`

To properly test LiveData we need to make them instant, meaning as soon as the value is set the observers are updated. To Do this we can use a `InstantExecutorExtension`.

Also We need to set MainDispatcher as TestDispatcher, for this we can use the `TestMainDispatcher` Extension.

To add this to our TestClass we need to do the following:

```kotlin
@ExtendWith(InstantExecutorExtension::class, TestMainDispatcher::class)
class CodeKataSplashViewModelTest {
```

Note: you can use `@RegisterExtension` to register an extension as a field and make it easier to reference.

Next let's setup our System Under Test as usual:

```kotlin
private lateinit var mockIsUserLoggedInUseCase: IsUserLoggedInUseCase
private lateinit var sut: SplashViewModel
private val testScheduler get() = TestMainDispatcher.testDispatcher.scheduler // just a shortcut

@BeforeEach
fun setUp() {
    mockIsUserLoggedInUseCase = mock() // the only dependency of the ViewModel
    sut = SplashViewModel(mockIsUserLoggedInUseCase)
}
```

### 1. `loggedOutUserGoesToAuthentication`

We want to test that if the user is not logged in then we are navigated to the Authentication screen.
So we need to setup the mock's response:

```kotlin
whenever(mockIsUserLoggedInUseCase.invoke()).doReturn(false)
```

Next up we want to setup our TestObserver for LiveData. This enables us to verify the values sent into a LiveData.
If a LiveData is not observed, its value may not be updated (like a LiveData that maps) so it's important to have a proper TestObserver set.


```kotin
val navigateToTestObserver = sut.navigateTo.test()
```

Since the action takes place in the ViewModel constructor, instead of additional calls, we need to simulate that time has elapsed.

Note: the `TestMainDispatcher` Extension we are using sets `StandardTestDispatcher` as the dispatcher for `Dispatcher.Main`, that's why our test is linear and not shaky.

```kotlin
testScheduler.advanceTimeBy(501)
```

Next, we verify that we navigated to Authentication and only to Authentication:

```kotlin
navigateToTestObserver.assertValueHistory(Event(SplashViewModel.NavigateTo.AUTHENTICATION))
```

### 2. `loggedInUserGoestoHome`

This is really similar to `loggedOutUserGoesToAuthentication`, so try to implement on your own.

However for completness, here is the code:

```kotlin
whenever(mockIsUserLoggedInUseCase.invoke()).doReturn(true)
val navigateToTestObserver = sut.navigateTo.test()

testScheduler.advanceTimeBy(501)

navigateToTestObserver.assertValueHistory(Event(SplashViewModel.NavigateTo.HOME))
```

### 3. `withoutEnoughTimeNoNavigationHappens`

Now let's verify that if the time didn't elapse then the event is not sent out.
The setup is the same, expect less time:

```kotlin
whenever(mockIsUserLoggedInUseCase.invoke()).doReturn(false)
val navigateToTestObserver = sut.navigateTo.test()

testScheduler.advanceTimeBy(100) // we wait only 100ms not 500ms
```

And as verification we just check that no values were submitted.

```kotlin
navigateToTestObserver.assertNoValue() // this is the way to test that no value has been sent out
```

With this we completed the SplashViewModel test. It is really simple, but it introduced extensions scheduling and LiveData testing.

## AuthViewModelTest Test

Our System Under Test will be `org.fnives.test.showcase.ui.auth.AuthViewModel`

What it does is:
- observes input username and password
- tries to login with the given data
- processes the response and either navigates or shows an error

Let's open `org.fnives.test.showcase.ui.auth.CodeKataAuthViewModel`.

The setup is already done because it's almost the same as mentioned in CodeKataSplashViewModelTest.

### 1. `initialSetup`

As always we start with the easiest test. This usually gives us motivation and helps us get ideas for the next tests.

First we setup the observers:
```kotlin
val usernameTestObserver = sut.username.test()
val passwordTestObserver = sut.password.test()
val loadingTestObserver = sut.loading.test()
val errorTestObserver = sut.error.test()
val navigateToHomeTestObserver = sut.navigateToHome.test()
```

Next we advance the scheduler until everything is idle:
```kotlin
testScheduler.advanceUntilIdle()
```

And now, we verify the values:
```kotlin
usernameTestObserver.assertNoValue()
passwordTestObserver.assertNoValue()
loadingTestObserver.assertValue(false)
errorTestObserver.assertNoValue()
navigateToHomeTestObserver.assertNoValue()
```

### 2. `whenPasswordChangedLiveDataIsUpdated`

Here we need to test the LiveData updates as we change the password.

So first let's add a subscriber to the ViewModel which we plan to verify:

```kotlin
val usernameTestObserver = sut.username.test()
val passwordTestObserver = sut.password.test()
val loadingTestObserver = sut.loading.test()
val errorTestObserver = sut.error.test()
val navigateToHomeTestObserver = sut.navigateToHome.test()
```

Next we do the action and update the password and advance the scheduler:

```kotlin
sut.onPasswordChanged("a")
sut.onPasswordChanged("al")
```

Advance the test scheduler before proceeding with the verifications.

```kotlin
testScheduler.advanceUntilIdle()
```

And at the end we verify the passwordTestObserver was updated and the others weren't:

```kotlin
usernameTestObserver.assertNoValue()
passwordTestObserver.assertValueHistory("a", "al")
loadingTestObserver.assertValue(false)
errorTestObserver.assertNoValue()
navigateToHomeTestObserver.assertNoValue()
```

### 3. `whenUsernameChangedLiveDataIsUpdated`

This is essentially the same as whenPasswordChangedLiveDataIsUpdated, just for the username, so try to do it on your own.
However for the sake of completeness:

```kotlin
val usernameTestObserver = sut.username.test()
val passwordTestObserver = sut.password.test()
val loadingTestObserver = sut.loading.test()
val errorTestObserver = sut.error.test()
val navigateToHomeTestObserver = sut.navigateToHome.test()

sut.onUsernameChanged("bla")
sut.onUsernameChanged("blabla")
testScheduler.advanceUntilIdle()

usernameTestObserver.assertValueHistory("bla", "blabla")
passwordTestObserver.assertNoValue()
loadingTestObserver.assertValue(false)
errorTestObserver.assertNoValue()
navigateToHomeTestObserver.assertNoValue()
```

### 4. `noPasswordUsesEmptyStringInLoginUseCase`

Now let's test some actual logic:
If we didn't give username and password to the ViewModel when login is clicked we should see loading, and empty string passed to the UseCase

Let's setup to login:

```kotlin
val loadingTestObserver = sut.loading.test()
runBlocking {
    whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Error(Throwable()))
}
```

`anyOrNull()` just means we do not care what is passed, anything is accepted.

Let's do the action:

```kotlin
sut.onLogin()
testScheduler.advanceUntilIdle() // ensure the coroutine has run
```

Verify the loading and the useCase call:

```kotlin
loadingTestObserver.assertValueHistory(false, true, false)
runBlocking { verify(mockLoginUseCase, times(1)).invoke(LoginCredentials("", "")) }
verifyNoMoreInteractions(mockLoginUseCase)
```
### 5. `onlyOneLoginIsSentOutWhenClickingRepeatedly`

Clicking the button once works as expected. But what if the user clicks the button multiple times before the request finishes? Let's make sure we only do actual actions once in such case.

We just setup the UseCase:
```kotlin
runBlocking { whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Error(Throwable())) }
```

Next we click the button multiple times then dispatch.
```kotlin
sut.onLogin()
sut.onLogin()
testScheduler.advanceUntilIdle()
```

And we verify the UseCase was called only once:
```kotlin
runBlocking { verify(mockLoginUseCase, times(1)).invoke(LoginCredentials("", "")) }
verifyNoMoreInteractions(mockLoginUseCase)
```

### 6. `argumentsArePassedProperlyToLoginUseCase`

Okay, now let's verify the UseCase receives the proper data.
We setup the UseCase response and update the username and password:
```kotlin
runBlocking {
    whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Error(Throwable()))
}
sut.onPasswordChanged("pass")
sut.onUsernameChanged("usr")
testScheduler.advanceUntilIdle()
```

Next we do our action and click the button:
```kotlin
sut.onLogin()
testScheduler.advanceUntilIdle()
```

Now, we just verify the UseCase is called properly:
```kotlin
runBlocking {
    verify(mockLoginUseCase, times(1)).invoke(LoginCredentials("usr", "pass"))
}
verifyNoMoreInteractions(mockLoginUseCase)
```

### 7. `loginUnexpectedErrorResultsInErrorState`

Next up, we will test a network error state.

So we return and Answer.Error state from our UseCase and set up the TestObservers, as usual:
```kotlin
runBlocking {
   whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Error(Throwable()))
}
val loadingTestObserver = sut.loading.test()
val errorTestObserver = sut.error.test()
val navigateToHomeTestObserver = sut.navigateToHome.test()
```

The action is the same, login:
```kotlin
sut.onLogin()
testScheduler.advanceUntilIdle()
```

And we verify loading state, no navigation event and that it is indeed the correct error state:
```kotlin
loadingTestObserver.assertValueHistory(false, true, false)
errorTestObserver.assertValueHistory(Event(AuthViewModel.ErrorType.GENERAL_NETWORK_ERROR))
navigateToHomeTestObserver.assertNoValue()
```

### 8. `invalidStatusResultsInErrorState`

Time to test Errors.
First we setup our UseCase and the TestObservers:
```kotlin
runBlocking {
    whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Success(LoginStatus.INVALID_CREDENTIALS))
}
val loadingTestObserver = sut.loading.test()
val errorTestObserver = sut.error.test()
val navigateToHomeTestObserver = sut.navigateToHome.test()
```

As usual, next comes the action:
```kotlin
sut.onLogin()
testScheduler.advanceUntilIdle()
```

And verify the LiveData values:
```
loadingTestObserver.assertValueHistory(false, true, false)
errorTestObserver.assertValueHistory(Event(AuthViewModel.ErrorType.INVALID_CREDENTIALS))
navigateToHomeTestObserver.assertNoValue()
```

Probably you are already getting bored of writing almost the same tests, and we need 2 more tests just like this only for different Error types.
So let's not write the same test again, but parametrize this one instead.
First we need to annotate our test, signal that it should be parametrized:

```kotlin
@MethodSource("loginErrorStatusesArguments")
@ParameterizedTest(name = "GIVEN answer success loginStatus {0} WHEN login called THEN error {1} is shown")
fun invalidStatusResultsInErrorState(
    loginStatus: LoginStatus,
    errorType: AuthViewModel.ErrorType
)
```

Define the parameters for our tests, the method should be static and notice its name:
```kotlin
companion object {

    @JvmStatic
    fun loginErrorStatusesArguments(): Stream<Arguments?> = Stream.of(
        Arguments.of(LoginStatus.INVALID_CREDENTIALS, AuthViewModel.ErrorType.INVALID_CREDENTIALS),
        Arguments.of(LoginStatus.INVALID_PASSWORD, AuthViewModel.ErrorType.UNSUPPORTED_PASSWORD),
        Arguments.of(LoginStatus.INVALID_USERNAME, AuthViewModel.ErrorType.UNSUPPORTED_USERNAME)
    )
}
```

And let's just adjust the test to use the parameters:
```kotlin
runBlocking {
    whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Success(loginStatus))
}
//...
errorTestObserver.assertValueHistory(Event(errorType))
```

And now if we run the test we see 3 different tests, with different names based on the parameters.
Great, this is how we can reduce duplication in tests, without losing readability.

### 9. `successLoginResultsInNavigation`

And finally let's test the happy flow as well.

We setup the observers and the UseCase:
```kotlin
runBlocking {
    whenever(mockLoginUseCase.invoke(anyOrNull())).doReturn(Answer.Success(LoginStatus.SUCCESS))
}
val loadingTestObserver = sut.loading.test()
val errorTestObserver = sut.error.test()
val navigateToHomeTestObserver = sut.navigateToHome.test()
```

The action:
```kotlin
sut.onLogin()
testScheduler.advanceUntilIdle()
```

And finally the verification:
```kotlin
loadingTestObserver.assertValueHistory(false, true, false)
errorTestObserver.assertNoValue()
navigateToHomeTestObserver.assertValueHistory(Event(Unit))
```

## Conclusion
That concludes our ViewModel tests.
As you can see it's not too different from the previous tests, we just needed to add a couple of additional setup and helper classes.
With this we are able to:
- Test ViewModels
- Test LiveData
- Use TestScheduler for ViewModels
- Use Test Extensions
- Parametrize tests to reduce duplication
