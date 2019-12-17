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
import androidx.compose.ambient
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.lifecycle.LiveData
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.setContent
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.layout.Arrangement
import androidx.ui.layout.AspectRatio
import androidx.ui.layout.Column
import androidx.ui.layout.EdgeInsets
import androidx.ui.layout.HeightSpacer
import androidx.ui.layout.Padding
import androidx.ui.layout.Row
import androidx.ui.layout.Spacing
import androidx.ui.layout.Stack
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.material.withOpacity
import androidx.ui.res.stringResource
import androidx.ui.text.style.TextOverflow
import androidx.ui.tooling.preview.Preview
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
fun composeEpisodeDetails(
    viewGroup: ViewGroup,
    state: LiveData<EpisodeDetailsViewState>,
    tiviDateFormatter: TiviDateFormatter
) {
    viewGroup.setContent {
        WrapInAmbients(tiviDateFormatter) {
            val viewState = +observe(state)
            if (viewState != null) {
                MaterialTheme(
                        typography = themeTypography,
                        colors = if (+isSystemInDarkTheme()) darkThemeColors else lightThemeColors
                ) {
                    EpisodeDetails(viewState)
                }
            }
        }
    }
}

@Composable
private fun EpisodeDetails(viewState: EpisodeDetailsViewState) {
    Column {
        viewState.episode?.let {
            Backdrop(episode = it)
        }
        VerticalScroller(modifier = Flexible(1f)) {
            Column {
                val episode = viewState.episode
                if (episode != null) {
                    InfoPanes(episode)
                    Summary(episode)
                }

                val watches = viewState.watches
                if (watches.isNotEmpty()) {
                    Header()
                    watches.forEach {
                        EpisodeWatch(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun Backdrop(episode: Episode) {
    Surface(
            color = (+MaterialTheme.colors()).onSurface.copy(alpha = 0.1f),
            modifier = AspectRatio(16 / 9f)
    ) {
        Stack {
            expanded {
                if (episode.tmdbBackdropPath != null) {
                    val image = +image(episode)
                    if (image != null) {
                        DrawImage(image = image)
                    }
                }
            }
            expanded {
                GradientScrim(baseColor = darkThemeColors.background)
            }
            aligned(Alignment.BottomLeft) {
                val type = +MaterialTheme.typography()
                Text(
                        text = episode.title ?: "No title",
                        style = type.h6.copy(color = darkThemeColors.onSurface),
                        modifier = Spacing(all = 16.dp)
                )
            }
        }
    }
}

private fun InfoPanes(episode: Episode) {
    Row(
            arrangement = Arrangement.SpaceAround
    ) {
        episode.traktRating?.let { rating ->
            InfoPane(
                    iconResId = R.drawable.ic_details_rating,
                    label = +stringResource(R.string.trakt_rating_text, rating * 10f)
            )
        }

        episode.firstAired?.let { firstAired ->
            val formatter = +ambient(TiviDateFormatterAmbient)
            InfoPane(
                    iconResId = R.drawable.ic_details_date,
                    label = formatter.formatShortRelativeTime(firstAired)
            )
        }
    }
}

private fun InfoPane(
    modifier: Modifier = Modifier.None,
    @DrawableRes iconResId: Int,
    label: String
) {
    Column(
            // modifier = modifier wraps Spacing(all = 16.dp)
    ) {
        VectorImage(
                id = iconResId,
                tint = (+MaterialTheme.colors()).onSurface.copy(alpha = 0.7f)
        )
        HeightSpacer(height = 4.dp)
        Text(
                text = label,
                style = (+MaterialTheme.typography()).body1.withOpacity(0.87f)
        )
    }
}

private fun Summary(episode: Episode) {
    Surface {
        Ripple(bounded = false) {
            Padding(padding = EdgeInsets(16.dp)) {
                val expanded = +state { false }
                Clickable(onClick = { expanded.value = !expanded.value }) {
                    Text(
                            text = episode.summary ?: "No summary",
                            style = (+MaterialTheme.typography()).body2.withOpacity(0.87f),
                            maxLines = if (expanded.value) null else 4,
                            overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun Header() {
    Padding(padding = EdgeInsets(16.dp, 8.dp, 16.dp, 8.dp)) {
        Text(
                text = +stringResource(R.string.episode_watches),
                style = (+MaterialTheme.typography()).subtitle1.withOpacity(0.87f)
        )
    }
}

private fun EpisodeWatch(episodeWatchEntry: EpisodeWatchEntry) {
    Padding(padding = EdgeInsets(16.dp, 8.dp, 16.dp, 8.dp)) {
        Row {
            val formatter = +ambient(TiviDateFormatterAmbient)
            Text(
                    modifier = Flexible(1f),
                    text = formatter.formatMediumDateTime(episodeWatchEntry.watchedAt),
                    style = (+MaterialTheme.typography()).body2.withOpacity(0.87f)
            )

            if (episodeWatchEntry.pendingAction != PendingAction.NOTHING) {
                VectorImage(
                        id = R.drawable.ic_upload_24dp,
                        tint = (+MaterialTheme.colors()).onSurface.copy(alpha = 0.7f),
                        modifier = Spacing(left = 8.dp)
                )
            }
            VectorImage(
                    id = when (episodeWatchEntry.pendingAction) {
                        PendingAction.DELETE -> R.drawable.ic_eye_off_24dp
                        else -> R.drawable.ic_eye_24dp
                    },
                    tint = (+MaterialTheme.colors()).onSurface.copy(alpha = 0.7f),
                    modifier = Spacing(left = 8.dp)
            )
        }
    }
}

@Preview
@Composable
private fun previewEpisodeDetails() {
    EpisodeDetails(
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
            )
    )
}
