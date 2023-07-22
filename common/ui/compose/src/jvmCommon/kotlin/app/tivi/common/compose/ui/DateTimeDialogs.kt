// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalMaterial3Api::class)

package app.tivi.common.compose.ui

import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.vanpra.composematerialdialogs.datetime.date.DatePicker
import com.vanpra.composematerialdialogs.datetime.time.TimePicker
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Composable
actual fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeChanged: (LocalTime) -> Unit,
    selectedTime: LocalTime,
    confirmLabel: String,
    title: String,
    is24Hour: Boolean,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text(text = confirmLabel)
            }
        },
        text = {
            TimePicker(
                title = title,
                initialTime = selectedTime,
                is24HourClock = false,
                onTimeChange = onTimeChanged,
            )
        },
    )
}

@Composable
actual fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateChanged: (LocalDate) -> Unit,
    selectedDate: LocalDate,
    confirmLabel: String,
    minimumDate: LocalDate?,
    maximumDate: LocalDate?,
    title: String,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text(text = confirmLabel)
            }
        },
        text = {
            DatePicker(
                title = title,
                initialDate = selectedDate,
                onDateChange = onDateChanged,
                allowedDateValidator = { date ->
                    when {
                        minimumDate != null && date < minimumDate -> false
                        maximumDate != null && date > maximumDate -> false
                        else -> true
                    }
                },
            )
        },
    )
}
