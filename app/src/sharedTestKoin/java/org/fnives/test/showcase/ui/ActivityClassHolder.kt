package org.fnives.test.showcase.ui

import org.fnives.test.showcase.ui.auth.AuthActivity
import org.fnives.test.showcase.ui.home.MainActivity
import org.fnives.test.showcase.ui.splash.SplashActivity

object ActivityClassHolder {

    fun authActivity() = AuthActivity::class

    fun mainActivity() = MainActivity::class

    fun splashActivity() = SplashActivity::class
}
