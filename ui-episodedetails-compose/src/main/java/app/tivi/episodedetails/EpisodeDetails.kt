/*
 * Copyright 2019 Google LLC
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

package app.tivi.episodedetails

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.Composable
import androidx.compose.Compose
import androidx.compose.ambient
import androidx.compose.state
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.ui.core.Modifier
import androidx.ui.core.Text
import androidx.ui.core.WithDensity
import androidx.ui.core.setContent
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.VerticalScroller
import androidx.ui.graphics.Color
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.Container
import androidx.ui.layout.EdgeInsets
import androidx.ui.layout.LayoutAlign
import androidx.ui.layout.LayoutAspectRatio
import androidx.ui.layout.LayoutGravity
import androidx.ui.layout.LayoutHeight
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.Padding
import androidx.ui.layout.Row
import androidx.ui.layout.Spacer
import androidx.ui.layout.Stack
import androidx.ui.material.EmphasisLevels
import androidx.ui.material.FloatingActionButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ProvideEmphasis
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.stringResource
import androidx.ui.text.style.TextOverflow
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Season
import app.tivi.episodedetails.compose.R
import app.tivi.util.TiviDateFormatter

/**
 * This is a bit of hack. I can't make `ui-episodedetails` depend on any of the compose libraries,
 * so I wrap `setContext` as my own function, which `ui-episodedetails` can use.
 */
fun ViewGroup.composeEpisodeDetails(
    state: LiveData<EpisodeDetailsViewState>,
    insets: LiveData<WindowInsetsCompat>,
    actioner: (EpisodeDetailsAction) -> Unit,
    tiviDateFormatter: TiviDateFormatter
) {
    setContent {
        WrapInAmbients(tiviDateFormatter, InsetsHolder()) {
            observeInsets(insets)

            val viewState = observe(state)
            if (viewState != null) {
                MaterialThemeFromAndroidTheme(context) {
                    EpisodeDetails(viewState, actioner)
                }
            }
        }
    }
}

fun ViewGroup.disposeComposition() = Compose.disposeComposition(this)

@Composable
private fun EpisodeDetails(
    viewState: EpisodeDetailsViewState,
    actioner: (EpisodeDetailsAction) -> Unit
) {
    Stack {
        Column {
            viewState.episode?.let {
                Backdrop(episode = it)
            }
            VerticalScroller(modifier = LayoutFlexible(1f)) {
                Column {
                    val episode = viewState.episode
                    if (episode != null) {
                        InfoPanes(episode)
                        Summary(episode)
                    }

                    val watches = viewState.watches
                    if (watches.isNotEmpty()) {
                        Header()
                        watches.forEach { EpisodeWatch(it) }
                    }
                }
            }
        }

        val insets = ambient(InsetsAmbient)
        WithDensity {
            WatchButton(
                modifier = LayoutGravity.BottomRight +
                    LayoutPadding(horizontal = 16.dp, bottom = 16.dp + insets.bottom.toDp()),
                action = viewState.action,
                actioner = actioner
            )
        }
    }
}

@Composable
private fun Backdrop(episode: Episode) {
    Surface(
        color = MaterialTheme.colors().onSurface.copy(alpha = 0.1f),
        modifier = LayoutAspectRatio(16f / 9)
    ) {
        Stack {
            if (episode.tmdbBackdropPath != null) {
                LoadAndShowImage(modifier = LayoutGravity.Stretch, data = episode)
            }

            Container(modifier = LayoutGravity.Stretch) {
                GradientScrim(baseColor = Color.Black)
            }

            val type = MaterialTheme.typography()
            Text(
                text = episode.title ?: "No title",
                style = type.h6.copy(color = Color.White),
                modifier = LayoutPadding(all = 16.dp) + LayoutGravity.BottomLeft
            )
        }
    }
}

@Composable
private fun InfoPanes(episode: Episode) {
    Row(
        arrangement = Arrangement.SpaceEvenly
    ) {
        episode.traktRating?.let { rating ->
            InfoPane(
                modifier = LayoutFlexible(1f),
                iconResId = R.drawable.ic_details_rating,
                label = stringResource(R.string.trakt_rating_text, rating * 10f)
            )
        }

        episode.firstAired?.let { firstAired ->
            val formatter = ambient(TiviDateFormatterAmbient)
            InfoPane(
                modifier = LayoutFlexible(1f),
                iconResId = R.drawable.ic_details_date,
                label = formatter.formatShortRelativeTime(firstAired)
            )
        }
    }
}

@Composable
private fun InfoPane(
    modifier: Modifier = Modifier.None,
    @DrawableRes iconResId: Int,
    label: String
) {
    Column(
        modifier = modifier + LayoutPadding(all = 16.dp)
    ) {
        VectorImage(
            modifier = LayoutAlign.CenterHorizontally,
            id = iconResId,
            tint = MaterialTheme.colors().onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = LayoutHeight(4.dp))

        ProvideEmphasis(emphasis = EmphasisLevels().high) {
            Text(
                modifier = LayoutAlign.CenterHorizontally,
                text = label,
                style = MaterialTheme.typography().body1
            )
        }
    }
}

@Composable
private fun Summary(episode: Episode) {
    Surface {
        Ripple(bounded = false) {
            Padding(padding = EdgeInsets(16.dp)) {
                val expanded = state { false }
                Clickable(onClick = { expanded.value = !expanded.value }) {
                    ProvideEmphasis(emphasis = EmphasisLevels().high) {
                        Text(
                            text = episode.summary ?: "No summary",
                            style = MaterialTheme.typography().body2,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = if (expanded.value) Int.MAX_VALUE else 4
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Padding(padding = EdgeInsets(16.dp, 8.dp, 16.dp, 8.dp)) {
        ProvideEmphasis(emphasis = EmphasisLevels().high) {
            Text(
                text = stringResource(R.string.episode_watches),
                style = MaterialTheme.typography().subtitle1
            )
        }
    }
}

@Composable
private fun EpisodeWatch(episodeWatchEntry: EpisodeWatchEntry) {
    Padding(padding = EdgeInsets(16.dp, 8.dp, 16.dp, 8.dp)) {
        Row {
            val formatter = ambient(TiviDateFormatterAmbient)
            ProvideEmphasis(emphasis = EmphasisLevels().high) {
                Text(
                    modifier = LayoutFlexible(1f),
                    text = formatter.formatMediumDateTime(episodeWatchEntry.watchedAt),
                    style = MaterialTheme.typography().body2
                )
            }

            if (episodeWatchEntry.pendingAction != PendingAction.NOTHING) {
                VectorImage(
                    id = R.drawable.ic_upload_24dp,
                    tint = MaterialTheme.colors().onSurface.copy(alpha = 0.7f),
                    modifier = LayoutPadding(left = 8.dp)
                )
            }
            VectorImage(
                id = when (episodeWatchEntry.pendingAction) {
                    PendingAction.DELETE -> R.drawable.ic_eye_off_24dp
                    else -> R.drawable.ic_eye_24dp
                },
                tint = MaterialTheme.colors().onSurface.copy(alpha = 0.7f),
                modifier = LayoutPadding(left = 8.dp)
            )
        }
    }
}

@Composable
fun WatchButton(
    modifier: Modifier = Modifier.None,
    action: Action,
    actioner: (EpisodeDetailsAction) -> Unit
) = FloatingActionButton(
    modifier = modifier,
    color = MaterialTheme.colors().secondary,
    onClick = {
        actioner(
            when (action) {
                Action.WATCH -> AddEpisodeWatchAction
                Action.UNWATCH -> RemoveAllEpisodeWatchesAction
            }
        )
    }
) {
    VectorImage(
        id = when (action) {
            Action.WATCH -> R.drawable.ic_eye_24dp
            Action.UNWATCH -> R.drawable.ic_eye_off_24dp
        }
    )
}

@Preview
@Composable
fun previewEpisodeDetails() = EpisodeDetails(
    viewState = EpisodeDetailsViewState(
        episodeId = 0,
        episode = Episode(
            seasonId = 100,
            title = "A show too far"
        ),
        season = Season(
            id = 100,
            showId = 0
        )
    ),
    actioner = {}
)
