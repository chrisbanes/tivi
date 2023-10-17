// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.LocalTiviDateFormatter
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTextField(
  selectedDate: LocalDate?,
  onDateSelected: (LocalDate) -> Unit,
  dialogTitle: String,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    val dateFormatter = LocalTiviDateFormatter.current
    val formattedDate = remember(dateFormatter, selectedDate) {
      selectedDate?.let { dateFormatter.formatShortDate(it) }
    }

    var showDialog by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(selectedDate) }
    val lastOnDateSelected by rememberUpdatedState(onDateSelected)

    ClickableReadOnlyOutlinedTextField(
      value = formattedDate.orEmpty(),
      label = { Text(text = LocalStrings.current.dateLabel) },
      onClick = { showDialog = true },
      modifier = Modifier.fillMaxWidth(),
    )

    if (showDialog) {
      DatePickerDialog(
        onDismissRequest = { showDialog = false },
        selectedDate = selectedDate ?: remember {
          Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        },
        onDateChanged = {
          date = it
          lastOnDateSelected(it)
        },
        maximumDate = remember {
          Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        },
        title = dialogTitle,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTextField(
  selectedTime: LocalTime?,
  onTimeSelected: (LocalTime) -> Unit,
  dialogTitle: String,
  modifier: Modifier = Modifier,
  @Suppress("UNUSED_PARAMETER") is24Hour: Boolean = false,
) {
  Box(modifier) {
    val dateFormatter = LocalTiviDateFormatter.current
    val formattedTime = remember(dateFormatter, selectedTime) {
      selectedTime?.let { dateFormatter.formatShortTime(it) }
    }

    var showDialog by remember { mutableStateOf(false) }
    var time by remember { mutableStateOf(selectedTime) }
    val lastOnTimeSelected by rememberUpdatedState(onTimeSelected)

    ClickableReadOnlyOutlinedTextField(
      value = formattedTime.orEmpty(),
      label = { Text(text = LocalStrings.current.timeLabel) },
      onClick = { showDialog = true },
      modifier = Modifier.fillMaxWidth(),
    )

    if (showDialog) {
      TimePickerDialog(
        onDismissRequest = { showDialog = false },
        selectedTime = selectedTime ?: remember {
          Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        },
        onTimeChanged = {
          time = it
          lastOnTimeSelected(it)
        },
        title = dialogTitle,
      )
    }
  }
}

@ExperimentalMaterial3Api
@Composable
fun ClickableReadOnlyOutlinedTextField(
  value: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  label: (@Composable () -> Unit)? = null,
  shape: Shape = OutlinedTextFieldDefaults.shape,
) {
  val borderColor = MaterialTheme.colorScheme.primary.copy(
    alpha = if (value.isNotEmpty()) 1f else 0.4f,
  )

  Surface(
    onClick = onClick,
    border = BorderStroke(1.dp, borderColor),
    shape = shape,
    modifier = modifier,
  ) {
    Box(Modifier.padding(16.dp)) {
      if (value.isNotEmpty()) {
        Text(text = value)
      } else {
        val disabledText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        CompositionLocalProvider(
          LocalTextStyle provides LocalTextStyle.current.copy(color = disabledText),
        ) {
          label?.invoke()
        }
      }
    }
  }
}
