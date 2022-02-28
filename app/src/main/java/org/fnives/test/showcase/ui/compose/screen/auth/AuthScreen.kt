package org.fnives.test.showcase.ui.compose.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.systemBarsPadding
import org.fnives.test.showcase.R

@Composable
fun AuthScreen(
    authScreenState: AuthScreenState = rememberAuthScreen()
) {
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Title()
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = authScreenState.username,
                onValueChange = { authScreenState.onUsernameChanged(it) },
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = authScreenState.password,
                onValueChange = { authScreenState.onPasswordChanged(it) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }

        Snackbar(authScreenState)
        LoginButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            onClick = { authScreenState.onLogin() }
        )
    }
}

@Composable
private fun Snackbar(authScreenState: AuthScreenState) {
    val snackbarState = remember { SnackbarHostState() }
    val errorType = authScreenState.error?.consume()
    LaunchedEffect(errorType) {
        if (errorType != null) {
            snackbarState.showSnackbar(errorType.name)
        }
    }
    SnackbarHost(hostState = snackbarState) {
        val stringId = errorType?.stringResId()
        if (stringId != null) {
            Snackbar(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(text = stringResource(stringId))
            }
        }
    }
}

@Composable
private fun LoginButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier) {
        Button(onClick = onClick, Modifier.fillMaxWidth()) {
            Text(text = "Login")
        }
    }
}

@Composable
private fun Title() {
    Text(
        stringResource(id = R.string.login_title),
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.h4
    )
}

private fun AuthScreenState.ErrorType.stringResId() = when (this) {
    AuthScreenState.ErrorType.INVALID_CREDENTIALS -> R.string.credentials_invalid
    AuthScreenState.ErrorType.GENERAL_NETWORK_ERROR -> R.string.something_went_wrong
    AuthScreenState.ErrorType.UNSUPPORTED_USERNAME -> R.string.username_is_invalid
    AuthScreenState.ErrorType.UNSUPPORTED_PASSWORD -> R.string.password_is_invalid
}