# 4. Starting of integration testing

You probably got bored of Unit Testing if you got to this point, so let's switch it up a little.

In this testing instruction set you will learn how to write simple Integration tests for your Java module:

- How to write integration tests
- How to use Fakes
- How to depend on test modules
- Exercise parametrized tests
- Exercise Junit Extensions

## AuthIntegrationTest test

Our System Under Test will be all Authentication related public classes of Core module, so namely:
 - `org.fnives.test.showcase.core.login.IsUserLoggedInUseCase`
 - `org.fnives.test.showcase.core.login.LoginUseCase`
 - `org.fnives.test.showcase.core.login.LogoutUseCase`

What we want to test here, is that all components hidden behind these classes together let the user login, store their session and logout.

### Setup

So let's open up our test class: `org.fnives.test.showcase.core.integration.CodeKataAuthIntegrationTest`

First, we want to take advantage of our DI module, so let's inject our actual classes:
```kotlin
private val isUserLoggedInUseCase by inject<IsUserLoggedInUseCase>()
private val loginUseCase by inject<LoginUseCase>()
private val logoutUseCase by inject<LogoutUseCase>()
```

Now let's startKoin in our setup method:
```kotlin
@BeforeEach
fun setup() {
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

Okay, a couple of things are missing. First of what are those fakes? Let's start with them

#### Fakes

So the `FavouriteContentLocalStorage` and `UserDataLocalStorage` will be injected into our modules.
However, we expect a specific behaviour from them.

So instead of mocking them, let's create simple fakes, that we can use in our tests, as they were the real class.

Let's start with `FakeUserDataLocalStorage`.

###### Let's open `CodeKataUserDataLocalStorage`.

This has to extend the `UserDataLocalStorage` interface, so add that. And the only required implementation is a modifiable field. So add it as a constructor argument and that's it.

```kotlin
class CodeKataUserDataLocalStorage(override var session: Session? = null) : UserDataLocalStorage
```

###### Now let's open `CodeKataFavouriteContentLocalStorage`.

This is a bit more tricky, there are multiple methods.

First of all we need a flow, so let's just use a SharedFlow and initialize it:
```kotlin
private val dataFlow = MutableSharedFlow<List<ContentId>>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
)

init {
    dataFlow.tryEmit(emptyList())
}
```

With that we can return our flow from `observeFavourites`:

```kotlin
override fun observeFavourites(): Flow<List<ContentId>> = dataFlow.asSharedFlow()
```

And our methods just need to update the flow as it would be expected:
```kotlin
override suspend fun markAsFavourite(contentId: ContentId) {
    dataFlow.emit(dataFlow.replayCache.first().plus(contentId))
}

override suspend fun deleteAsFavourite(contentId: ContentId) {
    dataFlow.emit(dataFlow.replayCache.first().minus(contentId))
}
```

Okay, we have our fakes. Let's navigate back to `CodeKataAuthIntegrationTest`

#### Continue Setup

Let's just declare our fakes and initialize them in the setup:
```kotlin
private lateinit var fakeFavouriteContentLocalStorage: FavouriteContentLocalStorage
private lateinit var mockSessionExpirationListener: SessionExpirationListener
private lateinit var fakeUserDataLocalStorage: UserDataLocalStorage

@Before
fun setup() {
    mockSessionExpirationListener = mock() // we are using mock, since it only has 1 function so we just want to verify if it's called
    fakeFavouriteContentLocalStorage = FakeFavouriteContentLocalStorage()
    fakeUserDataLocalStorage = FakeUserDataLocalStorage(null)
    startKoin {
        ///...
    }
}
```

We are still missing `mockServerScenarioSetupExtensions` this will be our TestExtension, to initialize MockWebServer.
`MockServerScenarioSetupExtensions` is declared in the `:network` test module.
However we are still able to import it.

That's because of [java-test-fixtures](https://docs.gradle.org/current/userguide/java_testing.html#sec:java_test_fixtures) plugin. It can be used to depend on a specific test module "textFixtures".
Check out the build.gradle's to see how that's done.
This can be useful to share some static Test Data, or extensions in our case.

> Test Fixtrues creates a new sourceset between the production code and the test code. test depends on testFixtures and testFixtures depends on source. So test sees everything in testFixtures and other modules can also use testFixtures. This way we can share extensions or other helper classes.
> An alternative to use test code between modules instead of TestFixtures is to use a separate module, like the :mockserver defined in the project.

So let's add this extension:
```kotlin
@RegisterExtension
@JvmField
val mockServerScenarioSetupExtensions = MockServerScenarioSetupExtensions()
private val mockServerScenarioSetup get() = mockServerScenarioSetupExtensions.mockServerScenarioSetup
```

This extension is a wrapper around MockWebServer containing setups of requests, request verifications and ContentData.
It is useful to mock our requests with this extension from now on so we don't repeat ourselves.

With that let's start testing:

### 1. `withoutSessionTheUserIsNotLoggedIn`

As usual, we start with the simplest test. Let's verify that if the session object is null, we are indeed logged out:

```kotlin
@DisplayName("GIVEN no session saved WHEN checking if user is logged in THEN they are not")
@Test
fun withoutSessionTheUserIsNotLoggedIn() = runTest {
    fakeUserDataLocalStorage.session = null
    val actual = isUserLoggedInUseCase.invoke()

    Assertions.assertFalse(actual, "User is expected to be not logged in")
    verifyZeroInteractions(mockSessionExpirationListener)
}
```

### 2. `loginSuccess`

Let's test that given good credentials and success response, our user can login in.

First we setup our mock server and the expected session:
```kotlin
mockServerScenarioSetup.setScenario(AuthScenario.Success(username = "usr", password = "sEc"), validateArguments = true) // validate arguments just verifies the request path, body, headers etc.
val expectedSession = ContentData.loginSuccessResponse
```

Now we login, and then check if we are actually logged in:
```kotlin
val answer = loginUseCase.invoke(LoginCredentials(username = "usr", password = "sEc"))
val actual = isUserLoggedInUseCase.invoke()
```

And just verify:
```kotlin
Assertions.assertEquals(Answer.Success(LoginStatus.SUCCESS), answer)
Assertions.assertTrue(actual, "User is expected to be logged in")
Assertions.assertEquals(expectedSession, fakeUserDataLocalStorage.session)
verifyZeroInteractions(mockSessionExpirationListener)
```

With this, looks like our Integration works correctly. Requests are called, proper response is received, login state is changed.

### 3. `localInputError`
We have to expected errors, that are returned even before running requests, if the username or password is empty.
This two tests would be really similar, so let's do Parametrized tests.

First we modify our method signature:
```kotlin
@MethodSource("localInputErrorArguments")
@ParameterizedTest(name = "GIVEN {0} credentials WHEN login called THEN error {1} is shown")
fun localInputError(credentials: LoginCredentials, loginError: LoginStatus)
```

Now let's declare our action:
```kotlin
val answer = loginUseCase.invoke(credentials)
val actual = isUserLoggedInUseCase.invoke()
```

And do our verifications, aka not logged in, not session expired and the correct error:
```kotlin
Assertions.assertEquals(Answer.Success(loginError), answer)
Assertions.assertFalse(actual, "User is expected to be not logged in")
Assertions.assertEquals(null, fakeUserDataLocalStorage.session)
verifyZeroInteractions(mockSessionExpirationListener)
```

Now we just need to declare our parameters for our test:
```kotlin
companion object {

    @JvmStatic
    fun localInputErrorArguments() = Stream.of(
        Arguments.of(LoginCredentials("", "password"), LoginStatus.INVALID_USERNAME),
        Arguments.of(LoginCredentials("username", ""), LoginStatus.INVALID_PASSWORD)
    )
}
```

With that we covered both of these errors.

### 4. `networkInputError`

Now let's do the same with network inputs. This will be really similar, only difference is we will initialize our mockserver with the AuthScenario.
Try to do it yourself, however for completeness sake, as usual, here is the code:
```kotlin
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

//...
companion object {
//...
    @JvmStatic
    fun networkErrorArguments() = Stream.of(
        Arguments.of(AuthScenario.GenericError(username = "a", password = "b")),
        Arguments.of(AuthScenario.UnexpectedJsonAsSuccessResponse(username = "a", password = "b")),
        Arguments.of(AuthScenario.MalformedJsonAsSuccessResponse(username = "a", password = "b")),
        Arguments.of(AuthScenario.MissingFieldJson(username = "a", password = "b"))
    )
}
```

### 5. `loginInvalidCredentials`

We have one more expected error type, but this comes from the NetworkResponse. We could add it as parametrized test, but for the sake of readability, let's just keep it separate.

This is really similar to the `networkInputError`, the differences are that this is not parametrized, we use `AuthScenario.InvalidCredentials` response and we expect `Answer.Success(LoginStatus.INVALID_CREDENTIALS)`

So together:
```kotlin
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
```

### 6. `logout`
Now let's verify if the user can logout properly.

For this we first need to have the user in a logged in state:
```kotlin
mockServerScenarioSetup.setScenario(AuthScenario.Success(username = "usr", password = "sEc"), validateArguments = true)
loginUseCase.invoke(LoginCredentials(username = "usr", password = "sEc"))
```

The user needs to logout:
```kotlin
logoutUseCase.invoke()
val actual = isUserLoggedInUseCase.invoke()
```

And we verify the user is indeed logged out now:
```kotlin
Assertions.assertFalse(actual, "User is expected to be logged out")
Assertions.assertEquals(null, fakeUserDataLocalStorage.session)
verifyZeroInteractions(mockSessionExpirationListener)
```

### 7. `logoutReleasesContent`
At last, let's verify that when the user logs out, their cache is released and the request is no longer authenticated.

To do this, first we setup our MockServer and login the user:
```kotlin
mockServerScenarioSetup.setScenario(AuthScenario.Success(username = "usr", password = "sEc"), validateArguments = true)
    .setScenario(ContentScenario.Success(usingRefreshedToken = false), validateArguments = true)
loginUseCase.invoke(LoginCredentials(username = "usr", password = "sEc"))
```

Now we get the content values before and after logout:
```kotlin
val valuesBeforeLogout = get<GetAllContentUseCase>().get().take(2).last()
logoutUseCase.invoke()
val valuesAfterLogout = get<GetAllContentUseCase>().get().take(2).last()
```
> Note: we are using get() from koin, since we don't want to depend on how the data is cleared and this way we get the UseCase a new user would get.

Now there is a bit of explaining to do. How `mockServerScenarioSetup` is setup is that if `validateArguments` is set, it will verify the path, the body and the authentication token. If it doesn't match, it will return a BAD Request.
We could do the same with MockWebServer and recorded request as well, it's just now hidden behind our TestHelper MockServer.

So what we want to verify, is that `valuesBeforeLogout` is a success, and the `valuesAfterLogout` is a failure.

```kotlin
Assertions.assertTrue(valuesBeforeLogout is Resource.Success, "Before we expect a cached Success")
Assertions.assertTrue(valuesAfterLogout is Resource.Error, "After we expect an error, since our request no longer is authenticated")
```
If it would be cached, the test would be stuck, cause Loading wouldn't be emitted, or if the request would be authenticated success would be returned as we setup Success response.

## Conclusions
With that we wrote our Integration tests.
There is no point of going over other integration test's in the core module, since the idea is captured, and nothing new could be shown.
If you want to give it a go, feel free, however consider using turbine for flow tests, cause it can be a bit tricky.

What we have learned:
- In integration tests, we mock the least amount of classes
- In integration tests we verify multiple classes and how they work together
- We learned we can share test classes between modules
- We learned how to write fakes
- We exercised the Parametrized tests
