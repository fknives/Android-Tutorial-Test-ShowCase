package org.fnives.test.showcase.compose.screen.splash

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.fnives.test.showcase.R

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(colorResource(R.color.purple_700)), contentAlignment = Alignment.Center) {
        val resourceId = if (VERSION.SDK_INT >= VERSION_CODES.N) {
            R.drawable.ic_launcher_foreground
        } else {
            R.mipmap.ic_launcher_round
        }
        Image(
            painter = painterResource(resourceId),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
    }
}
