# 6. Starting of Shared tests and Android Tests

This is our last instruction set and it's all about testing on Real Android devices and on Emulators.
It's important for this section to finish the Robolectric testing, because we will continue from that.

In this testing instruction set you will learn how to write simple tests running on real devices and run the same tests via Robolectric.

- We will learn how to share classes between testing and AndroidTesting
- Learn the differences between Robolectric and AndroidTests
- Learn how to create End-To-End tests via Espresso Test Recorder
- Our tests classes will be really similar to Robolectric since we are using the same components
- We will use RuleChains to order our Test Rules.

## Login UI Test
Instead of writing new tests from scratch, we will modify our existing Robolectric tests so they can be run on a Real Android device as well.N
For this we already have a `sharedTest` package.

Our classes will be `CodeKataAuthActivitySharedTest` and `CodeKataSharedRobotTest`.

### Setup

#### Phone setup
First let's set up our phone.
With testing on phone it's important that animations are disabled from the `Developer options`, namely:
`Window animation scale` Animation Off
`Transition animation scale` Animation Off
`Animator animation scale` Animation Off

This is needed for two fold, for one your tests will run faster, two. Espresso could have timing issues with animations.

#### Test setup
Let's open `org.fnives.test.showcase.ui.login.codekata.CodeKataAuthActivitySharedTest`.

We can see it's identical as our original `org.fnives.test.showcase.ui.codekata.CodeKataAuthActivityInstrumentedTest`.
So let's copy our existing code from the Robolectric test here. For that we can use the body of `org.fnives.test.showcase.ui.RobolectricAuthActivityInstrumentedTest`.

You immediately notice that there are no import issues. That's because sharedTest package is added to the test sources. You may check out the `app/build.gradle` to see how that's done.
However we need to modify our robot:
```kotlin
// Instead of this:
private lateinit var robot: RobolectricLoginRobot
// Write this:
private lateinit var robot: CodeKataSharedRobotTest

// And of course modify it's initialization:
// Instead of this:
robot = RobolectricLoginRobot()
// Write this:
robot = CodeKataSharedRobotTest()
```

For our starting point, this is all the setup we need. What we now will do is modify this piece of class, so it not only runs via Robolectric, but it can run on Real Devices as well.

### 1. Threads

So to discover the differences, let's handle them one by one, by Running our Test.
In shared tests, at least for me, it defaults to Android Test when running the class. So make sure your device is connected, and run the `invalidCredentialsGivenShowsProperErrorMessage` Test. It should start on your device and shall crash.
You will see something similar:
```kotlin
java.lang.IllegalStateException: Cannot invoke setValue on a background thread
        at androidx.lifecycle.LiveData.assertMainThread(LiveData.java:487)
        ...
```

So that brings us to the first difference: *while Robolectric uses the same thread running the tests as running the Main thread, in Android Tests these threads are different.*

So the issue is with this line: `testDispatcher.advanceUntilIdleWithIdlingResources()`. Since we are in the InstrumentedTest's thread, all our coroutines will run there as well, which doesn't play well with LiveData.
One idea would be to use LiveData `ArchTaskExecutor.getInstance()` and ensure our LiveData doesn't care about the Thread they are set from, **but** then we would touch our Views from Non-Main Thread, which is still an issue.
**So Instead** What we need to do is run our coroutines on the actual mainThread. We have a handy `runOnUIAwaitOnCurrent` function for that, so let's use it in our `invalidCredentialsGivenShowsProperErrorMessage` test, wrap around our dispatcher call.

The full function now will look like this:
```kotlin
/** GIVEN password and username and invalid credentials response WHEN signIn THEN error invalid credentials is shown */
@Test
fun invalidCredentialsGivenShowsProperErrorMessage() {
    mockServerScenarioSetup.setScenario(
        AuthScenario.InvalidCredentials(username = "alma", password = "banan"),
        validateArguments = true
    )
    robot
        .setUsername("alma")
        .setPassword("banan")
        .assertUsername("alma")
        .assertPassword("banan")
        .clickOnLogin()
        .assertLoadingBeforeRequests()
        .assertErrorIsNotShown()

    runOnUIAwaitOnCurrent { testDispatcher.advanceUntilIdleWithIdlingResources() }
    robot.assertErrorIsShown(R.string.credentials_invalid)
        .assertNotNavigatedToHome()
        .assertNotLoading()
}
```

Now if we run our `invalidCredentialsGivenShowsProperErrorMessage` it succeeds.

So let's wrap all of our `testDispatcher.advanceUntilIdleWithIdlingResources` calls into `runOnUIAwaitOnCurrent`

### 2. Application class

Let's run our full test class now by clicking next to the test class's run icon.
When you run your tests, you will see the first one succeeds as expected, but all the others fail with something along the lines of
```kotlin
kotlin.UninitializedPropertyAccessException: lateinit property mockServerScenarioSetup has not been initialized
at org.fnives.test.showcase.ui.login.codekata.CodeKataAuthActivitySharedTest.tearDown(CodeKataAuthActivitySharedTest.kt:68)
...
```

Now that's a weird one, it points to our tearDown. So our test crashes in the `tearDown`, because the `mockServerScenarioSetup` is not initialized?
When you see similar crashes, that suggest you had an exception in your `setup` and it didn't finish, so the `tearDown` also fails, because not all the elements are initialized.

If you select any other than the first that failed, and look for a root cause in the logs, you will see an issue along the lines of:
```kotlin
03-21 16:23:58.254 11370 11414 E TestRunner: java.lang.IllegalStateException: #init was called twice in a row. Make sure to call #release after every #init
03-21 16:23:58.254 11370 11414 E TestRunner: 	at androidx.test.espresso.intent.Checks.checkState(Checks.java:70)
```

That's still not the real issue however. This just means one of your tests before called `Intents.init()`, but didn't call `Intents.release()`.

So that's because something went wrong in our first test. I am describing these steps so you are more prepared in the future if you have similar issues.

**So now for the real cause**, checking the first test and scrolling up it's logs, we see a different error:
```kotlin
03-21 16:23:58.248 11370 11414 E TestRunner: java.lang.IllegalStateException: KoinApplication has not been started
03-21 16:23:58.248 11370 11414 E TestRunner: 	at org.koin.core.context.GlobalContext.get(GlobalContext.kt:36)
...
```

Now, here is a new difference between Robolectric and AndroidTest. In Robolectric, before every test, the Application class is initialized, however in AndroidTests, the Application class is only initialized once.
This is great if you want to have End-to-End tests that follow each other, but since now we only want to test some small subsection of the functionality, we have to restart Koin before every tests if it isn't yet started, so our tests don't use the same instances.
We will check if koin is initialized, if it isn't then we simply initialize it:
```kotlin
...
Intents.init()
if (GlobalContext.getOrNull() == null) {
    val application = ApplicationProvider.getApplicationContext<TestShowcaseApplication>()
    val baseUrl = BaseUrl(BuildConfig.BASE_URL)
    startKoin {
        androidContext(application)
        modules(createAppModules(baseUrl))
    }
} // needs to be before the Database overwriting
val dispatcher = StandardTestDispatcher()
...
```

With that now if you run the test class, all tests should succeed.

### 3. Animations

One difference which may or may not happened on your phone is with loading indicators and animations. It happened on some of my devices and not on others (it can be different between Android API levels as well).
If it happens to yours, the tests won't succeed they just hang. This happens because animations can add continuous work to the MainThread thus never letting it become idle.
The solution for this, to replace your Progress Bar or other infinitely animating element, with a simple view.
Some reference to this from stackoverflow [here](https://stackoverflow.com/questions/30469240/java-lang-runtimeexception-could-not-launch-intent-for-ui-with-indeterminate) and [here](https://stackoverflow.com/questions/35186902/testing-progress-bar-on-android-with-espresso).

What I usually do is something like this in my Robot:
```kotlin
/**
* Needed because Espresso idling waits until mainThread is idle.
*
* However, ProgressBar keeps the main thread active since it's animating.
*
* Another solution is described here: https://proandroiddev.com/progressbar-animations-with-espresso-57f826102187
* In short they replace the inflater to remove animations, by using custom test runner.
*/
fun replaceProgressBar() = apply {
      onView(withId(R.id.loading_indicator)).perform(ReplaceProgressBarDrawableToStatic())
}
```

And use this in my Robot, just before some Animating View is being shown.

### 4. ~~Extensions~~ Rules

Now our setup was already tedious in Robolectric Tests, it became even more tedious with sharedTests, it's time we take care of that.
We have used previously Extensions in `viewModel` and later `core.again` tests. Extensions are the JUnit5 version of JUnit4's Rules.
So since with Robolectric and AndroidTests we are in the world of JUnit4, we will have to write Rules instead.

#### 1. Intent init rule.
As we saw some of our test's setup failure also failed our other tests because of the initialization of the Intents, it would be nice if we wouldn't have to worry about that anymore.
So let's create a Rule for that.

Open `org.fnives.test.showcase.ui.login.codekata.rule.intent.CodeKataIntentInitRule`

You will see a basic TestRule. We get a Statement which you can think of like a Runnable. And we need to return another Statement.
Since from the function signature it's pretty clear what should we do, here is our implementation:
```kotlin
override fun apply(base: Statement, description: Description): Statement =
    object : Statement() {
        override fun evaluate() {
            try {
                Intents.init()
                base.evaluate()
            } finally {
                Intents.release()
            }
        }
    }
```
Pretty simple, we simply wrap the given Statement into our own, and in evaluation first we `init` the `Intents` and at the end we make sure it's `released`.

> Note: TestRule documentation contains a couple of other Base classes extending TestRule. Usually it's better to use one of them which matches your needs, that's because it might take care of additional things you wouldn't expect otherwise. Here we could have used ExternalResource, but I wanted the Intents.init() in the try as well.

##### Applying the Rule

Now let's go back to our Test class `org.fnives.test.showcase.ui.login.codekata.CodeKataAuthActivitySharedTest`

We can use Test rules either as BeforeClass/AfterClass, or Before/After. This basically means they can completely be equivalent to these methods.
We have our Intents.init() in @Before at the moment so we will use `@org..junit.Rule` annotation.
By intuition you would probably would write something like this:
```kotlin
@Rule
val intentRule = CodeKataIntentInitRule()
```
However, this is incorrect! The Rule annotation has to be on a Field, not property!, or on a function.
So your alternatives are:
```kotlin
@get:Rule // get is a function
val intentRule get() = CodeKataIntentInitRule()

@Rule
fun intentRule()  = CodeKataIntentInitRule()

@Rule
@JvmField
val intentRule = IntentInitRule()
```

**Now, you need to Remove the `Intents.init()` and `Intents.release()` from your `setup` and `tearDown`**

##### 2. Dispatcher TestRule

So we have that Dispatcher setup, which we probably would be using in other tests as well.
Let's create a TestRule for that setup next.
```kotlin
// before
val dispatcher = StandardTestDispatcher()
Dispatchers.setMain(dispatcher)
testDispatcher = dispatcher
TestDatabaseInitialization.overwriteDatabaseInitialization(dispatcher)

// after
Dispatchers.resetMain()
```

Open `org.fnives.test.showcase.ui.login.codekata.rule.dispatcher.CodeKataMainDispatcherRule`

The statement is already created so we just have to copy the lines from the `before` and `after function`.

```kotlin
override fun apply(base: Statement, description: Description): Statement = object : Statement() {
    override fun evaluate() {
        try {
            val dispatcher = StandardTestDispatcher()
            Dispatchers.setMain(dispatcher)
            TestDatabaseInitialization.overwriteDatabaseInitialization(dispatcher)
            _testDispatcher = dispatcher
            base.evaluate()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
```

Okay but we don't have the testDispatcher in this class. So let's create one and our actual test will just access that.
```kotlin
private var _testDispatcher: TestDispatcher? = null
val testDispatcher
    get() = _testDispatcher
        ?: throw IllegalStateException("TestDispatcher is accessed before it is initialized!")
```
We create the modifiable private field and make it accessible publicly to our tests. However if we access before a test is running, then we throw to let the user know.
One addition is that it's probably better to also clear that dispatcher at the end, so:
```kotlin
} finally {
    _testDispatcher = null
    Dispatchers.resetMain()
}
```

##### Make it more reusable
What if we were to use `UnconfinedTestDispatcher` Test Dispatcher instead. We wouldn't want to create a completely separate TestRule. So let's add a constructor parameter:
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class CodeKataMainDispatcherRule(private val useStandard: Boolean = true) : TestRule
```

And create Dispatcher by that flag:
```kotlin
val dispatcher = if (useStandard) StandardTestDispatcher() else UnconfinedTestDispatcher()
```

##### Apply the Rule.
Just like before we can add our Rule as before. One note, we have to overwrite the TestDispatcher in our Test class, so it will be like this:

```kotlin
@Rule
@JvmField
val mainDispatcherRule = CodeKataMainDispatcherRule()
private val testDispatcher: TestDispatcher get() = mainDispatcherRule.testDispatcher
```
>Notice we are using get function! so we don't access the dispatcher before our tests are run!

Now all we need to do is remove our previous dispatcher setups.

##### MockNetwork and Koin reset Rule

For setting up the MockServer and reseting the Koin, there is already a rule prepared for us: `org.fnives.test.showcase.testutils.MockServerScenarioSetupResetingTestRule`.

We will use this also to decluter our `setup` and `tearDown` function.

First let's remove what's no longer necessary:
```kotlin
@Before
fun setup() {
    robot = CodeKataSharedRobotTest()
    activityScenario = ActivityScenario.launch(AuthActivity::class.java)
    activityScenario.moveToState(Lifecycle.State.RESUMED)
}

@After
fun tearDown() {
    activityScenario.safeClose()
}
```

And apply our rule:
```kotlin
@Rule
@JvmField
val mockServerAndKoinRule = MockServerScenarioSetupResetingTestRule()
private val mockServerScenarioSetup: MockServerScenarioSetup get() = mockServerAndKoinRule.mockServerScenarioSetup
```

##### Rule order

What order our Rules are applied? Well, frankly I have no idea, that's because we can be explicit about our Rule's order, via RuleChain, so I always use that instead.
> Note our rule order is important, because Database and MockServer should happen after Koin reset.

I recommend doing this, so it's easier to read your test classes and being explicit what happens in what order.

So how would that look like:
```kotlin
private lateinit var activityScenario: ActivityScenario<AuthActivity>
private lateinit var robot: CodeKataSharedRobotTest

// rules
private val intentRule = CodeKataIntentInitRule()
private val mainDispatcherRule = CodeKataMainDispatcherRule()
private val testDispatcher: TestDispatcher get() = mainDispatcherRule.testDispatcher
private val mockServerAndKoinRule = MockServerScenarioSetupResetingTestRule()
private val mockServerScenarioSetup: MockServerScenarioSetup get() = mockServerAndKoinRule.mockServerScenarioSetup

@Rule
@JvmField
val ruleOrder: RuleChain = RuleChain.outerRule(intentRule)
    .around(mockServerAndKoinRule)
    .around(mainDispatcherRule)
```

> Notice: we removed the Rule annotations from the others, and only have one Rule, the RuleChain.

The Rule chain starts our `intentRule` first, then the `mockServerAndKoinRule` and `mainDispatcherRule`, when cleaning up, first `mainDispatcherRule` will clean up, then `mockServerAndKoinRule` and lastly `intentRule`. That's because of the Statements, since one statement calls the other's evaluation. So the `IntentRule` received now the `mockServerAndKoinRule`'s statement, and the `mockServerAndKoinRule` received the `mainDispatcherRule`'s statement and they call evaluate on it.

*TLDR: The rules are applied in the order in which they are added to the RuleChain*

##### Conclusions
That's all for Rules, if you wish you can write rules for the other setups as well.

#### 5. End-to-End tests

With all the weaponry you have been armed, you should be able to write End-to-End tests. You start your activity and do your tests without any mocking, or minmal mocking.
So we won't go into that in more detail, instead we will show an alternative to that.

##### Test records
An Android developer writing with more detail, is [here](https://developer.android.com/studio/test/other-testing-tools/espresso-test-recorder).

So basically you can use this Test Recorder tool to create Espresso tests.
You might be thinking, then why did we go through how to do these stuff manually?! Well, there are a couple of reasons:
- the generated tests, at least for me, still use deprecated ActivityTestRule instead of ActivityScenario
- the generated tests, might still have issues in them, like synchronisation with Okhttp and such.
- Some actions might be too specific, and you have to manually adjust the espresso test for it to work.

All in all, it is a good tool to get started on your test, but you probably still need to do manual modifications on it. So personally I would suggest them for bigger tests, which would take too much time manually, and then do the adjustments while running the tests.

> **Note: If you try it now, it will state that it is not compatible with Compose Projects.**
> So first switch to branch `TEST-HERE-Record-Espresso-Test` or commit `694d1bf0e71e40e80de849a3a6bb5e8a3430e348`.
> This is the last commit that still had no compose in it.

Okay, but how do we do it? As it's written on the site, we select `Run > Record Espresso Test`.
This will start the application on your device and you can do interactions with it. Such as writing into a text input.

Then in the Studio you may add assertions.
Assertions are espresso assertions, so you click on `Select an element from screenshot` and select what you want to check. It will highlight the element selected on the screenshot.
Then it will automatically suggest some assertion, like what it's text.

And that's all to it.

You can check out `org.fnives.test.showcase.endtoend.LoginLogoutEndToEndTest` which was created with TestRecording and the modifications that needed to be added.
> Note, the TestRule still should be switched out.

So End-to-End tests you basically write the same as in Robolectric and SharedTests, only that the Tests shouldn't use mocks or at least trying to minimize their usage, while in integration tests you may use Fakes more frequently.
One Additional benefit of AndroidTests over Robolectric is that your tests can interact with the Full Feature set of the device, with Notifications, Broadcast, Sensors etc.
I personally don't have too much experience with that part, so you will have to look up each of those when you want to write test for them.

##### Test Suits
Your End-to-End tests or even your Instrumentation tests can be bundled into a TestSuit. This is usually used so some tests run together, one after another.
An example I could think of is a Login Test at start, some Flow Tests and at the end a Logout Tests.
It should look something like this
```kotlin
@RunWith(Suite::class)
@Suite.SuiteClasses(
    LoginLogoutEndToEndTest::class,
    //...
)
class MyTestSuit
```

You can find it, at: `org.fnives.test.showcase.endtoend.MyTestSuit`

##### Test function orders
In End-to-End tests, it's possible you are testing file, database, sharedPreferences or other modifications as well.
This could make your tests dependent one on another. This can be good if you are verifying some specific scenario and trying to break it into smaller chunks.

I wouldn't recommend it in any other test type however.

If it is needed or mathing your case better, you can use the following annotation:
`@FixMethodOrder(MethodSorters.NAME_ASCENDING)`
And name your tests with an alphabetic order, like starting with numbers or something similar.

### 6. Some notes on other differences you may face between Robolectric and AndroidTests
#### 1. Hilt
Since currently only Koin is available in this repo, for updates follow this [issue](https://github.com/fknives/AndroidTest-ShowCase/issues/41), I thought to mentioned some issues with Hilt you may face:

##### Hilt requires a `HiltTestApplication` or something similar to test with.
You can replace the test application by creating a `AndroidJUnitRunner` and return your Custom Application class.
Then modify your build.gradle to reference that CustomAndroidJUnitRunner with full package.
Example:
```kotlin
class HiltAndroidJUnitRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, CustomHiltTestApplication::class.java.name, context)
    }
}
```
##### Hilt doesn't work directly with FragmentScenarios
For this the easiest is to use ActivityScenario instead with an EmptyActivity just containing the Fragment.

#### 2. Mockserver setup
In hilt you may use your MockWebServer setup in modules, this can be an issue if it's started from the MainThread.
It can throw a `NetworkOnMainThreadException` exception. [reference](https://github.com/square/okhttp/issues/3184)
To resolve this fast, a possible way is like this:
```runBlocking { withContext(Dispatcher.IO) { mockwebserver.url("/") } }```

#### 3. Unnecessary initializations in Application class
Another issue can be that Crashlytics or similar services is enabled in your tests. This can be resolved by the same principle as the HiltTestApplication issue, aka custom `AndroidJunitRunner`. Your custom TestClass will initialize only what it needs to.

#### 4. Dialogs
Dialogs cannot be tested properly via Robolectric without usage of Shadows, but they can be on Real Device. So what I usually do is setup a function which does one thing in one sourceset while does something else in another. You can see such example like `SpecificTestConfigurationsFactory`. To ease the usage I usually put a function in the sharedTest which uses the object `SpecificTestConfigurationsFactory`.

#### 5. Resource Access
Accessing test Resource files can also be an issue, you might not able to access your same test/res folder in AndroidTests. A way to do this is to declare the same folder as androidTest/assets in build gradle and similar to dialogs, create a function which uses Assets in Android Tests and uses Resources in Robolectric tests.

### Conclusion
Instrumentation and End-to-End tests are finally really similar, thanks to ~~Project Nitrogen~~ *Unified Test Platform*.
The only real difference is that our tests are larger, maybe touching multiple screens and we don't use any mocks.

**Some personal thoughts**
With all this described you should be able to start experimenting with testing.
Personally I would suggest to start with bug reports. When you get a simple bug ticket, write a test first which will fail if the bug is still present, then fix the bug.
This both helps the project ensuring that behaviour will never happen again, and you are able to experiment with Testing.
When you are more confident, you may start writing your features also together with small tests. These to me were started, when I had to work with timezones, dates or do similar calculations. These are great point to write tests, since it's a lot easier to see that your code works through examples then figuring out the whole thing at once.
Instrumentation and UI Tests I would suggest only on features that are not likely to change a lot, since these are a bit more expensive to maintain, but these tests also ensure when you do changes you are not contradicting some previous requirements.

Happy coding!