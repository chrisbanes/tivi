/*
 * Copyright 2023 Google LLC
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
@file:Suppress("UNUSED_PARAMETER")

package app.tivi.episode.track

import app.tivi.common.ui.resources.R as UiR
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.common.compose.ui.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ui.Backdrop
import app.tivi.common.compose.ui.ExpandingText
import app.tivi.common.compose.ui.ScrimmedIconButton
import app.tivi.common.compose.ui.none
import app.tivi.common.compose.viewModel
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import com.google.accompanist.navigation.material.BottomSheetNavigatorSheetState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlin.math.roundToInt
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

typealias EpisodeTrack = @Composable (
    sheetState: BottomSheetNavigatorSheetState,
    navigateUp: () -> Unit,
) -> Unit

@OptIn(ExperimentalMaterialApi::class)
@Inject
@Composable
fun EpisodeTrack(
    viewModelFactory: (SavedStateHandle) -> EpisodeTrackViewModel,
    @Assisted sheetState: BottomSheetNavigatorSheetState,
    @Assisted navigateUp: () -> Unit,
) {
    EpisodeTrack(
        viewModel = viewModel(factory = viewModelFactory),
        sheetState = sheetState,
        navigateUp = navigateUp,
    )
}

@ExperimentalMaterialApi
@Composable
internal fun EpisodeTrack(
    viewModel: EpisodeTrackViewModel,
    sheetState: BottomSheetNavigatorSheetState,
    navigateUp: () -> Unit,
) {
    val viewState by viewModel.state.collectAsState()

    EpisodeTrack(
        viewState = viewState,
        sheetState = sheetState,
        navigateUp = navigateUp,
        refresh = viewModel::refresh,
        onAddWatch = viewModel::addWatch,
        onMessageShown = viewModel::clearMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterialApi
@Composable
internal fun EpisodeTrack(
    viewState: EpisodeTrackViewState,
    sheetState: BottomSheetNavigatorSheetState,
    navigateUp: () -> Unit,
    refresh: () -> Unit,
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

    // I don't love this, but it's the only way currently to know where the modal sheet
    // is laid out. https://issuetracker.google.com/issues/209825720
    var bottomSheetY by remember { mutableStateOf(0) }

    Surface(
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("episode_details")
            .onGloballyPositioned { coords ->
                bottomSheetY = coords.positionInWindow().y.roundToInt()
            },
    ) {
        Column {
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
                    val showScrim = sheetState.targetValue == ModalBottomSheetValue.Expanded &&
                        bottomSheetY <= WindowInsets.statusBars.getBottom(LocalDensity.current)

                    AnimatedVisibility(visible = showScrim) {
                        Spacer(
                            Modifier
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f))
                                .windowInsetsTopHeight(WindowInsets.statusBars)
                                .fillMaxWidth(),
                        )
                    }

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
