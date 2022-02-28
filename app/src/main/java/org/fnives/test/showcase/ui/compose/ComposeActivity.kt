package org.fnives.test.showcase.ui.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import com.google.accompanist.insets.ProvideWindowInsets
import org.fnives.test.showcase.ui.compose.screen.AppNavigation

class ComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProvideWindowInsets {
                MaterialTheme {
                    AppNavigation()
                }
            }
        }
    }
}