package org.fnives.test.showcase.hilt.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.fnives.test.showcase.hilt.R
import org.fnives.test.showcase.hilt.databinding.ActivityAuthenticationBinding
import org.fnives.test.showcase.hilt.ui.IntentCoordinator
import org.fnives.test.showcase.hilt.ui.viewModels

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private val viewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        viewModel.loading.observe(this) {
            binding.loadingIndicator.isVisible = it == true
        }
        viewModel.password.observe(this, SetTextIfNotSameObserver(binding.passwordEditText))
        binding.passwordEditText.doAfterTextChanged { viewModel.onPasswordChanged(it?.toString().orEmpty()) }
        viewModel.username.observe(this, SetTextIfNotSameObserver(binding.userEditText))
        binding.userEditText.doAfterTextChanged { viewModel.onUsernameChanged(it?.toString().orEmpty()) }
        binding.loginCta.setOnClickListener {
            viewModel.onLogin()
        }
        viewModel.error.observe(this) {
            val stringResId = it?.consume()?.stringResId() ?: return@observe
            Snackbar.make(binding.snackbarHolder, stringResId, Snackbar.LENGTH_LONG).show()
        }
        viewModel.navigateToHome.observe(this) {
            it.consume() ?: return@observe
            startActivity(IntentCoordinator.mainActivitygetStartIntent(this))
            finishAffinity()
        }
        setContentView(binding.root)
    }

    companion object {

        private fun AuthViewModel.ErrorType.stringResId() = when (this) {
            AuthViewModel.ErrorType.INVALID_CREDENTIALS -> R.string.credentials_invalid
            AuthViewModel.ErrorType.GENERAL_NETWORK_ERROR -> R.string.something_went_wrong
            AuthViewModel.ErrorType.UNSUPPORTED_USERNAME -> R.string.username_is_invalid
            AuthViewModel.ErrorType.UNSUPPORTED_PASSWORD -> R.string.password_is_invalid
        }

        fun getStartIntent(context: Context): Intent = Intent(context, AuthActivity::class.java)
    }
}
