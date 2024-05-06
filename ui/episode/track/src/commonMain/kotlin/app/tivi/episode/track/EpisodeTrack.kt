// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.episode.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.ui.AsyncImage
import app.tivi.common.compose.ui.DateTextField
import app.tivi.common.compose.ui.LoadingButton
import app.tivi.common.compose.ui.TimeTextField
import app.tivi.data.imagemodels.asImageModel
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import app.tivi.screens.EpisodeTrackScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import me.tatarka.inject.annotations.Inject

@Inject
class EpisodeTrackUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    is EpisodeTrackScreen -> {
      ui<EpisodeTrackUiState> { state, modifier ->
        EpisodeTrack(state, modifier)
      }
    }

    else -> null
  }
}

@Composable
internal fun EpisodeTrack(
  state: EpisodeTrackUiState,
  modifier: Modifier = Modifier,
) {
  // Need to extract the eventSink out to a local val, so that the Compose Compiler
  // treats it as stable. See: https://issuetracker.google.com/issues/256100927
  val eventSink = state.eventSink

  EpisodeTrack(
    viewState = state,
    onSubmit = { eventSink(EpisodeTrackUiEvent.Submit) },
    onNowSelected = { eventSink(EpisodeTrackUiEvent.SelectNow) },
    onFirstAiredSelected = { eventSink(EpisodeTrackUiEvent.SelectFirstAired) },
    onDateSelected = { eventSink(EpisodeTrackUiEvent.SelectDate(it)) },
    onTimeSelected = { eventSink(EpisodeTrackUiEvent.SelectTime(it)) },
    onMessageShown = { eventSink(EpisodeTrackUiEvent.ClearMessage(it)) },
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun EpisodeTrack(
  viewState: EpisodeTrackUiState,
  onSubmit: () -> Unit,
  onDateSelected: (LocalDate) -> Unit,
  onTimeSelected: (LocalTime) -> Unit,
  onNowSelected: () -> Unit,
  onFirstAiredSelected: () -> Unit,
  onMessageShown: (id: Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }

  val dismissSnackbarState = rememberDismissState { value ->
    if (value != DismissValue.Default) {
      snackbarHostState.currentSnackbarData?.dismiss()
      true
    } else {
      false
    }
  }

  viewState.message?.let { message ->
    LaunchedEffect(message) {
      snackbarHostState.showSnackbar(message.message)
      // Notify the view model that the message has been dismissed
      onMessageShown(message.id)
    }
  }

  Box(
    modifier = modifier
      .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
      .fillMaxWidth()
      .testTag("episode_track"),
  ) {
    Column {
      viewState.episode?.let { episode ->
        EpisodeHeader(
          episode = episode,
          season = viewState.season,
        )
      }
      HorizontalDivider()

      EpisodeTrack(
        selectedDate = viewState.selectedDate,
        selectedTime = viewState.selectedTime,
        onDateSelected = onDateSelected,
        onTimeSelected = onTimeSelected,
        showSetFirstAired = viewState.showSetFirstAired,
        onFirstAiredSelected = onFirstAiredSelected,
        onNowSelected = onNowSelected,
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
        model = remember(episode, episode::asImageModel),
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
  onDateSelected: (LocalDate) -> Unit,
  onTimeSelected: (LocalTime) -> Unit,
  showSetFirstAired: Boolean,
  onNowSelected: () -> Unit,
  onFirstAiredSelected: () -> Unit,
  submitInProgress: Boolean,
  canSubmitWatch: Boolean,
  submitWatch: () -> Unit,
  selectedDate: LocalDate? = null,
  selectedTime: LocalTime? = null,
) {
  Column(Modifier.padding(top = 16.dp)) {
    val strings = LocalStrings.current

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = strings.episodeTrackPrompt,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.weight(1f),
      )
    }

    Row(Modifier.padding(top = Layout.gutter)) {
      DateTextField(
        selectedDate = selectedDate,
        onDateSelected = onDateSelected,
        dialogTitle = strings.episodeWatchDateTitle,
        modifier = Modifier.fillMaxWidth(3 / 5f),
      )

      Spacer(Modifier.width(Layout.gutter))

      TimeTextField(
        selectedTime = selectedTime,
        onTimeSelected = onTimeSelected,
        dialogTitle = strings.episodeWatchTimeTitle,
        modifier = Modifier.weight(1f),
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      if (showSetFirstAired) {
        TextButton(onClick = onFirstAiredSelected) {
          Text(text = strings.episodeTrackSetFirstAired)
        }
      }

      TextButton(onClick = onNowSelected) {
        Text(text = strings.episodeTrackSetNow)
      }
    }

    Spacer(Modifier.padding(top = Layout.gutter))

    LoadingButton(
      showProgressIndicator = submitInProgress,
      enabled = canSubmitWatch,
      onClick = submitWatch,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(text = strings.episodeMarkWatched)
    }
  }
}
