## Login UI Test with Compose

This section is equivalent to the one with ["Login UI Test"](../robolectric.instructionset.md#login-ui-test) from robolectric.instructionset.md.
Make sure to read that one first as this one only focuses on the differences that Compose brings.
We will write the same tests from `AuthActivityInstrumentedTest` so that we see clearly the differences between the two.

### Robot Pattern

We will apply the same Robot Pattern, since the concept applies exactly the same. 
The only thing that changes is the implementation details of the Robot class.

Here is a list of actions we want to do:
- we want to be able to type in the username
- we want to be able to type in the password
- we want to be able the username or password is indeed shows on the UI
- we want to be able to click on signin
- we want to be able verify if we are loading or not
- we want to verify if an error is shown or not
- we want to check if we navigated to Main or not

##### So here is the code for our the UI interactions

```kotlin
class ComposeLoginRobot(
    composeTestRule: ComposeTestRule,
) : ComposeTestRule by composeTestRule {

    fun setUsername(username: String): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.UsernameInput).performTextInput(username)
    }

    fun setPassword(password: String): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.PasswordInput).performTextInput(password)
    }

    fun assertPassword(password: String): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.PasswordVisibilityToggle).performClick()
        onNodeWithTag(AuthScreenTag.PasswordInput).assertTextContains(password)
    }

    fun assertUsername(username: String): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.UsernameInput).assertTextContains(username)
    }

    fun clickOnLogin(): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.LoginButton).performClick()
    }

    fun assertLoading(): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.LoadingIndicator).assertIsDisplayed()
    }
    fun assertNotLoading(): ComposeLoginRobot = apply {
        onAllNodesWithTag(AuthScreenTag.LoadingIndicator).assertCountEquals(0)
    }

    fun assertErrorIsShown(stringId: Int): ComposeLoginRobot = apply {
        onNodeWithTag(AuthScreenTag.LoginError)
            .assertTextContains(ApplicationProvider.getApplicationContext<Context>().resources.getString(stringId))
    }
}
```

While in the View system we're using Espresso to interact with views, 
in Compose we need a reference to the `ComposeTestRule` that contains our UI, 
which we will pass as a constructor parameter to the robot.

To create a `ComposeTestRule` you simply need to: 

```kotlin
    @get:Rule
    val composeTestRule = createComposeRule()
```

> Note: You need to add a debug dependency for the rule: `debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")`

Since we don't have view ids in Compose we need to search composables by tags, using for example `onNodeWithTag` finder.
To add a tag to a composable use the `testTag` modifier in your UI, for example:

```kotlin
    Modifier.testTag(AuthScreenTag.UsernameInput)
```

Once we have a node we can take actions such as `performClick()` or check assertions such as `assertTextContains`. 
For a list of finder, actions and assertions see the docs: https://developer.android.com/jetpack/compose/testing#testing-apis

##### Next up, we need to verify if we navigated:

If the navigation is also in compose we don't have an intent to check if we navigated. 
So instead, we're simply searching for regular composables that represent our destinations.

This means that we could write a robot for our navigation which will simply check whether the root Composable for destination exists:

```kotlin
    fun assertHomeScreen(): ComposeNavigationRobot = apply {
        composeTestRule.onNodeWithTag(AppNavigationTag.HomeScreen).assertExists()
    }

    fun assertAuthScreen(): ComposeNavigationRobot = apply {
        composeTestRule.onNodeWithTag(AppNavigationTag.AuthScreen).assertExists()
    }
```

##### What about the Snackbar

Since everything in Compose is a composable, our Snackbar doesn't have anything special.  
Put a tag on it and use the same finders and assertions.

#### Test class setup

The setup is the mostly the same as for View so for the sake of simplicity let's focus on the differences.


##### Initializing the UI
We don't need an activity scenario. We will use instead `createComposeRule()` which will handle the host activity. 
If you need a specific activity, use `createAndroidComposeRule<YourActivity>()`.

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Before
fun setup() {
    composeTestRule.setContent {
        AppNavigation(isUserLogeInUseCase = IsUserLoggedInUseCase(FakeUserDataLocalStorage()))
    }
    // ...
}
```

In `setContent` we can have any composable no matter how "small" or "big", it could be a single button or the whole app.
Here we are setting AppNavigation as the content, since the tests will be integration tests which will check navigation events.

Notice that we are injecting a fake local storage to control the logged in state.

##### The Robot
For the robot we will use the compose implementation of it.

```kotlin
private lateinit var robot: ComposeLoginRobot
private lateinit var navigationRobot: ComposeNavigationRobot

@Before
fun setup() {
    // ...
    robot = ComposeLoginRobot(composeTestRule)
    navigationRobot = ComposeNavigationRobot(composeTestRule)
}
```

##### Networking and Coroutines

Network synchronization and mocking is the same as for View.

```kotlin
private val mockServerScenarioSetupTestRule = MockServerScenarioSetupResetingTestRule()
private val mockServerScenarioSetup get() = mockServerScenarioSetupTestRule.mockServerScenarioSetup
```

Coroutine setup is the same, except for `Dispatchers.setMain(dispatcher)`, which we don't need. 

```kotlin
private val dispatcherTestRule = DatabaseDispatcherTestRule()
```

Setting the rules:

```kotlin
@Rule
@JvmField
val ruleOrder: RuleChain = RuleChain.outerRule(mockServerScenarioSetupTestRule)
    .around(dispatcherTestRule)
```

### 1. `properLoginResultsInNavigationToHome`

With this setup our test should be pretty simple.

First we mock our request:
```kotlin
mockServerScenarioSetup.setScenario(
    AuthScenario.Success(password = "alma", username = "banan")
)
```

Then we wait a bit, more precisely we wait for the app to navigate us correctly to AuthScreen since we're not logged in:
```kotlin
composeTestRule.mainClock.advanceTimeBy(510L)
```

We assert that we are indeed on the correct screen
```kotlin
navigationRobot.assertAuthScreen()
```

We insert the credentials into the input field:
```kotlin
robot.setPassword("alma")
    .setUsername("banan")
    .assertUsername("banan")
    .assertPassword("alma")
```

Now thing are getting a little tricky. We want to click on login and assert that loading is displayed before navigating away.
The problem is that, by the time the robot will look for the loading indicator, the app would have already be at the home screen.
To slow things down we will disable clock autoAdvancing:

```kotlin
composeTestRule.mainClock.autoAdvance = false // Stop the clock
robot.clickOnLogin() // Click the button
composeTestRule.mainClock.advanceTimeByFrame() // Advance the clock by one frame
robot.assertLoading() // Assert the loading
composeTestRule.mainClock.autoAdvance = true // Let clock auto advance again
```

Lastly we check the navigation was correct, meaning we should be on the home screen:
```kotlin
composeTestRule.mainClock.awaitIdlingResources() // wait for login network call idling resource
navigationRobot.assertHomeScreen()
```

> `awaitIdlingResources` is an extension function to await all idling resources.
> Note: Considering what the docs say this shouldn't be necessarily if the idling resources are setup in Espresso, since the compose test rule is aware of espresso and it waits for idle before every finder. In practice it only works with the line above. Could be a bug somewhere.

### 2. `emptyPasswordShowsProperErrorMessage`

Next up we verify what happens if the user doesn't set their password. We don't need a request in this case.

First we check that we are in the write place:
```kotlin
composeTestRule.mainClock.advanceTimeBy(510L)
navigationRobot.assertAuthScreen()
```

Then we set the username:
```kotlin
robot.setUsername("banan")
    .assertUsername("banan")
    .clickOnLogin()
```

Finally we let coroutines go and verify the error is shown and we have not navigated:
```kotlin
composeTestRule.mainClock.awaitIdlingResources()
robot.assertErrorIsShown(R.string.password_is_invalid)
    .assertNotLoading()
navigationRobot.assertAuthScreen()
```

### 3. `emptyUserNameShowsProperErrorMessage`

This will be really similar as the previous test, so try to do it on your own. The error is `R.string.username_is_invalid`

Still, here is the complete code:
```kotlin
composeTestRule.mainClock.advanceTimeBy(510L)
navigationRobot.assertAuthScreen()

robot
    .setPassword("banan")
    .assertPassword("banan")
    .clickOnLogin()

composeTestRule.mainClock.awaitIdlingResources()
robot.assertErrorIsShown(R.string.username_is_invalid)
    .assertNotLoading()
navigationRobot.assertAuthScreen()
```

### 4. `invalidCredentialsGivenShowsProperErrorMessage`

Now we verify network errors. First let's setup the response:
```kotlin
mockServerScenarioSetup.setScenario(
   AuthScenario.InvalidCredentials(username = "alma", password = "banan")
)
```

Now input the credentials and fire the event:
```kotlin
composeTestRule.mainClock.advanceTimeBy(510L)
navigationRobot.assertAuthScreen()
robot.setUsername("alma")
    .setPassword("banan")
    .assertUsername("alma")
    .assertPassword("banan")

composeTestRule.mainClock.autoAdvance = false
robot.clickOnLogin()
composeTestRule.mainClock.advanceTimeByFrame()
robot.assertLoading()
composeTestRule.mainClock.autoAdvance = true
```

Now at the end verify the error is shown properly:
```kotlin
composeTestRule.mainClock.awaitIdlingResources()
robot.assertErrorIsShown(R.string.credentials_invalid)
    .assertNotLoading()
navigationRobot.assertAuthScreen()
```

### 5. `networkErrorShowsProperErrorMessage`

Finally we verify the `AuthScenario.GenericError`. This will be really similar as the previous, except the error will be `R.string.something_went_wrong`.
You should try to do this on your own.

Here is the code for verification:
```kotlin
mockServerScenarioSetup.setScenario(
    AuthScenario.GenericError(username = "alma", password = "banan")
)

composeTestRule.mainClock.advanceTimeBy(510L)
navigationRobot.assertAuthScreen()
robot.setUsername("alma")
    .setPassword("banan")
    .assertUsername("alma")
    .assertPassword("banan")

composeTestRule.mainClock.autoAdvance = false
robot.clickOnLogin()
composeTestRule.mainClock.advanceTimeByFrame()
robot.assertLoading()
composeTestRule.mainClock.autoAdvance = true

composeTestRule.mainClock.awaitIdlingResources()
robot.assertErrorIsShown(R.string.something_went_wrong)
    .assertNotLoading()
navigationRobot.assertAuthScreen()
```

### 6. `restoringContentShowPreviousCredentials`

Since we're writing apps for Android, we must handle state restoration so let's write a test for it.

For simulating the recreation of the UI, we first need a `StateRestorationTester`:
```kotlin
    private val stateRestorationTester = StateRestorationTester(composeTestRule)
```

Then in `setup()`, we need to `setContent` on `stateRestorationTester` instead of on `composeTestRule`.

Now for the actual test, we first setup the content then we trigger restoration by calling `stateRestorationTester.emulateSavedInstanceStateRestore()`, afterwards we can verify that the content is recreated in the correct way:

```kotlin
composeTestRule.mainClock.advanceTimeBy(510L)
navigationRobot.assertAuthScreen()
robot.setUsername("alma")
    .setPassword("banan")
    .assertUsername("alma")
    .assertPassword("banan")

stateRestorationTester.emulateSavedInstanceStateRestore()

navigationRobot.assertAuthScreen()
robot.assertUsername("alma")
    .assertPassword("banan")
```