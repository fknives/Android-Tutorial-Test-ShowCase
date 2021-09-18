package org.fnives.test.showcase.ui

import android.content.Context
import org.fnives.test.showcase.ui.auth.HiltAuthActivity
import org.fnives.test.showcase.ui.home.HiltMainActivity

object IntentCoordinator {

    fun mainActivitygetStartIntent(context: Context) =
        HiltMainActivity.getStartIntent(context)

    fun authActivitygetStartIntent(context: Context) =
        HiltAuthActivity.getStartIntent(context)
}
