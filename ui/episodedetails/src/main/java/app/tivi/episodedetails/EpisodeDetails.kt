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

@file:OptIn(ExperimentalMaterialNavigationApi::class)

package app.tivi.episodedetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.common.compose.ui.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ui.Backdrop
import app.tivi.common.compose.ui.ExpandingText
import app.tivi.common.compose.ui.ScrimmedIconButton
import app.tivi.common.compose.ui.TiviAlertDialog
import app.tivi.common.compose.ui.boundsInParent
import app.tivi.common.compose.ui.none
import app.tivi.common.compose.ui.onPositionInParentChanged
import app.tivi.common.ui.resources.R as UiR
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Season
import app.tivi.ui.animations.lerp
import com.google.accompanist.navigation.material.BottomSheetNavigatorSheetState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlin.math.absoluteValue
import kotlin.math.hypot
import org.threeten.bp.OffsetDateTime

@ExperimentalMaterialApi
@Composable
fun EpisodeDetails(
    sheetState: BottomSheetNavigatorSheetState,
    navigateUp: () -> Unit,
) {
    EpisodeDetails(
        viewModel = hiltViewModel(),
        sheetState = sheetState,
        navigateUp = navigateUp,
    )
}

@ExperimentalMaterialApi
@Composable
internal fun EpisodeDetails(
    viewModel: EpisodeDetailsViewModel,
    sheetState: BottomSheetNavigatorSheetState,
    navigateUp: () -> Unit,
) {
    val viewState by viewModel.state.collectAsState()

    EpisodeDetails(
        viewState = viewState,
        sheetState = sheetState,
        navigateUp = navigateUp,
        refresh = viewModel::refresh,
        onRemoveAllWatches = viewModel::removeAllWatches,
        onRemoveWatch = viewModel::removeWatchEntry,
        onAddWatch = viewModel::addWatch,
        onMessageShown = viewModel::clearMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterialApi
@Composable
internal fun EpisodeDetails(
    viewState: EpisodeDetailsViewState,
    sheetState: BottomSheetNavigatorSheetState,
    navigateUp: () -> Unit,
    refresh: () -> Unit,
    onRemoveAllWatches: () -> Unit,
    onRemoveWatch: (id: Long) -> Unit,
    onAddWatch: () -> Unit,
    onMessageShown: (id: Long) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val dismissSnackbarState = rememberDismissState(
        confirmValueChange = { value ->
            if (value != DismissValue.Default) {
                snackbarHostState.currentSnackbarData?.dismiss()
                true
            } else {
                false
            }
        },
    )

    viewState.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message.message)
            // Notify the view model that the message has been dismissed
            onMessageShown(message.id)
        }
    }

    Scaffold(
        topBar = {
            Surface {
                if (viewState.episode != null && viewState.season != null) {
                    EpisodeDetailsBackdrop(
                        season = viewState.season,
                        episode = viewState.episode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f),
                    )
                }

                Column {
                    AnimatedVisibility(
                        visible = sheetState.targetValue == ModalBottomSheetValue.Expanded,
                    ) {
                        Spacer(
                            Modifier
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f))
                                .windowInsetsTopHeight(WindowInsets.statusBars)
                                .fillMaxWidth(),
                        )
                    }

                    EpisodeDetailsAppBar(
                        // backgroundColor = Color.Transparent,
                        isRefreshing = viewState.refreshing,
                        navigateUp = navigateUp,
                        refresh = refresh,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                SwipeToDismiss(
                    state = dismissSnackbarState,
                    background = {},
                    dismissContent = { Snackbar(snackbarData = data) },
                    modifier = Modifier
                        .padding(horizontal = Layout.bodyMargin)
                        .fillMaxWidth(),
                )
            }
        },
        modifier = Modifier
            .testTag("episode_details"),
    ) { contentPadding ->
        Surface(
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding),
            ) {
                val episode = viewState.episode
                if (episode != null) {
                    InfoPanes(episode)

                    ExpandingText(
                        text = episode.summary ?: "No summary",
                        modifier = Modifier.padding(16.dp),
                    )
                }

                if (viewState.canAddEpisodeWatch) {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (viewState.watches.isEmpty()) {
                        MarkWatchedButton(
                            onClick = onAddWatch,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    } else {
                        AddWatchButton(
                            onClick = onAddWatch,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (viewState.watches.isNotEmpty()) {
                    var openDialog by remember { mutableStateOf(false) }

                    EpisodeWatchesHeader(
                        onSweepWatchesClick = { openDialog = true },
                    )

                    if (openDialog) {
                        RemoveAllWatchesDialog(
                            onConfirm = {
                                onRemoveAllWatches()
                                openDialog = false
                            },
                            onDismiss = { openDialog = false },
                        )
                    }
                }

                viewState.watches.forEach { watch ->
                    key(watch.id) {
                        val dismissState = rememberDismissState(
                            confirmValueChange = { value ->
                                if (value != DismissValue.Default) {
                                    onRemoveWatch(watch.id)
                                    true
                                } else {
                                    false
                                }
                            },
                        )

                        SwipeToDismiss(
                            state = dismissState,
                            modifier = Modifier.padding(vertical = 4.dp),
                            directions = setOf(DismissDirection.EndToStart),
                            background = {
                                val fraction = dismissState.progress
                                EpisodeWatchSwipeBackground(
                                    swipeProgress = fraction,
                                    wouldCompleteOnRelease = fraction.absoluteValue >= 0.5f,
                                )
                            },
                            dismissContent = { EpisodeWatch(episodeWatchEntry = watch) },
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun EpisodeDetailsBackdrop(
    season: Season,
    episode: Episode,
    modifier: Modifier = Modifier,
) {
    TiviTheme(useDarkColors = true) {
        Backdrop(
            imageModel = if (episode.tmdbBackdropPath != null) episode else null,
            shape = RectangleShape,
            overline = {
                val epNumber = episode.number
                val seasonNumber = season.number
                if (seasonNumber != null && epNumber != null) {
                    @Suppress("DEPRECATION")
                    Text(
                        text = stringResource(
                            UiR.string.season_episode_number,
                            seasonNumber,
                            epNumber,
                        ).uppercase(LocalConfiguration.current.locale),
                    )
                }
            },
            title = { Text(text = episode.title ?: "No title") },
            modifier = modifier,
        )
    }
}

@Composable
private fun InfoPanes(episode: Episode) {
    Row {
        episode.traktRating?.let { rating ->
            InfoPane(
                imageVector = Icons.Default.Star,
                label = stringResource(UiR.string.trakt_rating_text, rating * 10f),
                contentDescription = stringResource(UiR.string.cd_trakt_rating, rating * 10f),
                modifier = Modifier.weight(1f),
            )
        }

        episode.firstAired?.let { firstAired ->
            val formatter = LocalTiviDateFormatter.current
            val formattedDate = formatter.formatShortRelativeTime(firstAired)
            InfoPane(
                imageVector = Icons.Default.CalendarToday,
                label = formattedDate,
                contentDescription = stringResource(
                    UiR.string.cd_episode_first_aired,
                    formattedDate,
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun InfoPane(
    imageVector: ImageVector,
    contentDescription: String?,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = label,
            style = MaterialTheme.typography.bodyLarge,
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
            text = stringResource(UiR.string.episode_watches),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.weight(1f))
        IconButton(
            modifier = Modifier.padding(end = 4.dp),
            onClick = { onSweepWatchesClick() },
        ) {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = stringResource(UiR.string.cd_delete),
            )
        }
    }
}

@Composable
private fun EpisodeWatch(episodeWatchEntry: EpisodeWatchEntry) {
    Surface {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .sizeIn(minWidth = 40.dp, minHeight = 40.dp),
        ) {
            val formatter = LocalTiviDateFormatter.current
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = formatter.formatMediumDateTime(episodeWatchEntry.watchedAt),
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.weight(1f))

            if (episodeWatchEntry.pendingAction != PendingAction.NOTHING) {
                Icon(
                    imageVector = Icons.Default.Publish,
                    contentDescription = stringResource(UiR.string.cd_episode_syncing),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically),
                )
            }

            if (episodeWatchEntry.pendingAction == PendingAction.DELETE) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = stringResource(UiR.string.cd_episode_deleted),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically),
                )
            }
        }
    }
}

@Composable
private fun EpisodeWatchSwipeBackground(
    swipeProgress: Float,
    wouldCompleteOnRelease: Boolean = false,
) {
    var iconCenter by remember { mutableStateOf(Offset(0f, 0f)) }
    val maxRadius = hypot(iconCenter.x.toDouble(), iconCenter.y.toDouble())

    val secondary = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
    val default = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RectangleShape),
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
                        },
                    ).value,
                    center = iconCenter,
                    radius = lerp(
                        startValue = 0f,
                        endValue = maxRadius.toFloat(),
                        fraction = FastOutLinearInEasing.transform(swipeProgress),
                    ),
                ),
        )

        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(UiR.string.cd_delete),
            modifier = Modifier
                .onPositionInParentChanged { iconCenter = it.boundsInParent.center }
                .padding(start = 0.dp, top = 0.dp, end = 16.dp, bottom = 0.dp)
                .align(Alignment.CenterEnd),
        )
    }
}

private fun Modifier.drawGrowingCircle(
    color: Color,
    center: Offset,
    radius: Float,
) = drawWithContent {
    drawContent()

    clipRect {
        drawCircle(
            color = color,
            radius = radius,
            center = center,
        )
    }
}

@Composable
private fun MarkWatchedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(text = stringResource(UiR.string.episode_mark_watched))
    }
}

@Composable
private fun AddWatchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(text = stringResource(UiR.string.episode_add_watch))
    }
}

@Composable
private fun RemoveAllWatchesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    TiviAlertDialog(
        title = stringResource(UiR.string.episode_remove_watches_dialog_title),
        message = stringResource(UiR.string.episode_remove_watches_dialog_message),
        confirmText = stringResource(UiR.string.episode_remove_watches_dialog_confirm),
        onConfirm = { onConfirm() },
        dismissText = stringResource(UiR.string.dialog_dismiss),
        onDismissRequest = { onDismiss() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodeDetailsAppBar(
    isRefreshing: Boolean,
    navigateUp: () -> Unit,
    refresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            actionIconContentColor = LocalContentColor.current,
        ),
        title = {},
        navigationIcon = {
            ScrimmedIconButton(showScrim = true, onClick = navigateUp) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(UiR.string.cd_close),
                )
            }
        },
        actions = {
            if (isRefreshing) {
                AutoSizedCircularProgressIndicator(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxHeight()
                        .padding(14.dp),
                )
            } else {
                ScrimmedIconButton(showScrim = true, onClick = refresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(UiR.string.cd_refresh),
                    )
                }
            }
        },
        windowInsets = WindowInsets.none,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun PreviewEpisodeDetails() {
    EpisodeDetails(
        viewState = EpisodeDetailsViewState(
            episode = Episode(
                seasonId = 100,
                title = "A show too far",
                summary = "A long description of a episode",
                traktRating = 0.5f,
                traktRatingVotes = 84,
                firstAired = OffsetDateTime.now(),
            ),
            season = Season(
                id = 100,
                showId = 0,
            ),
            watches = listOf(
                EpisodeWatchEntry(
                    id = 10,
                    episodeId = 100,
                    watchedAt = OffsetDateTime.now(),
                ),
            ),
        ),
        sheetState = BottomSheetNavigatorSheetState(
            ModalBottomSheetState(ModalBottomSheetValue.HalfExpanded),
        ),
        navigateUp = {},
        refresh = {},
        onRemoveAllWatches = {},
        onRemoveWatch = {},
        onAddWatch = {},
        onMessageShown = {},
    )
}
