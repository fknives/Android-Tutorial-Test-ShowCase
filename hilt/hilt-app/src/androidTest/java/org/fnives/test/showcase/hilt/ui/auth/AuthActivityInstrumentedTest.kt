package org.fnives.test.showcase.hilt.ui.auth

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import org.fnives.test.showcase.hilt.test.shared.ui.auth.AuthActivityInstrumentedSharedTest
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthActivityInstrumentedTest : AuthActivityInstrumentedSharedTest()
