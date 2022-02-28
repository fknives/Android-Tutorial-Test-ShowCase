package org.fnives.test.showcase.ui.compose.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.fnives.test.showcase.R

@Composable
fun HomeScreen(
    homeScreenState = rememberHomeScreenState()
) {
    Column(Modifier.fillMaxSize()) {
        Title()
        SwipeRefresh(state = rememberSwipeRefreshState(isRefreshing = false), onRefresh = {  }) {
            LazyColumn {

            }
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
