# Android Test Showcase

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Mock project showcasing testing on Android. I plan to use this as a [CodeKata](http://codekata.com/) to experiment with testing.
> Disclaimer: Every test you see in this project you should take with a pinch of salt, this is my self-taught experimentation of Testing on Android

## Project Overview
The project uses mock api and is really bare bone. It's used just to showcase example features I usually encounter on Android.

You can login with any credentials, mark favourite content items, log out and that's about it.

As dependency injection / Service Locator [koin](https://insert-koin.io/) is used.

The application is separated into different modules each module is tested differently.

<details>
<summary><span style="font-size: 1.32rem">Modules<span></summary>

- [model](#model)
- [app](#app)
- [core](#core)
- [network](#networking)
- [mockserver](#mock-server)
- [examplecase](#example-case)

### Model
Self explanatory, contains all shared data classes (POJOs) and Exceptions used by the other modules.
There is nothing to test here.

### App

The android module of the application, contains the UI (Activities), Room-Database and ViewModels.

The UI is strucured in [MVVM](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel).

Has dependency on the [core module](#core) and only on core module.

There are 3 kinds of tests in this module:
- junit5 tests for ViewModels and Dependency Injection
- Robolectric Test for Room Database and SharedPreferences
- Shared Tests for Screens (shared between Robolectric and AndroidTests)
- End to End Android Tests

#### Unit tests
Verify the ViewModels are reacting to the mocked UseCases properly.

[Kotlin-Mockito](https://github.com/mockito/mockito-kotlin) is used to mock out UseCases comming from the core module.

[Koin-Test](https://insert-koin.io/docs/quickstart/junit-test/) is used to verify the ServiceLocator modules.

[LiveData-Testing](https://github.com/jraska/livedata-testing) is used to verify the LiveData values of ViewModels.

#### Robolectric
Verifies the DataBase interactions and also verifies the interactions on each Screen.

Robolectric [website link](http://robolectric.org/androidx_test/).

In [Unit](#unit-tests) and Robolectric tests [coroutine-test](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/) is used to switch out the mainThread thus enabling fine control over interactions.

#### AndroidTest
Verifies the interactions with the Screens.

In [Robolectric](#robolectric) and AndroidTests [Espresso](https://developer.android.com/training/testing/espresso) is used to interact with UI.

In [Robolectric](#robolectric) and AndroidTests [OkhttpIdlingResources](https://github.com/JakeWharton/okhttp-idling-resource) is used to synchronize OkHttp with Espresso.

In [Robolectric](#robolectric) and AndroidTests [Mock Server](mock-server) module is used to mock the network requests.

In [Robolectric](#robolectric) and AndroidTests [InstantTaskExecutor](https://developer.android.com/reference/androidx/arch/core/executor/testing/package-summary) is used to set LiveData values immediately and a Custom implementation in [Unit](#unit-tests) tests.

### Core
Business layer of the application. Contains Repositories and UseCases.

Has dependency on the [network](#networking) module. Database/SharedPreferences LocalStorage classes are injected into the core module.

All tests are junit5 and they are using [Kotlin-Mockito](https://github.com/mockito/mockito-kotlin) to mock all dependencies.

[Coroutine-test](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/) is also used in every test.
[Turbine](https://github.com/cashapp/turbine) is used to test flows, but tests will be shown without turbine as well.

The tests are verifying the interactions between the RemoteSource and LocalSource components. It also verifies that ErrorHandling is done properly.

Has also Integration tests which verify multiple components working together, for this [Mock Server](mock-server) module is used to mock responses.

### Networking
As the name suggests this is the module which sends requests to the Backend and parses the responses received. All responses and requests are mapped to and from models from the [model](#model) data classes.

[Retrofit](https://square.github.io/retrofit/) + [OkHttp](https://square.github.io/okhttp/) + [Moshi](https://github.com/square/moshi) is used to send and parse requests.


All tests are Junit5. The Retrofit Services are injected into tests to make sure the parsing and other setups are proper.

The tests are verifying all the requests contain the correct arguments, headers, path, methods. It is also responsible to verify the responses are parsed properly.

[Mock Server](mock-server) module and [MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver)  is  used to respond to the requests sent by OkHttp.
[JsonAssert](https://github.com/skyscreamer/JSONassert) is used to compare JSON models.

### Mock Server
This module is not actually part of the APK. This module is only used to unify mocking of Network request between Instrumentation tests from [app](#app) module, [core](#core) integration tests and [network](#networking) module.

It contains a way to setup responses to requests in a unified way. Contains all Response.json and expected Request.json files.

[MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver) is used to respond to the requests sent by OkHttp.
[JsonAssert](https://github.com/skyscreamer/JSONassert) is used to compare JSON models.
[OkHttp-TLS](https://github.com/square/okhttp/tree/master/okhttp-tls) is used to have HTTPS requests on Android Tests.

### Example Case

This folder contains examples of specific cases such as NavController testing.

</details>

 ---

## Server
The actual server when running the application is [mockapi.io](https://www.mockapi.io/) so don't expect actual functionalities in the application.

## Code Kata

This section describes what you need to play with the exercises in the project.

<details>
<summary><span style="font-size: 1.32rem">Code Kata details</span></summary>

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
* Testing First: Open the [test first instruction set](./codekata/testfirst.instructionset.md) To see how to start writing your test first.

#### Robolectric and Android Tests.
Open the [shared tests instruction set](./codekata/sharedtests.instructionset.md).

In this section we will see how can we share Robolectric test source with AndroidTests to run our same tests on actual device.
We will also see how to write AndroidTest End to End Tests.

</details>

---

## Util classes

Additional modules have been added prefixed with test-util.

These contain all the reusable Test Util classes used in the showcase.

The Testing setup is extracted into a separate gradle script, which with some modifications, you should be able to easily add to your own project.

To use the TestUtil classes, you will need to add the GitHub Repository as a source for dependencies:

<details>
<summary> See details</summary>

```groovy
// top level build.gradle
allprojects {
    repositories {
        // ...
        maven {
            url "https://maven.pkg.github.com/fknives/AndroidTest-ShowCase"
            credentials {
                username = project.findProperty("GITHUB_USERNAME") ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("GITHUB_TOKEN") ?: System.getenv("GITHUB_TOKEN")
            }
            // https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token
        }
    }
}
// OR
// top level build.gradle.kts
allprojects {
    repositories {
        // ...
        maven {
            url = uri("https://maven.pkg.github.com/fknives/AndroidTest-ShowCase")
            credentials {
                username = extra.properties["GITHUB_USERNAME"] as String? ?: System.getenv("GITHUB_USERNAME")
                password = extra.properties["GITHUB_TOKEN"] as String? ?: System.getenv("GITHUB_TOKEN")
            }
            // https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token
        }
    }
}
```

*Latest version:*![Latest release](https://img.shields.io/github/v/release/fknives/AndroidTest-ShowCase)

and then you can use the following dependencies:
```groovy
testImplementation "org.fnives.android.testutil:android-unit-junit5:<latestVersion>"   // test-util-junit5-android
testImplementation "org.fnives.android.testutil:shared-robolectric:<latestVersion>"    // test-util-shared-robolectric
testImplementation "org.fnives.android.testutil:android:<latestVersion>"               // test-util-android
androidTestImplementation "org.fnives.android.testutil:android:<latestVersion>"        // test-util-android
androidTestImplementation "org.fnives.android.testutil:shared-android:<latestVersion>" // test-util-shared-android
```
</details>

---

## Code Coverage Report

For Code Coverage Reporting, Jacoco is used.

<details>
<summary>See details</summary>

For Code Coverage Reporting, Jacoco is setup in [jacoco.config.gradle](./gradlescripts/jacoco.config.gradle).

- Each sub module has it's own code coverage report, enabled by the gradle script.
- Additionally it contains gradle task for an aggregated code coverage report for the project as a whole.

Feel free to use that script and tweak it for your project and module setup.

The script is documented, to the best of my understanding, but specific to this project, not prepared for multiple buildFlavours or different buildTypes than debug.

### Sub module reports
To run tests and Jacoco report for a submodule, run task `jacocoTestReport`:
- for java it will run unit tests and creates a report
- for android it will run jacocoAndroidTestReport and jacocoUnitTestReport and create 2 separate reports.

> Note:
> - jacocoAndroidTestReport is alias to createDebugAndroidTestCoverageReport
> - jacocoUnitTestReport is alias to createDebugUnitTestCoverageReport

### Aggregated reports
To see an aggregated code coverage report:
- task `jacocoRootReport` will pull together all the submodules report and create a single one from them ($projectDir/build/coverage-report).
- task `runTestAndJacocoRootReport` will run all the sub modules reports and tests then run `jacocoRootReport`.

### Issues
- One issue, is that the androidTest reports don't work with the sharedTest module setup, this issue is reported [here](https://issuetracker.google.com/issues/250130118)
- Another issue, is that seems like the tests fail with Resource.NotFound on API 21 if `enableAndroidTestCoverage` is true, so I disabled that for CI.

By shared test module setup I mean a module like `app-shared-test`, which has a dependency graph of:
- app-shared-test -> app.main
- app.test -> app-shared-test

### Reference
Here are the two articles I used for the jacoco setup script: [jacoco-in-android](https://medium.com/swlh/multi-module-multi-flavored-test-coverage-with-jacoco-in-android-bc4fb4d135a3)
[aggregate-test-coverage](https://lkrnac.net/blog/2016/10/aggregate-test-coverage-report/).

</details>

---

## Screenshot Testing

Screenshot testing example is not present in the repository, but here are my findings regarding the topic.

<details>
<summary>See details</summary>

Screenshot testing can be valuable in a large project. The basic idea is that you have reference screenshots of screens / components which you check once manually. Based on these references you can verify that your changes did not modify other parts of the UI.

## Comparision
There are other people who already compared most of the libraries I found.
Here are the list of valuable resources that go into more detail than I am doing here.
- [article](an-introduction-to-snapshot-testing-on-android-in-2021) and [repo](https://github.com/sergio-sastre/Android-screenshot-testing-playground) Comparing multiple solutions.
- Compose native [sample](https://github.com/android/compose-samples/blob/e6994123804b976083fa937d3f5bf926da4facc5/Rally/app/src/androidTest/java/com/example/compose/rally/ScreenshotComparator.kt)
- Showkase library, that helps visualise your Compose previews. Can be integrated with screenshot testing as described [here](https://proandroiddev.com/automatic-screenshot-testing-for-all-your-compose-previews-6add202fecc7)
- [Cookbook](https://android-ui-testing.github.io/Cookbook/basics/screenshot_testing/)

## Libraries

So here is a list of libraries I found that can be used for this purpose.
### [screenshot-tests-for-android](https://github.com/facebook/screenshot-tests-for-android)
This library creates images from your views inflated in your Instrumented Test classes. Can be used with activities launched as well.

**Compose Support**: Inside activites, yes. As component no.

#### Learn More
- [documentation](https://facebook.github.io/screenshot-tests-for-android/#creating-a-screenshot)
- [sample](https://github.com/facebook/screenshot-tests-for-android/tree/main/sample)
- [article](https://www.runtastic.com/blog/en/screenshot-testing-for-android/)

---

### [Shot](https://github.com/pedrovgs/Shot) 

Build on top of Facebook's screenshot tests, has all it's capabilities, Additionally generates side by side comparision when verifying. 

**Compose Support**: Yes.

#### Learn More
- [article](https://medium.com/sampingan-tech/snapshot-testing-in-android-app-using-shot-library-1edbb3b8c76c)
- [sample](https://github.com/pedrovgs/Shot/tree/master/shot-consumer)

---

### [android-testify](https://github.com/ndtp/android-testify/) 
Extends ActivityTestRule and enables taking screenshots of your activities. Has extension for full screen, compose and accssibility! 

**Compose Support**: Yes.

#### Learn More
- [article](https://levelup.gitconnected.com/testing-ui-in-android-with-screenshot-testing-7cc633836aad)
- [sample](https://github.com/ndtp/android-testify/tree/main/Sample)

---

### [dropshots](https://github.com/dropbox/dropshots])

Takes screenshots on the device and compares them to asset bitmaps.

**Compose Support**: Yes inside activites.

#### Learn more
- [sample](https://github.com/dropbox/dropshots/tree/main/sample)

---

### [paparazzi](https://github.com/cashapp/paparazzi)

JVM Based solution, renders the screens without Device. It is as limited as Studio's Preview.

**Compose Support**: Yes.

#### Learn more
- [video](https://www.droidcon.com/2021/11/17/keeping-your-pixels-perfect-paparazzi-1-0-2/)
- [article](https://betterprogramming.pub/sanely-test-your-android-ui-libraries-with-paparazzi-b6d46c55f6b0)
- [sample](https://github.com/cashapp/paparazzi/tree/master/sample)

### [kotlin-snapshot-testing](https://github.com/QuickBirdEng/kotlin-snapshot-testing)

Inspired by [Swift snapshot testing library](https://github.com/pointfreeco/swift-snapshot-testing)

It is not specific for Screenshots, but serializable data. For our purpose it's mainly compose based, but extensible if required.

**Compose Support**: Yes.

#### Learn more
- [article](https://quickbirdstudios.com/blog/snapshot-testing-kotlin/)

----

### Writing your own 

There are also critics of existing libraries, if you have been burned, you might want to write your own. Here is an [article](https://proandroiddev.com/easy-ui-and-screenshot-testing-on-android-2b138f6d1eb8) to get you started

> Screenshots are also implemented in as somewhat custom way in this repository. Screenshots are taken when a test fail to see why it did. This can help identify failures like a System dialog showing so your Activity can't get focus.

</details>

---

## License
[License file](./LICENSE)
