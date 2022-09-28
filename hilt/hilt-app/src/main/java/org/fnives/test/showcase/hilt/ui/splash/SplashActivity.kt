package org.fnives.test.showcase.hilt.ui.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.fnives.test.showcase.hilt.R
import org.fnives.test.showcase.hilt.ui.IntentCoordinator
import org.fnives.test.showcase.hilt.ui.viewModels

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
open class SplashActivity : AppCompatActivity() {

    private val viewModel by viewModels<SplashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel.navigateTo.observe(this) {
            val intent = when (it.consume()) {
                SplashViewModel.NavigateTo.HOME -> IntentCoordinator.mainActivitygetStartIntent(this)
                SplashViewModel.NavigateTo.AUTHENTICATION -> IntentCoordinator.authActivitygetStartIntent(this)
                null -> return@observe
            }
            startActivity(intent)
            finishAffinity()
        }
    }
}
