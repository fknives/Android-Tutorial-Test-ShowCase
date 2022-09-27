package org.fnives.test.showcase.hilt.ui

import android.content.Context
import android.content.Intent
import org.fnives.test.showcase.hilt.ui.auth.AuthActivity
import org.fnives.test.showcase.hilt.ui.home.MainActivity

object IntentCoordinator {

    fun mainActivitygetStartIntent(context: Context): Intent =
        MainActivity.getStartIntent(context)

    fun authActivitygetStartIntent(context: Context): Intent =
        AuthActivity.getStartIntent(context)
}
