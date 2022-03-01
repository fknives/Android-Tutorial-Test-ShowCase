package org.fnives.test.showcase.ui.compose.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.fnives.test.showcase.R
import org.fnives.test.showcase.model.content.FavouriteContent

@Composable
fun HomeScreen(
    homeScreenState: HomeScreenState = rememberHomeScreenState()
) {
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Title(Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.logout_24),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { homeScreenState.onLogout() }
            )
        }
        Box {
            if (homeScreenState.isError) {
                ErrorText(Modifier.align(Alignment.Center))
            }
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = homeScreenState.loading),
                onRefresh = {
                    homeScreenState.onRefresh()
                }) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(homeScreenState.content) { item ->
                        Item(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            favouriteContent = item,
                            onFavouriteToggle = { homeScreenState.onFavouriteToggleClicked(item.content.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Item(
    modifier: Modifier = Modifier,
    favouriteContent: FavouriteContent,
    onFavouriteToggle: () -> Unit,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = rememberImagePainter(favouriteContent.content.imageUrl.url),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(120.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
        )
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(text = favouriteContent.content.title)
            Text(text = favouriteContent.content.description)
        }
        Image(
            painter = painterResource(id = if (favouriteContent.isFavourite) R.drawable.favorite_24 else R.drawable.favorite_border_24),
            contentDescription = null,
            Modifier.clickable { onFavouriteToggle() }
        )
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

@Composable
private fun ErrorText(modifier: Modifier = Modifier) {
    Text(
        stringResource(id = R.string.something_went_wrong),
        modifier = modifier.padding(16.dp),
        style = MaterialTheme.typography.h4,
        textAlign = TextAlign.Center
    )
}