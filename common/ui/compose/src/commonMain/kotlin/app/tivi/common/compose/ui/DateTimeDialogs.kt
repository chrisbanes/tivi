// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.tivi.common.ui.resources.MR
import dev.icerock.moko.resources.compose.stringResource
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
    confirmLabel: String = stringResource(MR.strings.button_confirm),
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
    confirmLabel: String = stringResource(MR.strings.button_confirm),
    title: String = "",
    is24Hour: Boolean = false,
)
