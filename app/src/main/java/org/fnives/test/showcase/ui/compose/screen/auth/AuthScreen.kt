package org.fnives.test.showcase.ui.compose.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.accompanist.insets.statusBarsPadding
import org.fnives.test.showcase.R

@Composable
fun AuthScreen(
    authScreenState: AuthScreenState = rememberAuthScreenState()
) {
    ConstraintLayout(Modifier.fillMaxSize()) {
        val (title, credentials, snackbar, loading, login) = createRefs()
        Title(
            Modifier
                .statusBarsPadding()
                .constrainAs(title) { top.linkTo(parent.top) })
        CredentialsFields(authScreenState, Modifier.constrainAs(credentials) {
            top.linkTo(title.bottom)
            bottom.linkTo(login.top)
        })
        Snackbar(authScreenState, Modifier.constrainAs(snackbar) {
            bottom.linkTo(login.top)
        })
        if (authScreenState.loading) {
            CircularProgressIndicator(Modifier.constrainAs(loading) {
                bottom.linkTo(login.top)
                centerHorizontallyTo(parent)
            })
        }
        LoginButton(
            modifier = Modifier
                .constrainAs(login) { bottom.linkTo(parent.bottom) }
                .padding(16.dp),
            onClick = { authScreenState.onLogin() }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CredentialsFields(authScreenState: AuthScreenState, modifier: Modifier = Modifier) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = authScreenState.username,
            label = { Text(text = stringResource(id = R.string.username)) },
            placeholder = { Text(text = stringResource(id = R.string.username)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            onValueChange = { authScreenState.onUsernameChanged(it) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = authScreenState.password,
            label = { Text(text = stringResource(id = R.string.password)) },
            placeholder = { Text(text = stringResource(id = R.string.password)) },
            onValueChange = { authScreenState.onPasswordChanged(it) },
            keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                authScreenState.onLogin()
            }),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
    }
}

@Composable
private fun Snackbar(authScreenState: AuthScreenState, modifier: Modifier = Modifier) {
    val snackbarState = remember { SnackbarHostState() }
    val error = authScreenState.error
    LaunchedEffect(error) {
        if (error != null) {
            snackbarState.showSnackbar(error.name)
            authScreenState.dismissError()
        }
    }
    SnackbarHost(hostState = snackbarState, modifier) {
        val stringId = error?.stringResId()
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
private fun Title(modifier: Modifier = Modifier) {
    Text(
        stringResource(id = R.string.login_title),
        modifier = modifier.padding(16.dp),
        style = MaterialTheme.typography.h4
    )
}

private fun AuthScreenState.ErrorType.stringResId() = when (this) {
    AuthScreenState.ErrorType.INVALID_CREDENTIALS -> R.string.credentials_invalid
    AuthScreenState.ErrorType.GENERAL_NETWORK_ERROR -> R.string.something_went_wrong
    AuthScreenState.ErrorType.UNSUPPORTED_USERNAME -> R.string.username_is_invalid
    AuthScreenState.ErrorType.UNSUPPORTED_PASSWORD -> R.string.password_is_invalid
}