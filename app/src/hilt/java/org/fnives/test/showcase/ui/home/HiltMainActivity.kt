package org.fnives.test.showcase.ui.home

import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltMainActivity : MainActivity() {
    companion object {
        fun getStartIntent(context: Context): Intent = Intent(context, HiltMainActivity::class.java)
    }
}
