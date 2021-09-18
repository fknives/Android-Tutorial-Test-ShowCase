package org.fnives.test.showcase.ui

import org.fnives.test.showcase.ui.auth.HiltAuthActivity
import org.fnives.test.showcase.ui.home.HiltMainActivity
import org.fnives.test.showcase.ui.splash.HiltSplashActivity

object ActivityClassHolder {

    fun authActivity() = HiltAuthActivity::class

    fun mainActivity() = HiltMainActivity::class

    fun splashActivity() = HiltSplashActivity::class
}
