package org.fnives.test.showcase.hilt.test.shared.ui

import dagger.hilt.android.testing.HiltAndroidRule
import org.fnives.test.showcase.hilt.test.shared.testutils.idling.NetworkSynchronizationHelper
import org.junit.After
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

open class NetworkSynchronizedActivityTest {

    @Inject
    lateinit var networkSynchronizationHelper: NetworkSynchronizationHelper

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        setupBeforeInjection()

        hiltRule.inject()
        networkSynchronizationHelper.setup()
        setupAfterInjection()
    }

    open fun setupBeforeInjection() {
    }

    open fun setupAfterInjection() {
    }

    @After
    fun tearDown() {
        networkSynchronizationHelper.dispose()
        additionalTearDown()
    }

    open fun additionalTearDown() {
    }
}
