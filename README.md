# Android Test Showcase

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Mock project showcasing testing on Android. I plan to use this as a [CodeKata](http://codekata.com/) to experiment with testing.
> Disclaimer: Every test you see in this project you should take with a pinch of salt, this is my self-taught experimentation of Testing on Android

## Project Overview
The project uses mock api and is really simplified. It's used just to showcase example features I usually encounter on Android.

You can login with any credentials, mark favourite content items, log out and that's about it.


As dependency injection / Service Locator [koin](https://insert-koin.io/) is used.

The application is separated into different modules each module is tested differently.

### Modules

- [model](#model)
- [app](#app)
- [core](#core)
- [network](#networking)
- [mockserver](#mock-server)

#### Model
Self explanatory, contains all shared data classes (POJOs) and Exceptions used by the other modules.

There is nothing to test here.

#### App

The android module of the application, contains the UI (Activities), Room-Database and ViewModels.

The UI is strucured in [MVVM](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel).

Has dependency on the [core module](#core).

There are 3 kinds of tests in this module:
- junit5 tests for ViewModels
- Robolectric Test for Room Database
- Shared Tests for Screens (shared between Robolectric and AndroidTests)

##### Unit tests
Verify the ViewModels are reacting to the mocked UseCases properly.

[Kotlin-Mockito](https://github.com/mockito/mockito-kotlin) is used to mock out UseCases comming from the core module.

[Koin-Test](https://insert-koin.io/docs/quickstart/junit-test/) is used to verify the ServiceLocator modules.

[LiveData-Testing](https://github.com/jraska/livedata-testing) is used to verify the LiveData values of ViewModels.

##### Robolectric
Verifies the DataBase interactions and also verifies the interactions on each Screen.

Robolectric [website link](http://robolectric.org/androidx_test/).

In [Unit](#unit-tests) and Robolectric tests [coroutine-test](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/) is used to switch out the mainThread thus enabling fine control over interactions.

##### AndroidTest
Verifies the interactions with the Screens.

In [Robolectric](#robolectric) and AndroidTests [Espresso](https://developer.android.com/training/testing/espresso) is used to interact with UI.

In [Robolectric](#robolectric) and AndroidTests [OkhttpIdlingResources](https://github.com/JakeWharton/okhttp-idling-resource) is used to synchronize OkHttp with Espresso.

In [Robolectric](#robolectric) and AndroidTests [Mock Server](mock-server) module is used to mock the network requests.

In [Robolectric](#robolectric) and AndroidTests [InstantTaskExecutor](https://developer.android.com/reference/androidx/arch/core/executor/testing/package-summary) is used to set LiveData values immediately and a Custom implementation in [Unit](#unit-tests) tests.

#### Core
Business layer of the application. Contains Repositories and UseCases.

Has dependency on the [network](#networking) module. Database/SharedPreferences LocalStorage classes are injected into the core module.

All tests are junit5 and they are using [Kotlin-Mockito](https://github.com/mockito/mockito-kotlin) to mock all dependencies.

[Coroutine-test](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/) is also used in every test.


The tests are verifying the interactions between the RemoteSource and LocalSource components. It also verifies that ErrorHandling is done properly.

#### Networking
As the name suggests this is the module which sends requests to the Backend and parses the responses received. All responses and requests are mapped to and from models from the [model](#model) data classes.

[Retrofit](https://square.github.io/retrofit/) + [OkHttp](https://square.github.io/okhttp/) + [Moshi](https://github.com/square/moshi) is used to send and parse requests.


All tests are Junit5. The Retrofit Services are injected into tests to make sure the parsing and other setups are proper.

The tests are verifying all the requests contain the correct arguments, headers, path, methods. It is also responsible to verify the responses are parsed properly.

[Mock Server](mock-server) module is used to mock the network requests.

[JsonAssert](https://github.com/skyscreamer/JSONassert) is used to compare JSON models.

#### Mock Server
This module is not actually part of the APK. This module is only used to unify mocking of Network request between Instrumentation tests from [app](#app) module and [network](#networking) module.

It contains a way to setup responses to requests in a unified way. Contains all Response.json and expected Request.json files.


[MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver) is used to respond to the requests sent by OkHttp.

[JsonAssert](https://github.com/skyscreamer/JSONassert) is used to compare JSON models.

[OkHttp-TLS](https://github.com/square/okhttp/tree/master/okhttp-tls) is used to have HTTPS requests on Android Tests.

## Server
The actual server when running the application is [mockapi.io](https://www.mockapi.io/) so don't expect actual functionalities in the application.

## Code Kata
TODO

The kata will be in a separate branch with instructions and empty tests to be filled.

## Future Plans for Myself
This just contains todos for myself when next time I am exercising testing on this project

- Finish Code Kata section, by adding new branch with empty tests and instructions
- Add Room migration tests
- Update RecyclerView tests with [Espresso.onData](https://developer.android.com/training/testing/espresso/lists)
- Add [NavController](https://developer.android.com/reference/androidx/navigation/NavController) functionality and tests to it
- Update test names with [DisplayName](https://junit.org/junit5/docs/5.0.2/api/org/junit/jupiter/api/DisplayName.html) annotation

## License
[License file](./LICENSE)