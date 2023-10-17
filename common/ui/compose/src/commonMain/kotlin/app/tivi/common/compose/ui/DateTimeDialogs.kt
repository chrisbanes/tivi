// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.tivi.common.compose.LocalStrings
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
expect fun DatePickerDialog(
  onDismissRequest: () -> Unit,
  onDateChanged: (LocalDate) -> Unit,
  selectedDate: LocalDate = remember {
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
  },
  confirmLabel: String = LocalStrings.current.buttonConfirm,
  minimumDate: LocalDate? = null,
  maximumDate: LocalDate? = null,
  title: String = "",
)

@Composable
expect fun TimePickerDialog(
  onDismissRequest: () -> Unit,
  onTimeChanged: (LocalTime) -> Unit,
  selectedTime: LocalTime = remember {
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
  },
  confirmLabel: String = LocalStrings.current.buttonConfirm,
  title: String = "",
)
