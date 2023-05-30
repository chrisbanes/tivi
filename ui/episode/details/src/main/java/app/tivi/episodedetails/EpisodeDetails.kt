// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.episodedetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.common.compose.ui.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ui.Backdrop
import app.tivi.common.compose.ui.ExpandingText
import app.tivi.common.compose.ui.ScrimmedIconButton
import app.tivi.common.compose.ui.TiviAlertDialog
import app.tivi.common.compose.ui.none
import app.tivi.common.ui.resources.MR
import app.tivi.data.imagemodels.asImageModel
import app.tivi.data.models.Episode
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.PendingAction
import app.tivi.data.models.Season
import app.tivi.overlays.showInBottomSheet
import app.tivi.screens.EpisodeDetailsScreen
import app.tivi.screens.EpisodeTrackScreen
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

@Inject
class EpisodeDetailsUiFactory : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
        is EpisodeDetailsScreen -> {
            ui<EpisodeDetailsUiState> { state, modifier ->
                EpisodeDetails(state, modifier)
            }
        }

        else -> null
    }
}

@Composable
internal fun EpisodeDetails(
    viewState: EpisodeDetailsUiState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val overlayHost = LocalOverlayHost.current

    EpisodeDetails(
        viewState = viewState,
        navigateUp = { viewState.eventSink(EpisodeDetailsUiEvent.NavigateUp) },
        refresh = { viewState.eventSink(EpisodeDetailsUiEvent.Refresh(true)) },
        onRemoveAllWatches = { viewState.eventSink(EpisodeDetailsUiEvent.RemoveAllWatches) },
        onRemoveWatch = { id -> viewState.eventSink(EpisodeDetailsUiEvent.RemoveWatchEntry(id)) },
        onAddWatch = {
            scope.launch {
                overlayHost.showInBottomSheet(EpisodeTrackScreen(viewState.episode!!.id))
            }
        },
        onMessageShown = { id -> viewState.eventSink(EpisodeDetailsUiEvent.ClearMessage(id)) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EpisodeDetails(
    viewState: EpisodeDetailsUiState,
    navigateUp: () -> Unit,
    refresh: () -> Unit,
    onRemoveAllWatches: () -> Unit,
    onRemoveWatch: (id: Long) -> Unit,
    onAddWatch: () -> Unit,
    onMessageShown: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("episode_details"),
    ) {
        Column {
            Surface {
                if (viewState.episode != null && viewState.season != null) {
                    EpisodeDetailsBackdrop(
                        season = viewState.season,
                        episode = viewState.episode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 10f),
                    )
                }

                Column {
                    Spacer(
                        Modifier
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.35f))
                            .windowInsetsTopHeight(WindowInsets.statusBars)
                            .fillMaxWidth(),
                    )

                    EpisodeDetailsAppBar(
                        isRefreshing = viewState.refreshing,
                        navigateUp = navigateUp,
                        refresh = refresh,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
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
                                EpisodeWatchSwipeBackground(
                                    targetValue = dismissState.targetValue,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            },
                            dismissContent = {
                                EpisodeWatch(episodeWatchEntry = watch)
                            },
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }

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
            imageModel = episode.asImageModel(),
            shape = RectangleShape,
            overline = {
                val epNumber = episode.number
                val seasonNumber = season.number
                if (seasonNumber != null && epNumber != null) {
                    @Suppress("DEPRECATION")
                    Text(
                        text = stringResource(
                            MR.strings.season_episode_number,
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
                label = stringResource(MR.strings.trakt_rating_text, rating * 10f),
                contentDescription = stringResource(MR.strings.cd_trakt_rating, rating * 10f),
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
                    MR.strings.cd_episode_first_aired,
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
            text = stringResource(MR.strings.episode_watches),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.weight(1f))
        IconButton(
            modifier = Modifier.padding(end = 4.dp),
            onClick = { onSweepWatchesClick() },
        ) {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = stringResource(MR.strings.cd_delete),
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val formatter = LocalTiviDateFormatter.current
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = formatter.formatMediumDateTime(episodeWatchEntry.watchedAt),
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(episodeWatchEntry.pendingAction != PendingAction.NOTHING) {
                Icon(
                    imageVector = Icons.Default.Publish,
                    contentDescription = stringResource(MR.strings.cd_episode_syncing),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            AnimatedVisibility(episodeWatchEntry.pendingAction == PendingAction.DELETE) {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = stringResource(MR.strings.cd_episode_deleted),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodeWatchSwipeBackground(
    targetValue: DismissValue,
    modifier: Modifier = Modifier,
) {
    val error = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
    val circleOverlayColor by animateColorAsState(
        targetValue = when (targetValue != DismissValue.Default) {
            true -> error
            false -> Color.Transparent
        },
    )

    Surface(
        tonalElevation = 4.dp,
        modifier = modifier,
    ) {
        Box(Modifier.fillMaxSize()) {
            Spacer(
                modifier = Modifier
                    .background(circleOverlayColor)
                    .matchParentSize(),
            )

            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(MR.strings.cd_delete),
                modifier = Modifier
                    .padding(12.dp)
                    .padding(end = 8.dp)
                    .align(Alignment.CenterEnd),
            )
        }
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
        Text(text = stringResource(MR.strings.episode_mark_watched))
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
        Text(text = stringResource(MR.strings.episode_add_watch))
    }
}

@Composable
private fun RemoveAllWatchesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    TiviAlertDialog(
        title = stringResource(MR.strings.episode_remove_watches_dialog_title),
        message = stringResource(MR.strings.episode_remove_watches_dialog_message),
        confirmText = stringResource(MR.strings.episode_remove_watches_dialog_confirm),
        onConfirm = { onConfirm() },
        dismissText = stringResource(MR.strings.dialog_dismiss),
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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(MR.strings.cd_navigate_up),
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
                        contentDescription = stringResource(MR.strings.cd_refresh),
                    )
                }
            }
        },
        windowInsets = WindowInsets.none,
        modifier = modifier,
    )
}

@Preview
@Composable
fun PreviewEpisodeDetails() {
    EpisodeDetails(
        viewState = EpisodeDetailsUiState(
            episode = Episode(
                seasonId = 100,
                title = "A show too far",
                summary = "A long description of a episode",
                traktRating = 0.5f,
                traktRatingVotes = 84,
                firstAired = Clock.System.now(),
            ),
            season = Season(
                id = 100,
                showId = 0,
            ),
            watches = listOf(
                EpisodeWatchEntry(
                    id = 10,
                    episodeId = 100,
                    watchedAt = Clock.System.now(),
                ),
            ),
            eventSink = {},
        ),
        navigateUp = {},
        refresh = {},
        onRemoveAllWatches = {},
        onRemoveWatch = {},
        onAddWatch = {},
        onMessageShown = {},
    )
}
