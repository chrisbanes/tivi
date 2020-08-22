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

import android.os.Build
import android.view.ViewGroup
import androidx.compose.animation.ColorPropKey
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.animation.transition
import androidx.compose.foundation.ContentColorAmbient
import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.contentColor
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredSizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.ProvideEmphasis
import androidx.compose.material.Snackbar
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ConfigurationAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.loadVectorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.ui.tooling.preview.Preview
import app.tivi.common.compose.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ExpandingText
import app.tivi.common.compose.IconResource
import app.tivi.common.compose.ProvideDisplayInsets
import app.tivi.common.compose.SwipeDirection
import app.tivi.common.compose.SwipeToDismiss
import app.tivi.common.compose.TiviAlertDialog
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.common.compose.boundsInParent
import app.tivi.common.compose.navigationBarsHeight
import app.tivi.common.compose.navigationBarsPadding
import app.tivi.common.compose.onLoadRun
import app.tivi.common.compose.onPositionInParentChanged
import app.tivi.common.compose.rememberMutableState
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Season
import app.tivi.ui.animations.lerp
import app.tivi.util.TiviDateFormatter
import com.google.android.material.composethemeadapter.MdcTheme
import dev.chrisbanes.accompanist.coil.CoilImageWithCrossfade
import org.threeten.bp.OffsetDateTime
import kotlin.math.hypot

/**
 * This is a bit of hack. I can't make `ui-episodedetails` depend on any of the compose libraries,
 * so I wrap `setContext` as my own function, which `ui-episodedetails` can use.
 *
 * We need to return an `Any` since this method will be called from modules which do not depend
 * on Compose
 */
fun ViewGroup.composeEpisodeDetails(
    state: LiveData<EpisodeDetailsViewState>,
    actioner: (EpisodeDetailsAction) -> Unit,
    tiviDateFormatter: TiviDateFormatter
): Any = setContent(Recomposer.current()) {
    MdcTheme {
        Providers(TiviDateFormatterAmbient provides tiviDateFormatter) {
            ProvideDisplayInsets {
                val viewState by state.observeAsState()
                if (viewState != null) {
                    EpisodeDetails(viewState!!, actioner)
                }
            }
        }
    }
}

@Composable
private fun EpisodeDetails(
    viewState: EpisodeDetailsViewState,
    actioner: (EpisodeDetailsAction) -> Unit
) {
    Stack {
        Column {
            Stack {
                if (viewState.episode != null && viewState.season != null) {
                    Backdrop(
                        season = viewState.season,
                        episode = viewState.episode,
                        modifier = Modifier.aspectRatio(16 / 10f)
                    )
                }
                EpisodeDetailsAppBar(
                    backgroundColor = Color.Transparent,
                    isRefreshing = viewState.refreshing,
                    actioner = actioner,
                    elevation = 0.dp
                )
            }
            ScrollableColumn {
                Surface(elevation = 2.dp) {
                    Column {
                        val episode = viewState.episode
                        if (episode != null) {
                            InfoPanes(episode)

                            ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
                                ExpandingText(
                                    episode.summary ?: "No summary",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        val watches = viewState.watches

                        if (viewState.canAddEpisodeWatch) {
                            Spacer(modifier = Modifier.preferredHeight(8.dp))

                            if (watches.isEmpty()) {
                                MarkWatchedButton(
                                    modifier = Modifier.gravity(Alignment.CenterHorizontally),
                                    actioner = actioner
                                )
                            } else {
                                AddWatchButton(
                                    modifier = Modifier.gravity(Alignment.CenterHorizontally),
                                    actioner = actioner
                                )
                            }
                        }

                        Spacer(modifier = Modifier.preferredHeight(16.dp))

                        if (watches.isNotEmpty()) {
                            var openDialog by rememberMutableState { false }

                            EpisodeWatchesHeader(
                                onSweepWatchesClick = { openDialog = true }
                            )

                            if (openDialog) {
                                RemoveAllWatchesDialog(
                                    actioner = actioner,
                                    onDialogClosed = { openDialog = false }
                                )
                            }
                        }

                        watches.forEach { watch ->
                            SwipeToDismiss(
                                swipeDirections = listOf(SwipeDirection.START),
                                onSwipeComplete = {
                                    actioner(RemoveEpisodeWatchAction(watch.id))
                                },
                                swipeChildren = { _, _ -> EpisodeWatch(episodeWatchEntry = watch) },
                                backgroundChildren = { swipeProgress, completeOnRelease ->
                                    EpisodeWatchSwipeBackground(
                                        swipeProgress = swipeProgress,
                                        wouldCompleteOnRelease = completeOnRelease
                                    )
                                }
                            )
                        }

                        Spacer(Modifier.preferredHeight(8.dp))
                        Spacer(Modifier.navigationBarsHeight())
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
                .gravity(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            Crossfade(current = viewState.error) { error ->
                if (error != null) {
                    // TODO: Convert this to swipe-to-dismiss
                    Snackbar(
                        text = { Text(error.message) },
                        modifier = Modifier.clickable(onClick = { actioner(ClearError) })
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Backdrop(
    season: Season,
    episode: Episode,
    modifier: Modifier
) {
    Surface(modifier = modifier) {
        Stack(Modifier.fillMaxSize()) {
            if (episode.tmdbBackdropPath != null) {
                CoilImageWithCrossfade(
                    data = episode,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.gravity(Alignment.BottomStart)
                    .background(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(topRight = 8.dp)
                    )
                    .padding(all = 16.dp)
            ) {
                val type = MaterialTheme.typography
                val epNumber = episode.number
                val seasonNumber = season.number

                Providers(ContentColorAmbient provides Color.White) {
                    if (seasonNumber != null && epNumber != null) {
                        ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                            @Suppress("DEPRECATION")
                            val locale = ConfigurationAmbient.current.locale
                            Text(
                                text = stringResource(
                                    R.string.season_episode_number,
                                    seasonNumber, epNumber
                                ).toUpperCase(locale),
                                style = MaterialTheme.typography.overline
                                    .copy(color = contentColor())
                            )
                        }
                        Spacer(modifier = Modifier.preferredHeight(4.dp))
                    }

                    ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
                        Text(
                            text = episode.title ?: "No title",
                            style = type.h6.copy(color = contentColor())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPanes(episode: Episode) {
    Row {
        episode.traktRating?.let { rating ->
            InfoPane(
                icon = Icons.Default.Star,
                label = stringResource(R.string.trakt_rating_text, rating * 10f),
                modifier = Modifier.weight(1f)
            )
        }

        episode.firstAired?.let { firstAired ->
            val formatter = TiviDateFormatterAmbient.current
            val deferredIcon = loadVectorResource(id = R.drawable.ic_calendar_today)
            deferredIcon.onLoadRun { asset ->
                InfoPane(
                    icon = asset,
                    label = formatter.formatShortRelativeTime(firstAired),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoPane(
    modifier: Modifier = Modifier,
    icon: VectorAsset,
    label: String
) {
    Column(modifier = modifier.padding(all = 16.dp)) {
        ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
            Icon(
                asset = icon,
                modifier = Modifier.gravity(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.preferredHeight(4.dp))

        ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
            Text(
                modifier = Modifier.gravity(Alignment.CenterHorizontally),
                text = label,
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Composable
private fun EpisodeWatchesHeader(onSweepWatchesClick: () -> Unit) {
    Row {
        ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    .gravity(Alignment.CenterVertically)
                    .weight(1f),
                text = stringResource(R.string.episode_watches),
                style = MaterialTheme.typography.subtitle1
            )
        }

        ProvideEmphasis(EmphasisAmbient.current.disabled) {
            IconButton(
                modifier = Modifier.padding(end = 4.dp),
                onClick = { onSweepWatchesClick() }
            ) {
                IconResource(R.drawable.ic_delete_sweep)
            }
        }
    }
}

@Composable
private fun EpisodeWatch(episodeWatchEntry: EpisodeWatchEntry) {
    Surface {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                .preferredSizeIn(minWidth = 40.dp, minHeight = 40.dp)
        ) {
            ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
                val formatter = TiviDateFormatterAmbient.current
                Text(
                    modifier = Modifier.weight(1f).gravity(Alignment.CenterVertically),
                    text = formatter.formatMediumDateTime(episodeWatchEntry.watchedAt),
                    style = MaterialTheme.typography.body2
                )
            }

            ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
                if (episodeWatchEntry.pendingAction != PendingAction.NOTHING) {
                    IconResource(
                        resourceId = R.drawable.ic_publish,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .gravity(Alignment.CenterVertically)
                    )
                }

                if (episodeWatchEntry.pendingAction == PendingAction.DELETE) {
                    IconResource(
                        resourceId = R.drawable.ic_visibility_off,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .gravity(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

private val color = ColorPropKey()

@Composable
private fun EpisodeWatchSwipeBackground(
    swipeProgress: Float,
    wouldCompleteOnRelease: Boolean = false
) {
    var iconCenter by rememberMutableState { Offset(0f, 0f) }

    val maxRadius = remember(iconCenter) {
        hypot(iconCenter.x.toDouble(), iconCenter.y.toDouble())
    }

    // Note: can't reference these directly in transitionDefinition {} as
    // it's not @Composable
    val secondary = MaterialTheme.colors.error.copy(alpha = 0.5f)
    val default = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)

    val transition = remember(secondary, default) {
        transitionDefinition<Boolean> {
            state(true) {
                this[color] = secondary
            }
            state(false) {
                this[color] = default
            }

            transition {
                color using tween(durationMillis = 200)
            }
        }
    }

    val transitionState = transition(
        definition = transition,
        toState = wouldCompleteOnRelease
    )

    Stack(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colors.onSurface.copy(alpha = 0.2f), RectangleShape)
    ) {
        // A simple box to draw the growing circle, which emanates from behind the icon
        Spacer(
            modifier = Modifier.fillMaxSize()
                .drawGrowingCircle(
                    color = transitionState[color],
                    center = iconCenter,
                    radius = lerp(0f, maxRadius.toFloat(), FastOutSlowInEasing(swipeProgress))
                )
        )

        ProvideEmphasis(emphasis = EmphasisAmbient.current.medium) {
            Icon(
                asset = Icons.Default.Delete,
                modifier = Modifier
                    .onPositionInParentChanged { iconCenter = it.boundsInParent.center }
                    .padding(start = 0.dp, top = 0.dp, end = 16.dp, bottom = 0.dp)
                    .gravity(Alignment.CenterEnd)
            )
        }
    }
}

private fun Modifier.drawGrowingCircle(
    color: Color,
    center: Offset,
    radius: Float
) = drawWithContent {
    drawContent()

    clipRect {
        drawCircle(
            color = color,
            radius = radius,
            center = center
        )
    }
}

@Composable
fun MarkWatchedButton(
    modifier: Modifier = Modifier,
    actioner: (EpisodeDetailsAction) -> Unit
) {
    Button(
        modifier = modifier,
        elevation = if (Build.VERSION.SDK_INT != 28) 2.dp else 0.dp, // b/152696056
        onClick = { actioner(AddEpisodeWatchAction) }
    ) {
        ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
            Text(
                text = stringResource(R.string.episode_mark_watched),
                style = MaterialTheme.typography.button.copy(color = contentColor())
            )
        }
    }
}

@Composable
fun AddWatchButton(
    modifier: Modifier = Modifier,
    actioner: (EpisodeDetailsAction) -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = { actioner(AddEpisodeWatchAction) }
    ) {
        ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
            Text(
                text = stringResource(R.string.episode_add_watch),
                style = MaterialTheme.typography.button.copy(color = contentColor())
            )
        }
    }
}

@Composable
private fun RemoveAllWatchesDialog(
    actioner: (EpisodeDetailsAction) -> Unit,
    onDialogClosed: () -> Unit
) {
    TiviAlertDialog(
        title = stringResource(R.string.episode_remove_watches_dialog_title),
        message = stringResource(R.string.episode_remove_watches_dialog_message),
        confirmText = stringResource(R.string.episode_remove_watches_dialog_confirm),
        onConfirm = {
            actioner(RemoveAllEpisodeWatchesAction)
            onDialogClosed()
        },
        dismissText = stringResource(R.string.dialog_dismiss),
        onDismissRequest = { onDialogClosed() }
    )
}

@Composable
private fun EpisodeDetailsAppBar(
    backgroundColor: Color,
    isRefreshing: Boolean,
    actioner: (EpisodeDetailsAction) -> Unit,
    elevation: Dp,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = { actioner(Close) }) {
                Icon(Icons.Default.Close)
            }
        },
        actions = {
            if (isRefreshing) {
                AutoSizedCircularProgressIndicator(
                    modifier = Modifier.aspectRatio(1f)
                        .fillMaxHeight()
                        .padding(14.dp)
                )
            } else {
                IconButton(onClick = { actioner(RefreshAction) }) {
                    Icon(Icons.Default.Refresh)
                }
            }
        },
        elevation = elevation,
        backgroundColor = backgroundColor,
        modifier = modifier
    )
}

@Preview
@Composable
fun previewEpisodeDetails() = EpisodeDetails(
    viewState = EpisodeDetailsViewState(
        episodeId = 0,
        episode = Episode(
            seasonId = 100,
            title = "A show too far",
            summary = "A long description of a episode",
            traktRating = 0.5f,
            traktRatingVotes = 84,
            firstAired = OffsetDateTime.now()
        ),
        season = Season(
            id = 100,
            showId = 0
        ),
        watches = listOf(
            EpisodeWatchEntry(
                id = 10,
                episodeId = 100,
                watchedAt = OffsetDateTime.now()
            )
        )
    ),
    actioner = {}
)
