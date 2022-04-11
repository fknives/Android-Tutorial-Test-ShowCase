package org.fnives.test.showcase.endtoend

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.fnives.test.showcase.R
import org.fnives.test.showcase.network.testutil.NetworkTestConfigurationHelper
import org.fnives.test.showcase.storage.database.DatabaseInitialization
import org.fnives.test.showcase.testutils.idling.CompositeDisposable
import org.fnives.test.showcase.testutils.idling.Disposable
import org.fnives.test.showcase.testutils.idling.IdlingResourceDisposable
import org.fnives.test.showcase.testutils.idling.OkHttp3IdlingResource
import org.fnives.test.showcase.testutils.idling.loopMainThreadFor
import org.fnives.test.showcase.ui.splash.SplashActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
@Ignore("Example for Test Recording")
class LoginLogoutEndToEndTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(SplashActivity::class.java)

    private var disposable: Disposable? = null

    @Before
    fun before() {
        /** Needed to add the dispatcher to the Database */
        DatabaseInitialization.dispatcher = UnconfinedTestDispatcher()

        /** Needed to register the Okhttp as Idling resource, so Espresso actually waits for the response.*/
        val idlingResources = NetworkTestConfigurationHelper.getOkHttpClients()
            .associateBy(keySelector = { it.toString() })
            .map { (key, client) -> OkHttp3IdlingResource.create(key, client) }
            .map(::IdlingResourceDisposable)
        disposable = CompositeDisposable(idlingResources)
    }

    @After
    fun after() {
        disposable?.dispose()
    }

    @Test
    fun loginLogoutEndToEndTest() {
        /** Needed to add looping here so the splash finishes */
        loopMainThreadFor(600L)

        val textInputEditText = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.user_edit_text),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.user_input),
                        0
                    ),
                    0
                ),
                ViewMatchers.isDisplayed()
            )
        )
        textInputEditText.perform(ViewActions.replaceText("alma"), ViewActions.closeSoftKeyboard())

        val textInputEditText2 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.password_edit_text),
                /** this was too specific and didn't find the element, probably cause the keyboard isn't closed just yet. */
//                childAtPosition(
//                    childAtPosition(
//                        withId(R.id.password_input),
//                        0
//                    ),
//                    0
//                ),
//                isDisplayed()
            )
        )
        textInputEditText2.perform(ViewActions.replaceText("banan"), ViewActions.closeSoftKeyboard())

        val materialButton = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.login_cta), ViewMatchers.withText("Login"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(android.R.id.content),
                        0
                    ),
                    5
                ),
                ViewMatchers.isDisplayed()
            )
        )
        materialButton.perform(ViewActions.click())

        val textView = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Content"),
                ViewMatchers.withParent(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.toolbar),
                        ViewMatchers.withParent(IsInstanceOf.instanceOf(ViewGroup::class.java))
                    )
                ),
                ViewMatchers.isDisplayed()
            )
        )
        textView.check(ViewAssertions.matches(ViewMatchers.withText("Content")))

        val actionMenuItemView = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.logout_cta), ViewMatchers.withContentDescription("Logout"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.toolbar),
                        1
                    ),
                    0
                ),
                ViewMatchers.isDisplayed()
            )
        )
        actionMenuItemView.perform(ViewActions.click())

        val textView2 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Mock Login"),
                ViewMatchers.withParent(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.toolbar),
                        ViewMatchers.withParent(IsInstanceOf.instanceOf(ViewGroup::class.java))
                    )
                ),
                ViewMatchers.isDisplayed()
            )
        )
        textView2.check(ViewAssertions.matches(ViewMatchers.withText("Mock Login")))

        val button = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.login_cta), ViewMatchers.withText("LOGIN"),
                ViewMatchers.withParent(ViewMatchers.withParent(ViewMatchers.withId(android.R.id.content))),
                ViewMatchers.isDisplayed()
            )
        )
        button.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    private fun childAtPosition(parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent) && view == parent.getChildAt(position)
            }
        }
    }
}
