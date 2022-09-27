package org.fnives.test.showcase.hilt.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.google.accompanist.insets.ProvideWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import org.fnives.test.showcase.hilt.compose.screen.AppNavigation

@AndroidEntryPoint
class ComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestShowCaseApp()
        }
    }
}

@Composable
fun TestShowCaseApp() {
    ProvideWindowInsets {
        MaterialTheme {
            AppNavigation()
        }
    }
}
