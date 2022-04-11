# Android Test Showcase

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Mock project showcasing testing on Android. I plan to use this as a [CodeKata](http://codekata.com/) to experiment with testing.
> Disclaimer: Every test you see in this project you should take with a pinch of salt, this is my self-taught experimentation of Testing on Android

## Project Overview
The project uses mock api and is really bare bone. It's used just to showcase example features I usually encounter on Android.

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

Has dependency on the [core module](#core) and only on core module.

There are 3 kinds of tests in this module:
- junit5 tests for ViewModels and Dependency Injection
- Robolectric Test for Room Database and SharedPreferences
- Shared Tests for Screens (shared between Robolectric and AndroidTests)
- End to End Android Tests

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
[Turbine](https://github.com/cashapp/turbine) is used to test flows, but tests will be shown without turbine as well.

The tests are verifying the interactions between the RemoteSource and LocalSource components. It also verifies that ErrorHandling is done properly.

Has also Integration tests which verify multiple components working together, for this [Mock Server](mock-server) module is used to mock responses.

#### Networking
As the name suggests this is the module which sends requests to the Backend and parses the responses received. All responses and requests are mapped to and from models from the [model](#model) data classes.

[Retrofit](https://square.github.io/retrofit/) + [OkHttp](https://square.github.io/okhttp/) + [Moshi](https://github.com/square/moshi) is used to send and parse requests.


All tests are Junit5. The Retrofit Services are injected into tests to make sure the parsing and other setups are proper.

The tests are verifying all the requests contain the correct arguments, headers, path, methods. It is also responsible to verify the responses are parsed properly.

[Mock Server](mock-server) module and [MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver)  is  used to respond to the requests sent by OkHttp.
[JsonAssert](https://github.com/skyscreamer/JSONassert) is used to compare JSON models.

#### Mock Server
This module is not actually part of the APK. This module is only used to unify mocking of Network request between Instrumentation tests from [app](#app) module, [core](#core) integration tests and [network](#networking) module.

It contains a way to setup responses to requests in a unified way. Contains all Response.json and expected Request.json files.

[MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver) is used to respond to the requests sent by OkHttp.
[JsonAssert](https://github.com/skyscreamer/JSONassert) is used to compare JSON models.
[OkHttp-TLS](https://github.com/square/okhttp/tree/master/okhttp-tls) is used to have HTTPS requests on Android Tests.

## Server
The actual server when running the application is [mockapi.io](https://www.mockapi.io/) so don't expect actual functionalities in the application.

## Code Kata

### Preparation
Download the project, open it in [Android Studio](https://developer.android.com/studio?gclid=Cj0KCQjw1PSDBhDbARIsAPeTqrfKrSx8qD_B9FegOmpVgxtPWFHhBHeqnml8n4ak-I5wPvqlwGdwrUQaAtobEALw_wcB&gclsrc=aw.ds).

* In the gradle window you can see in the root gradle there is a "tests" group. In this group you will see a jvmTests, robolectricTests and androidTests task.
* First run the jvmTests.
* When that finished, build the application to your phone.
* Login with whatever credentials and look over the app, what will you test.
* When finished, run androidTests.

This will ensure the testing setup is proper, the project can resolve all the dependencies and such issues won't come up during your exercise.

### Structure

The Code Kata is structured into 6 different section, each section in different what we are testing and how we are testing it.

Since our layering is "app", "core" and "networking", of course we will jump right into the middle and start with core.

#### Core
Open the [core instruction set](./codekata/core.instructionset.md).

The core tests are the simplest, we will look into how to use mockito to mock class dependencies and write our first simple tests.

We will also see how to test flows.

#### Networking
Open the [networking instruction set](./codekata/networking.instructionset.md).

The networking instruction set will show you how to test network request with mockwebserver.

It will also show you that you can write tests not only for one class mocking all the dependencies, but a component.

#### App ViewModel Unit Tests
Open the [app viewModel unit tests instruction set](./codekata/viewmodel.instructionset.md).

This section we will see how to replace the dispatcher to testDispatcher to control the ViewModel's coroutines.

We will also see how to test with LiveData.

We will introduce Rules, aka easy to reuse "Before" and "After" components.

#### Core Again (Integration)
Open the [core again instruction set](./codekata/core.again.instructionset.md).

We complicate things here. We write our first Integraiton Test.
We will verify the Authentication classes and the networking module is working together like a charm.

#### App Robolectric Unit Tests.
Open the [app robolectric unit tests instruction set](./codekata/robolectric.instructionset.md).

In this section we will see how to test component depending on context such as Room database.
In this tests we will also see how to interact with View components in tests via Espresso.
We will also see how to test a specific Activity (same concept can be applied to fragments)

Bonus:
* Testing Compose UI: Open the [compose instruction set](./codekata/compose.instructionset.md)

#### Robolectric and Android Tests.
Open the [shared tests instruction set](./codekata/sharedtests.instructionset.md).

In this section we will see how can we share Robolectric test source with AndroidTests to run our same tests on actual device.
We will also see how to write AndroidTest End to End Tests.

## License
[License file](./LICENSE)
