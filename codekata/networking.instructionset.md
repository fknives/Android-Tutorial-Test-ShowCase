# 2. Starting of networking testing

In this testing instruction set you will learn how to write simple tests with retrofit and networking.

Every System Under Test will be injected to ensure the setup of Retrofit is correct.
In every test class will use a mocked response and verify the requests sent out.
It will also be verify how OAuth token refreshing can be tested.

I would suggest to open this document in your browser, while working in Android Studio.

## Simple network test

Our System Under Test will be `org.fnives.test.showcase.network.auth.LoginRemoteSourceImpl` But we will only test the login function. The refresh method is part of sesssion handling, that's why it is internal.

The login function sends out a retrofit request and parses a response for us, this is what we intend to test.

Let's setup our testClass: `org.fnives.test.showcase.network.auth.CodeKataLoginRemoteSourceTest`

### Setup
First since we are using Koin as Service Locator, we should extend KoinTest, thus giving us an easier way to access koin functions.

```kotlin
class CodeKataLoginRemoteSourceTest : KoinTest {
//...
}
```

Next we need to inject our System Under Test, setup koin and setup or MockServer. However we also need to tearDown our setup after every test. Let's take it step by step.

First we declare our required fields:
```kotlin
private val sut by inject<LoginRemoteSource>()
private lateinit var mockWebServer : MockWebServer
```

MockWebServer is what we will use to give Mock Responses to our Retrofit calls. For this we need to start it, and at the end tear it down.
```kotlin
@BeforeEach
fun setUp() {
    mockWebServer = MockWebServer()
    mockWebServer.start()
}

@AfterEach
fun tearDown() {
    mockWebServer.shutdown()
}
```

Now we need to setup our Koin:
```kotlin
@BeforeEach
fun setUp() {
    //...
    val mockNetworkSessionLocalStorage = mock<NetworkSessionLocalStorage>()
    startKoin {
        modules(
            createNetworkModules(
                baseUrl = BaseUrl(mockWebServer.url("mockserver/").toString()),
                enableLogging = true,
                networkSessionExpirationListenerProvider = mock(),
                networkSessionLocalStorageProvider = { mockNetworkSessionLocalStorage }
            ).toList()
        )
    }
}

@AfterEach
fun tearDown() {
    stopKoin()
    //...
}
```

We use mockwebserver's url as baseUrl thus every retrofit request we will send out the MockWebServer will capture and respond.
Koin also needs to be stopped so our LoginRemoteSource is injected every time.
`createNetworkModules` is the koin modules defining the network module's injections.
For now we do not care about `networkSessionExpirationListenerProvider`, and `networkSessionLocalStorageProvider`.
We enable logging so it's easier to see what happens in tests.

With this setup we are ready to start testing. Just for the sake of simplicity here is the full code:
```kotlin
private val sut by inject<LoginRemoteSource>()
private lateinit var mockWebServer: MockWebServer

@BeforeEach
fun setUp() {
    mockWebServer = MockWebServer()
    mockWebServer.start()
    startKoin {
        modules(
            createNetworkModules(
                baseUrl = BaseUrl(mockWebServer.url("mockserver/").toString()),
                enableLogging = true,
                networkSessionExpirationListenerProvider = { mock() },
                networkSessionLocalStorageProvider = { mock() }
            ).toList()
        )
    }
}

@AfterEach
fun tearDown() {
    stopKoin()
    mockWebServer.shutdown()
}
```

### 1. `successResponseParsedProperly`

Notice we are starting with `runBlocking` instead of `runTest`. That's because we do not have any concurrency, and also don't care about the Threads used by OkHttp, we just want to be sure to get the responses in sync.

First we need to setup mockwebserver to respond to our request and the expected value:

```kotlin
mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(readResourceFile("success_response_login.json")))
val expected = LoginStatusResponses.Success(Session(accessToken = "login-access", refreshToken = "login-refresh"))
```

As you can see we can set the responseCode of the request and a body. here we use a helper function which goes to the resources folder and reads the content of the given file.
Usually you will need to create your own files out of the expected responses so please check where the file is located.

Next, we declared the expected value, the accessToken and refreshToken come from the ResponseBody (aka the file), this is where it should be parsed from.

We then declare the action
```kotlin
val actual = sut.login(LoginCredentials(username = "a", password = "b"))
```

And at the end we just verify the response is what we expected
```kotlin
Assertions.assertEquals(expected, actual)
```

Now, running this test, you will see logs by OkHttpLoggingInterceptor and see what request was sent out and received.

### 2. `requestProperlySetup`

So far we verified how to parse a response, but what about the validity of the request send out. This is what we will test next:

First we setup the mockwebserver just like before, however we no longer care about the returned value:
```kotlin
mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(readResourceFile("success_response_login.json")))
```

Next we call the function, and get the sent requestData from MockWebServer:
```kotlin
sut.login(LoginCredentials("a", "b"))
val request = mockWebServer.takeRequest()
```

Now that we have the requestData we can do verifications on it.
In this case we need to verify the method, headers, path, requestBody:

```kotlin
// we expect it's a POST HTTP request
Assertions.assertEquals("POST", request.method)

// we verify the Platform header is sent correctly
Assertions.assertEquals("Android", request.getHeader("Platform"))

// we verify the request doesn't contain any authorization
Assertions.assertEquals(null, request.getHeader("Authorization"))

// we verify the path of the request, "/mockserver" part comes from the setup,
// when we gave koin the url. With this we also verified the base url is kept in the request.
Assertions.assertEquals("/mockserver/login", request.path)

val loginRequestBody = """
{
    "username": "a",
    "password": "b"
}
    """.trimIndent()
// Since the responseBody is json we use a library that can compare json properly
JSONAssert.assertEquals(loginRequestBody, request.body.readUtf8(), JSONCompareMode.NON_EXTENSIBLE)
```

With this we can be sure our request contains exactly what we want it to contain.

### 3. `badRequestMeansInvalidCredentials`

Now we take a look at an expected error test:

First we setup or mockwebserver to return 400. This should mean our credentials were invalid.

```kotlin
mockWebServer.enqueue(MockResponse().setResponseCode(400).setBody(""))
val expected = LoginStatusResponses.InvalidCredentials
```

Next we get the actual value and verify it:

```kotlin
val actual = sut.login(LoginCredentials("a", "b"))

Assertions.assertEquals(expected, actual)
```
Notice we expected this error so no exception is thrown.

### 4. `genericErrorMeansNetworkError`

Next, let's see if we get an unexpected response code. In such case we actually except a specific exception to be thrown, so it looks like this:

```kotlin
mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody(""))

val actual = Assertions.assertThrows(NetworkException::class.java) {
    runBlocking { sut.login(LoginCredentials("a", "b")) }
}
```

Now we can verify the details of the exception as well:
```kotlin
Assertions.assertEquals("HTTP 500 Server Error", actual.message)
Assertions.assertTrue(actual.cause is HttpException)
```

### 5. `invalidJsonMeansParsingException`

We also need to verify if we get an unexpected json, we handle that case properly:

```kotlin
mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

val actual = Assertions.assertThrows(ParsingException::class.java) {
    runBlocking { sut.login(LoginCredentials(username = "a", password = "b")) }
}

// you not necessarily care about the details of the exception, but here it's just described how to do it.
Assertions.assertEquals("Expected BEGIN_OBJECT but was BEGIN_ARRAY at path \$", actual.message)
Assertions.assertTrue(actual.cause is JsonDataException)
```

### 6. `missingFieldJsonMeansParsingException`
We also need to verify if a field is missing the error is not just ignored:
```kotlin
mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

val actual = Assertions.assertThrows(ParsingException::class.java) {
    runBlocking { sut.login(LoginCredentials(username = "a", password = "b")) }
}

Assertions.assertEquals("Required value 'accessToken' missing at \$", actual.message)
Assertions.assertTrue(actual.cause is JsonDataException)
```

### 7. `malformedJsonMeansParsingException`

We can also get malformed json from the Backend, we still shouldn't crash:
```kotlin
mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{"))

Assertions.assertThrows(ParsingException::class.java) {
   runBlocking { sut.login(LoginCredentials("a", "b")) }
}
```

### Conclusion

With these types of responses we should be covered for the generic errors that could happen.
Based on what the request is doing additional tests can be added for every specific HTTP code / response the client should handle. Now you should be equipped to deal with that.

## Token Refreshing test

In order to verify OAuth token refreshing we need to have a remote source that is session dependent, for this we will use `org.fnives.test.showcase.network.content.ContentRemoteSource`

So let's describe exactly what happens with session expiration and token refreshing:
- a request is called that needs authentication
- the server responds with 401 meaning the token is expired
- we need to call refresh token request
- we get a new token and call the request again
- or we get an error and we propagate the error and send a sessionExpiration notice

With that in mind open `Corg.fnives.test.showcase.network.content.odeKataSessionExpirationTest`

The setup is alreay done since it's equivalent to our LoginRemoteSource tests except the mocks for storage and expiration listener which we now will care about.

### 1. `successRefreshResultsInRequestRetry`

First we need to setup our mockwebserver with the expected requests:
- 401 content request
- success refresh token response
- success content request with the new token

Also we need to update our `mockNetworkSessionLocalStorage` so it returns a beforeSession and later returns any session that was saved into it.

So together:
```kotlin
mockWebServer.enqueue(MockResponse().setResponseCode(401))
mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(readResourceFile("success_response_login.json")))
mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

var sessionToReturnByMock: Session? = Session(accessToken = "before-access", refreshToken = "before-refresh") // before session
whenever(mockNetworkSessionLocalStorage.session).doAnswer { sessionToReturnByMock } // whenever requested return the value of sessionToReturnByMock
doAnswer { sessionToReturnByMock = it.arguments[0] as Session? }
    .whenever(mockNetworkSessionLocalStorage).session = anyOrNull() // whenever set is called get the argument and save it into the sessionToReturnByMock
```

The MockWebServer setup is standard, these responses will be returned one after another.
The mocking of storage may look a bit strange, but as described in the comments, we return a variable when accessed, the lambda is executed for each access.
For overwriting the value we overwrite the setter and save the argument into the `sessionToReturnByMock`
Basically we mocked out a modifiable field.

Now we need to take the action and get the requests to verify:
```kotlin
sut.get()

mockWebServer.takeRequest() // we don't really care of the first request
val refreshRequest = mockWebServer.takeRequest()
val contentRequestAfterRefreshed = mockWebServer.takeRequest()
```

Next we need to verify
- the refresh request was properly setup
- the new content request used the updated access token
- no session expiration event was sent and token was saved

```kotlin
Assertions.assertEquals("PUT", refreshRequest.method)
Assertions.assertEquals("/mockserver/login/before-refresh", refreshRequest.path)
Assertions.assertEquals(null, refreshRequest.getHeader("Authorization"))
Assertions.assertEquals("Android", refreshRequest.getHeader("Platform"))
Assertions.assertEquals("", refreshRequest.body.readUtf8())

Assertions.assertEquals("login-access", retryAfterTokenRefreshRequest.getHeader("Authorization"))

// this matches the data from the success_response_login.json
val expectedSavedSession = Session(accessToken = "login-access", refreshToken = "login-refresh")
verify(mockNetworkSessionLocalStorage, times(1)).session = expectedSavedSession
verifyZeroInteractions(mockNetworkSessionExpirationListener)
```

### 2. `failingRefreshResultsInSessionExpiration`

Now we need to test what if the refresh request fails.

First setup for failuire:
```kotlin
mockWebServer.enqueue(MockResponse().setResponseCode(401))
mockWebServer.enqueue(MockResponse().setResponseCode(400))
whenever(mockNetworkSessionLocalStorage.session).doReturn(Session(accessToken = "before-access", refreshToken = "before-refresh"))
```

Next verify do the action which will throw!

```kotlin
Assertions.assertThrows(NetworkException::class.java) {
   runBlocking { sut.get() }
}
```

Lastly verify the session was cleared and session expiration notified:

```kotlin
verify(mockNetworkSessionLocalStorage, times(3)).session // storage was accessed for the requests
verify(mockNetworkSessionLocalStorage, times(1)).session = null // storage was cleared
verifyNoMoreInteractions(mockNetworkSessionLocalStorage)
verify(mockNetworkSessionExpirationListener, times(1)).onSessionExpired() // listener was updated
```

### Conclusion

With these presented tests we can verify:
- our requests send the proper data
- our parsing is proper
- our error handling handles edge cases as well

### Reusability

Now if you wondered around the non-CodeKata Test files, you noticed it uses the mockserver module.

The basic idea with this is that it contains all the response, request jsons and make it easier to setup sepcific scenarios.

This doesn't make sense if we are only testing networking, however if we want to write instrumentation / feature tests as well, there we also need to mock out the requests.

So this can be one way to do mocking the same way for both cases, but that's up to you how to handle this case.