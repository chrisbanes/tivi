/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.home.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AmbientContentAlpha
import androidx.compose.material.ButtonConstants
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.Carousel
import app.tivi.common.compose.IconResource
import app.tivi.common.compose.PosterCard
import app.tivi.common.compose.rememberMutableState
import app.tivi.common.compose.spacerItem
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TmdbImageEntity
import app.tivi.data.entities.TraktUser
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.trakt.TraktAuthState
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun Discover(
    state: DiscoverViewState,
    actioner: (DiscoverAction) -> Unit
) {
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            var appBarHeight by rememberMutableState { 0 }

            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    val height = with(AmbientDensity.current) { appBarHeight.toDp() }
                    Spacer(Modifier.preferredHeight(height))
                }

                spacerItem(16.dp)

                state.nextEpisodeWithShowToWatched?.let { nextEpisodeToWatch ->
                    item {
                        Header(title = stringResource(R.string.discover_keep_watching_title))
                    }
                    item {
                        NextEpisodeToWatch(
                            show = nextEpisodeToWatch.show,
                            poster = nextEpisodeToWatch.poster,
                            season = nextEpisodeToWatch.season,
                            episode = nextEpisodeToWatch.episode,
                            modifier = Modifier.fillMaxWidth().clickable {
                                actioner(
                                    OpenShowDetails(
                                        showId = nextEpisodeToWatch.show.id,
                                        episodeId = nextEpisodeToWatch.episode.id
                                    )
                                )
                            }
                        )
                    }

                    spacerItem(16.dp)
                }

                item {
                    CarouselWithHeader(
                        items = state.trendingItems,
                        title = stringResource(R.string.discover_trending_title),
                        refreshing = state.trendingRefreshing,
                        onItemClick = { actioner(OpenShowDetails(it.id)) },
                        onMoreClick = { actioner(OpenTrendingShows) }
                    )
                }

                item {
                    CarouselWithHeader(
                        items = state.recommendedItems,
                        title = stringResource(R.string.discover_recommended_title),
                        refreshing = state.recommendedRefreshing,
                        onItemClick = { actioner(OpenShowDetails(it.id)) },
                        onMoreClick = { actioner(OpenRecommendedShows) }
                    )
                }

                item {
                    CarouselWithHeader(
                        items = state.popularItems,
                        title = stringResource(R.string.discover_popular_title),
                        refreshing = state.popularRefreshing,
                        onItemClick = { actioner(OpenShowDetails(it.id)) },
                        onMoreClick = { actioner(OpenPopularShows) }
                    )
                }

                spacerItem(16.dp)
            }

            DiscoverAppBar(
                loggedIn = state.authState == TraktAuthState.LOGGED_IN,
                user = state.user,
                refreshing = state.refreshing,
                onRefreshActionClick = { actioner(RefreshAction) },
                onUserActionClick = { actioner(OpenUserDetails) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { appBarHeight = it.height }
            )
        }
    }
}

@Composable
private fun NextEpisodeToWatch(
    show: TiviShow,
    poster: TmdbImageEntity?,
    season: Season,
    episode: Episode,
    modifier: Modifier = Modifier,
) {
    Surface(modifier) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (poster != null) {
                PosterCard(
                    show = show,
                    poster = poster,
                    modifier = Modifier.preferredWidth(64.dp).aspectRatio(2 / 3f)
                )

                Spacer(Modifier.preferredWidth(16.dp))
            }

            Column(Modifier.align(Alignment.CenterVertically)) {
                val textCreator = AmbientDiscoverTextCreator.current
                Providers(AmbientContentAlpha provides ContentAlpha.disabled) {
                    Text(
                        text = textCreator.seasonEpisodeTitleText(season, episode),
                        style = MaterialTheme.typography.caption
                    )
                }

                Spacer(Modifier.preferredHeight(4.dp))

                Text(
                    text = episode.title
                        ?: stringResource(R.string.episode_title_fallback, episode.number!!),
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}

@Composable
private fun <T : EntryWithShow<*>> CarouselWithHeader(
    items: List<T>,
    title: String,
    refreshing: Boolean,
    onItemClick: (TiviShow) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        if (refreshing || items.isNotEmpty()) {
            Spacer(Modifier.preferredHeight(16.dp))

            Header(
                title = title,
                loading = refreshing,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onMoreClick,
                    colors = ButtonConstants.defaultTextButtonColors(
                        contentColor = MaterialTheme.colors.secondary
                    ),
                    modifier = Modifier.alignBy(FirstBaseline)
                ) {
                    Text(text = stringResource(R.string.header_more))
                }
            }
        }
        if (items.isNotEmpty()) {
            EntryShowCarousel(
                items = items,
                onItemClick = onItemClick,
                modifier = Modifier.preferredHeight(192.dp).fillMaxWidth()
            )
        }
        // TODO empty state
    }
}

@Composable
private fun <T : EntryWithShow<*>> EntryShowCarousel(
    items: List<T>,
    onItemClick: (TiviShow) -> Unit,
    modifier: Modifier = Modifier
) {
    Carousel(
        items = items,
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        itemSpacing = 4.dp,
        modifier = modifier
    ) { item, padding ->
        PosterCard(
            show = item.show,
            poster = item.poster,
            onClick = { onItemClick(item.show) },
            modifier = Modifier
                .padding(padding)
                .fillParentMaxHeight()
                .aspectRatio(2 / 3f)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Header(
    title: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    content: @Composable RowScope.() -> Unit = {}
) {
    Row(modifier) {
        Spacer(Modifier.preferredWidth(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(vertical = 8.dp)
        )

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(visible = loading) {
            AutoSizedCircularProgressIndicator(
                color = MaterialTheme.colors.secondary,
                modifier = Modifier.padding(8.dp).preferredSize(16.dp)
            )
        }

        content()

        Spacer(Modifier.preferredWidth(16.dp))
    }
}

private const val TranslucentAppBarAlpha = 0.93f

@Composable
private fun DiscoverAppBar(
    loggedIn: Boolean,
    user: TraktUser?,
    refreshing: Boolean,
    onRefreshActionClick: () -> Unit,
    onUserActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colors.surface.copy(alpha = TranslucentAppBarAlpha),
        contentColor = MaterialTheme.colors.onSurface,
        elevation = 4.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .preferredHeight(56.dp)
                .padding(start = 16.dp, end = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.discover_title),
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(Modifier.weight(1f))

            Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                IconButton(
                    onClick = onRefreshActionClick,
                    enabled = !refreshing,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    if (refreshing) {
                        AutoSizedCircularProgressIndicator(Modifier.preferredSize(20.dp))
                    } else {
                        Icon(Icons.Default.Refresh)
                    }
                }

                IconButton(
                    onClick = onUserActionClick,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    when {
                        loggedIn && user?.avatarUrl != null -> {
                            CoilImage(
                                data = user.avatarUrl!!,
                                modifier = Modifier.preferredSize(32.dp).clip(CircleShape)
                            )
                        }
                        loggedIn -> IconResource(R.drawable.ic_person)
                        else -> IconResource(R.drawable.ic_person_outline)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDiscoverAppBar() {
    DiscoverAppBar(
        loggedIn = false,
        user = null,
        refreshing = false,
        onUserActionClick = {},
        onRefreshActionClick = {}
    )
}

@Preview
@Composable
private fun PreviewHeader() {
    Surface(Modifier.fillMaxWidth()) {
        Header(
            title = "Being watched now",
            loading = true
        )
    }
}
