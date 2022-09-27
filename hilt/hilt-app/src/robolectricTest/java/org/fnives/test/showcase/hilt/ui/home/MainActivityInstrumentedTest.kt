package org.fnives.test.showcase.hilt.ui.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import org.fnives.test.showcase.hilt.test.shared.ui.home.MainActivityInstrumentedSharedTest
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest : MainActivityInstrumentedSharedTest()
