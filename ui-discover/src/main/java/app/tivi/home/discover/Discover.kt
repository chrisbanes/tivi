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

import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.InnerPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideEmphasis
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.emptyContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.ui.tooling.preview.Preview
import app.tivi.common.compose.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.Carousel
import app.tivi.common.compose.IconResource
import app.tivi.common.compose.LogCompositions
import app.tivi.common.compose.PosterCard
import app.tivi.common.compose.ProvideDisplayInsets
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.common.compose.onSizeChanged
import app.tivi.common.compose.rememberMutableState
import app.tivi.common.compose.statusBarsPadding
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TmdbImageEntity
import app.tivi.data.entities.TraktUser
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.trakt.TraktAuthState
import app.tivi.util.TiviDateFormatter
import com.google.android.material.composethemeadapter.MdcTheme
import dev.chrisbanes.accompanist.coil.CoilImage

internal fun ViewGroup.composeDiscover(
    state: LiveData<DiscoverViewState>,
    actioner: (DiscoverAction) -> Unit,
    tiviDateFormatter: TiviDateFormatter,
    textCreator: DiscoverTextCreator
): Any = setContent(Recomposer.current()) {
    Providers(
        TiviDateFormatterAmbient provides tiviDateFormatter,
        DiscoverTextCreatorAmbient provides textCreator
    ) {
        MdcTheme {
            LogCompositions("MdcTheme")

            ProvideDisplayInsets {
                LogCompositions("ProvideInsets")
                val viewState by state.observeAsState()
                if (viewState != null) {
                    LogCompositions("ViewState observeAsState")
                    Discover(viewState!!, actioner)
                }
            }
        }
    }
}

@OptIn(ExperimentalLazyDsl::class)
@Composable
fun Discover(
    state: DiscoverViewState,
    actioner: (DiscoverAction) -> Unit
) {
    Stack(Modifier.fillMaxSize()) {
        var appBarHeight by rememberMutableState { 0 }

        LazyColumn(Modifier.fillMaxSize()) {
            item {
                val height = with(DensityAmbient.current) { appBarHeight.toDp() } + 16.dp
                Spacer(Modifier.preferredHeight(height))
            }

            state.nextEpisodeWithShowToWatched?.also { nextEpisodeToWatch ->
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

                item { Spacer(Modifier.preferredHeight(16.dp)) }
            }

            if (state.trendingRefreshing || state.trendingItems.isNotEmpty()) {
                item {
                    Header(
                        title = stringResource(R.string.discover_trending_title),
                        loading = state.trendingRefreshing
                    )
                }
            }
            if (state.trendingItems.isNotEmpty()) {
                item {
                    EntryShowCarousel(
                        items = state.trendingItems,
                        onItemClick = { actioner(OpenShowDetails(it.id)) },
                        modifier = Modifier.preferredHeight(192.dp).fillMaxWidth()
                    )
                }
            } else {
                // TODO empty state
            }

            if (state.recommendedRefreshing || state.recommendedItems.isNotEmpty()) {
                item { Spacer(Modifier.preferredHeight(16.dp)) }

                item {
                    Header(
                        title = stringResource(R.string.discover_recommended_title),
                        loading = state.recommendedRefreshing
                    )
                }
            }
            if (state.recommendedItems.isNotEmpty()) {
                item {
                    EntryShowCarousel(
                        items = state.recommendedItems,
                        onItemClick = { actioner(OpenShowDetails(it.id)) },
                        modifier = Modifier.preferredHeight(192.dp).fillMaxWidth()
                    )
                }
            } else {
                // TODO empty state
            }

            if (state.popularRefreshing || state.popularItems.isNotEmpty()) {
                item { Spacer(Modifier.preferredHeight(16.dp)) }
                item {
                    Header(
                        title = stringResource(R.string.discover_popular_title),
                        loading = state.popularRefreshing
                    )
                }
            }
            if (state.popularItems.isNotEmpty()) {
                item {
                    EntryShowCarousel(
                        items = state.popularItems,
                        onItemClick = { actioner(OpenShowDetails(it.id)) },
                        modifier = Modifier.preferredHeight(192.dp).fillMaxWidth()
                    )
                }
            } else {
                // TODO empty state
            }

            item { Spacer(Modifier.preferredHeight(16.dp)) }
        }

        DiscoverAppBar(
            loggedIn = state.authState == TraktAuthState.LOGGED_IN,
            user = state.user,
            actioner = actioner,
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { appBarHeight = it.height }
        )
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
            }

            Spacer(Modifier.preferredWidth(16.dp))

            Column(Modifier.gravity(Alignment.CenterVertically)) {
                val textCreator = DiscoverTextCreatorAmbient.current
                ProvideEmphasis(EmphasisAmbient.current.disabled) {
                    Text(
                        text = textCreator.seasonEpisodeTitleText(season, episode),
                        style = MaterialTheme.typography.caption
                    )
                }

                Spacer(Modifier.preferredHeight(4.dp))

                ProvideEmphasis(EmphasisAmbient.current.high) {
                    Text(
                        text = episode.title ?: stringResource(R.string.episode_title_fallback),
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }
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
        contentPadding = InnerPadding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
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
    loading: Boolean = false,
    action: @Composable () -> Unit = emptyContent(),
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        ProvideEmphasis(EmphasisAmbient.current.high) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier
                    .gravity(Alignment.CenterVertically)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .weight(1f, true)
            )
        }

        AnimatedVisibility(visible = loading) {
            AutoSizedCircularProgressIndicator(
                Modifier.padding(horizontal = 8.dp).preferredSize(32.dp)
            )
        }

        action()
    }
}

@Composable
private fun DiscoverAppBar(
    loggedIn: Boolean,
    user: TraktUser?,
    actioner: (DiscoverAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colors.surface.copy(alpha = 0.93f),
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
                modifier = Modifier.weight(1f, fill = true)
                    .gravity(Alignment.CenterVertically)
            )

            IconButton(
                onClick = { actioner(OpenUserDetails) },
                modifier = Modifier.gravity(Alignment.CenterVertically)
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

@Preview
@Composable
private fun PreviewDiscoverAppBar() {
    DiscoverAppBar(
        loggedIn = false,
        user = null,
        actioner = {}
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
