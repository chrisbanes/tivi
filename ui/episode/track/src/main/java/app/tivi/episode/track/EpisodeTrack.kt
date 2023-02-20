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

@file:Suppress("UNUSED_PARAMETER")

package app.tivi.episode.track

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.ui.AsyncImage
import app.tivi.common.compose.ui.DateTextField
import app.tivi.common.compose.ui.LoadingButton
import app.tivi.common.compose.ui.TimeTextField
import app.tivi.common.compose.viewModel
import app.tivi.common.ui.resources.R
import app.tivi.data.imagemodels.asImageModel
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

typealias EpisodeTrack = @Composable (
    navigateUp: () -> Unit,
) -> Unit

@Inject
@Composable
fun EpisodeTrack(
    viewModelFactory: (SavedStateHandle) -> EpisodeTrackViewModel,
    @Assisted navigateUp: () -> Unit,
) {
    EpisodeTrack(
        viewModel = viewModel(factory = viewModelFactory),
        navigateUp = navigateUp,
    )
}

@Composable
internal fun EpisodeTrack(
    viewModel: EpisodeTrackViewModel,
    navigateUp: () -> Unit,
) {
    val viewState by viewModel.state.collectAsState()
    EpisodeTrack(
        viewState = viewState,
        navigateUp = navigateUp,
        refresh = viewModel::refresh,
        onSubmit = viewModel::submitWatch,
        onNowSelected = viewModel::selectNow,
        onSetFirstAired = viewModel::selectEpisodeFirstAired,
        onDateSelected = viewModel::selectDate,
        onTimeSelected = viewModel::selectTime,
        onMessageShown = viewModel::clearMessage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EpisodeTrack(
    viewState: EpisodeTrackViewState,
    navigateUp: () -> Unit,
    refresh: () -> Unit,
    onSubmit: () -> Unit,
    onNowSelected: (Boolean) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onSetFirstAired: () -> Unit,
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

    LaunchedEffect(viewState.shouldDismiss) {
        if (viewState.shouldDismiss) {
            navigateUp()
        }
    }

    Surface(
        shadowElevation = 2.dp,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("episode_track"),
    ) {
        Column(Modifier.padding(16.dp)) {
            viewState.episode?.let { episode ->
                EpisodeHeader(
                    episode = episode,
                    season = viewState.season,
                )
            }
            Divider()

            EpisodeTrack(
                selectedNow = viewState.selectedNow,
                selectedDate = viewState.selectedDate,
                selectedTime = viewState.selectedTime,
                onNowSelected = onNowSelected,
                onDateSelected = onDateSelected,
                onTimeSelected = onTimeSelected,
                showSetFirstAired = viewState.showSetFirstAired,
                onSetFirstAired = onSetFirstAired,
                submitInProgress = viewState.submitInProgress,
                canSubmitWatch = viewState.canSubmit,
                submitWatch = onSubmit,
            )
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
private fun EpisodeHeader(
    episode: Episode,
    season: Season?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.3f) // 30% of the width
                .aspectRatio(16 / 11f),
        ) {
            AsyncImage(
                model = episode.asImageModel(),
                requestBuilder = { crossfade(true) },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            val textCreator = LocalTiviTextCreator.current

            Text(
                text = episode.title
                    ?: textCreator.episodeNumberText(episode).toString(),
            )
            Text(
                text = textCreator.seasonEpisodeTitleText(
                    season = season,
                    episode = episode,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun EpisodeTrack(
    selectedNow: Boolean,
    onNowSelected: (Boolean) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    showSetFirstAired: Boolean,
    onSetFirstAired: () -> Unit,
    submitInProgress: Boolean,
    canSubmitWatch: Boolean,
    submitWatch: () -> Unit,
    selectedDate: LocalDate? = null,
    selectedTime: LocalTime? = null,
) {
    Column(Modifier.padding(top = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.episode_track_prompt),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = stringResource(R.string.episode_track_now),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = Layout.gutter),
            )

            Switch(
                checked = selectedNow,
                onCheckedChange = onNowSelected,
            )
        }

        AnimatedVisibility(visible = !selectedNow) {
            Column(Modifier.padding(top = Layout.gutter)) {
                Row {
                    DateTextField(
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected,
                        modifier = Modifier.fillMaxWidth(3 / 5f),
                    )

                    Spacer(Modifier.width(Layout.gutter))

                    TimeTextField(
                        selectedTime = selectedTime,
                        onTimeSelected = onTimeSelected,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (showSetFirstAired) {
                    TextButton(onClick = onSetFirstAired) {
                        Text(text = stringResource(R.string.episode_track_set_first_aired))
                    }
                }
            }
        }

        Spacer(Modifier.padding(top = Layout.gutter))

        LoadingButton(
            showProgressIndicator = submitInProgress,
            enabled = canSubmitWatch,
            onClick = submitWatch,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.episode_mark_watched))
        }
    }
}
