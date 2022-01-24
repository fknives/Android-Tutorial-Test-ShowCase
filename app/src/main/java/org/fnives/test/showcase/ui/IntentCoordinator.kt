package org.fnives.test.showcase.ui

import android.content.Context
import org.fnives.test.showcase.ui.auth.AuthActivity
import org.fnives.test.showcase.ui.home.MainActivity

object IntentCoordinator {

    fun mainActivitygetStartIntent(context: Context) =
        MainActivity.getStartIntent(context)

    fun authActivitygetStartIntent(context: Context) =
        AuthActivity.getStartIntent(context)
}
