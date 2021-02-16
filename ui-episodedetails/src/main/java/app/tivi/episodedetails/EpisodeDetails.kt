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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ExpandingText
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.SwipeDismissSnackbar
import app.tivi.common.compose.TiviAlertDialog
import app.tivi.common.compose.boundsInParent
import app.tivi.common.compose.onPositionInParentChanged
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Season
import app.tivi.ui.animations.lerp
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.insets.navigationBarsHeight
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import org.threeten.bp.OffsetDateTime
import kotlin.math.absoluteValue
import kotlin.math.hypot

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EpisodeDetails(
    viewState: EpisodeDetailsViewState,
    actioner: (EpisodeDetailsAction) -> Unit
) {
    Box {
        Column {
            Surface {
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
                    elevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                )
            }
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Surface(elevation = 2.dp) {
                    Column {
                        val episode = viewState.episode
                        if (episode != null) {
                            InfoPanes(episode)

                            ExpandingText(
                                text = episode.summary ?: "No summary",
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        val watches = viewState.watches

                        if (viewState.canAddEpisodeWatch) {
                            Spacer(modifier = Modifier.height(8.dp))

                            if (watches.isEmpty()) {
                                MarkWatchedButton(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    actioner = actioner
                                )
                            } else {
                                AddWatchButton(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    actioner = actioner
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (watches.isNotEmpty()) {
                            var openDialog by remember { mutableStateOf(false) }

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
                            key(watch.id) {
                                val dismissState = rememberDismissState {
                                    if (it != DismissValue.Default) {
                                        actioner(RemoveEpisodeWatchAction(watch.id))
                                    }
                                    it != DismissValue.DismissedToEnd
                                }

                                SwipeToDismiss(
                                    state = dismissState,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    directions = setOf(DismissDirection.EndToStart),
                                    background = {
                                        val fraction = dismissState.progress.fraction
                                        EpisodeWatchSwipeBackground(
                                            swipeProgress = fraction,
                                            wouldCompleteOnRelease = fraction.absoluteValue >= 0.5f
                                        )
                                    },
                                    dismissContent = { EpisodeWatch(episodeWatchEntry = watch) }
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Spacer(Modifier.navigationBarsHeight())
                    }
                }
            }
        }

        val snackbarHostState = remember { SnackbarHostState() }

        SnackbarHost(
            hostState = snackbarHostState,
            snackbar = {
                SwipeDismissSnackbar(
                    data = it,
                    onDismiss = { actioner(ClearError) }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        )

        LaunchedEffect(viewState.error) {
            viewState.error?.let { error ->
                snackbarHostState.showSnackbar(error.message)
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
        Box(Modifier.fillMaxSize()) {
            if (episode.tmdbBackdropPath != null) {
                CoilImage(
                    data = episode,
                    fadeIn = true,
                    contentDescription = stringResource(R.string.cd_show_poster),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(topEnd = 8.dp)
                    )
                    .padding(all = 16.dp)
            ) {
                val epNumber = episode.number
                val seasonNumber = season.number

                CompositionLocalProvider(LocalContentColor provides Color.White) {
                    if (seasonNumber != null && epNumber != null) {
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            @Suppress("DEPRECATION")
                            val locale = LocalConfiguration.current.locale
                            Text(
                                text = stringResource(
                                    R.string.season_episode_number,
                                    seasonNumber,
                                    epNumber
                                ).toUpperCase(locale),
                                style = MaterialTheme.typography.overline
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = episode.title ?: "No title",
                        style = MaterialTheme.typography.h6
                    )
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
                contentDescription = stringResource(R.string.cd_trakt_rating, rating * 10f),
                modifier = Modifier.weight(1f)
            )
        }

        episode.firstAired?.let { firstAired ->
            val formatter = LocalTiviDateFormatter.current
            val formattedDate = formatter.formatShortRelativeTime(firstAired)
            InfoPane(
                painter = painterResource(R.drawable.ic_calendar_today),
                label = formattedDate,
                contentDescription = stringResource(
                    R.string.cd_episode_first_aired,
                    formattedDate
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoPane(
    icon: ImageVector,
    contentDescription: String?,
    label: String,
    modifier: Modifier = Modifier,
) {
    InfoPane(
        painter = rememberVectorPainter(image = icon),
        contentDescription = contentDescription,
        label = label,
        modifier = modifier
    )
}

@Composable
private fun InfoPane(
    painter: Painter,
    contentDescription: String?,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(all = 16.dp)) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = label,
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
private fun EpisodeWatchesHeader(onSweepWatchesClick: () -> Unit) {
    Row {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.CenterVertically),
            text = stringResource(R.string.episode_watches),
            style = MaterialTheme.typography.subtitle1
        )

        Spacer(Modifier.weight(1f))

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
            IconButton(
                modifier = Modifier.padding(end = 4.dp),
                onClick = { onSweepWatchesClick() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete_sweep),
                    contentDescription = stringResource(R.string.cd_delete),
                )
            }
        }
    }
}

@Composable
private fun EpisodeWatch(episodeWatchEntry: EpisodeWatchEntry) {
    Surface {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .sizeIn(minWidth = 40.dp, minHeight = 40.dp)
        ) {
            val formatter = LocalTiviDateFormatter.current
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = formatter.formatMediumDateTime(episodeWatchEntry.watchedAt),
                style = MaterialTheme.typography.body2
            )

            Spacer(Modifier.weight(1f))

            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                if (episodeWatchEntry.pendingAction != PendingAction.NOTHING) {
                    Icon(
                        painter = painterResource(R.drawable.ic_publish),
                        contentDescription = stringResource(R.string.cd_episode_syncing),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .align(Alignment.CenterVertically),
                    )
                }

                if (episodeWatchEntry.pendingAction == PendingAction.DELETE) {
                    Icon(
                        painter = painterResource(R.drawable.ic_visibility_off),
                        contentDescription = stringResource(R.string.cd_episode_deleted),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .align(Alignment.CenterVertically),
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeWatchSwipeBackground(
    swipeProgress: Float,
    wouldCompleteOnRelease: Boolean = false
) {
    var iconCenter by remember { mutableStateOf(Offset(0f, 0f)) }
    val maxRadius = hypot(iconCenter.x.toDouble(), iconCenter.y.toDouble())

    val secondary = MaterialTheme.colors.error.copy(alpha = 0.5f)
    val default = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.onSurface.copy(alpha = 0.2f), RectangleShape)
    ) {
        // A simple box to draw the growing circle, which emanates from behind the icon
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawGrowingCircle(
                    color = animateColorAsState(
                        targetValue = when (wouldCompleteOnRelease) {
                            true -> secondary
                            false -> default
                        }
                    ).value,
                    center = iconCenter,
                    radius = lerp(
                        startValue = 0f,
                        endValue = maxRadius.toFloat(),
                        fraction = FastOutLinearInEasing.transform(swipeProgress)
                    )
                )
        )

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.cd_delete),
                modifier = Modifier
                    .onPositionInParentChanged { iconCenter = it.boundsInParent.center }
                    .padding(start = 0.dp, top = 0.dp, end = 16.dp, bottom = 0.dp)
                    .align(Alignment.CenterEnd)
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
        onClick = { actioner(AddEpisodeWatchAction) }
    ) {
        Text(
            text = stringResource(R.string.episode_mark_watched),
            style = MaterialTheme.typography.button.copy(color = LocalContentColor.current)
        )
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
        Text(text = stringResource(R.string.episode_add_watch))
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
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cd_close),
                )
            }
        },
        actions = {
            if (isRefreshing) {
                AutoSizedCircularProgressIndicator(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxHeight()
                        .padding(14.dp)
                )
            } else {
                IconButton(onClick = { actioner(RefreshAction) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.cd_refresh)
                    )
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
fun PreviewEpisodeDetails() {
    EpisodeDetails(
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
}
