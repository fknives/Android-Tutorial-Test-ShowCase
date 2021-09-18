package org.fnives.test.showcase.ui.auth

import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltAuthActivity : AuthActivity() {
    companion object {
        fun getStartIntent(context: Context): Intent = Intent(context, HiltAuthActivity::class.java)
    }
}