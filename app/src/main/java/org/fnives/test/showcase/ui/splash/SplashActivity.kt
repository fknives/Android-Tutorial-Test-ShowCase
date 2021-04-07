package org.fnives.test.showcase.ui.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.fnives.test.showcase.R
import org.fnives.test.showcase.ui.auth.AuthActivity
import org.fnives.test.showcase.ui.home.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : AppCompatActivity() {

    private val viewModel by viewModel<SplashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel.navigateTo.observe(this) {
            val intent = when (it.consume()) {
                SplashViewModel.NavigateTo.HOME -> MainActivity.getStartIntent(this)
                SplashViewModel.NavigateTo.AUTHENTICATION -> AuthActivity.getStartIntent(this)
                null -> return@observe
            }
            startActivity(intent)
            finishAffinity()
        }
    }
}
