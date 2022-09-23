package org.fnives.test.showcase.examplecase.navcontroller

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.fnives.test.showcase.android.testutil.viewaction.recycler.RemoveItemAnimations
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Shows basic navigation test from a ListScreen to a Detail Screen.
 *
 * Sets up TestNavController and shows how it can be used to verify proper arguments, destination.
 *
 * For more info check out https://developer.android.com/guide/navigation/navigation-testing
 */
@RunWith(AndroidJUnit4::class)
class HomeNavigationTest {

    private lateinit var fragmentScenario: FragmentScenario<HomeFragment>
    private lateinit var testNavController: TestNavHostController

    @Before
    fun setup() {
        testNavController = TestNavHostController(ApplicationProvider.getApplicationContext())
        fragmentScenario = launchFragmentInContainer()
        fragmentScenario.runOnMain { testNavController.setGraph(R.navigation.nav_example) }

        fragmentScenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), testNavController)
        }

        Espresso.onView(ViewMatchers.withId(R.id.recycler))
            .perform(RemoveItemAnimations())
    }

    @Test
    fun clickingOnItemNavigatesProperlyAndBackstackIsCorrect() {
        val position = 25
        Espresso.onView(ViewMatchers.withId(R.id.recycler))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Item $position"), ViewMatchers.withId(R.id.item_cta),
                ViewMatchers.withParent(ViewMatchers.withId(R.id.recycler))
            )
        )
            .perform(ViewActions.click())

        Assert.assertEquals(R.id.detailFragment, testNavController.currentDestination?.id)
    }

    @Test
    fun clickingOnItemTwiceNavigatesProperly() {
        val position = 16
        Espresso.onView(ViewMatchers.withId(R.id.recycler))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Item $position"), ViewMatchers.withId(R.id.item_cta),
                ViewMatchers.withParent(ViewMatchers.withId(R.id.recycler))
            )
        )
            .perform(ViewActions.click())
            .perform(ViewActions.click())

        Assert.assertEquals(R.id.detailFragment, testNavController.currentDestination?.id)
        Assert.assertEquals(listOf(R.id.nav_example_xml, R.id.homeFragment, R.id.detailFragment), testNavController.backStack.map { it.destination.id })
        testNavController.backStack.map { it.arguments }
    }

    @Test
    fun clickingOnTwoItemsOpensOnlyTheFirst() {
        val position1 = 16
        val position2 = 15
        Espresso.onView(ViewMatchers.withId(R.id.recycler))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position1))

        Espresso.onView(itemViewMatcher(position1)).perform(ViewActions.click())
        Espresso.onView(itemViewMatcher(position2)).perform(ViewActions.click())

        Assert.assertEquals(R.id.detailFragment, testNavController.currentDestination?.id)
        Assert.assertEquals(listOf(R.id.nav_example_xml, R.id.homeFragment, R.id.detailFragment), testNavController.backStack.map { it.destination.id })
        val actualArgs = DetailFragmentArgs.fromBundle(testNavController.backStack.last().arguments ?: Bundle())
        Assert.assertEquals(position1, actualArgs.position)
    }

    private fun itemViewMatcher(position: Int) =
        Matchers.allOf(
            ViewMatchers.withText("Item $position"), ViewMatchers.withId(R.id.item_cta),
            ViewMatchers.withParent(ViewMatchers.withId(R.id.recycler))
        )

    companion object {
        inline fun <T : Fragment> FragmentScenario<T>.runOnMain(crossinline action: () -> Unit) {
            onFragment { action() }
        }
    }
}