package org.fnives.test.showcase.rule

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.fnives.test.showcase.android.testutil.activity.SafeCloseActivityRule
import org.fnives.test.showcase.android.testutil.screenshot.ScreenshotRule
import org.fnives.test.showcase.android.testutil.synchronization.MainDispatcherTestRule
import org.fnives.test.showcase.ui.splash.SplashActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.koin.test.KoinTest

@Suppress("TestFunctionName")
@RunWith(AndroidJUnit4::class)
class ScreenshotTest : KoinTest {

    private lateinit var activityScenario: ActivityScenario<SplashActivity>

    private val mainDispatcherTestRule = MainDispatcherTestRule()

    @Rule
    @JvmField
    val ruleOrder: RuleChain = RuleChain.outerRule(mainDispatcherTestRule)
        .around(SafeCloseActivityRule { activityScenario })
        .around(ScreenshotRule(prefix = "screenshot-rule", takeOnSuccess = true))

    /** GIVEN loggedInState WHEN opened after some time THEN MainActivity is started */
    @Test
    fun screenshot() {
        activityScenario = ActivityScenario.launch(SplashActivity::class.java)
        activityScenario.moveToState(Lifecycle.State.RESUMED)
    }
}
