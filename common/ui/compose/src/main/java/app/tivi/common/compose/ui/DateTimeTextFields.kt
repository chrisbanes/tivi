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

package app.tivi.common.compose.ui

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.LocalTiviDateFormatter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTextField(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDateDialog by remember { mutableStateOf(false) }

    Box(modifier.clickable { showDateDialog = true }) {
        val dateFormatter = LocalTiviDateFormatter.current
        val formattedDate = remember(dateFormatter, selectedDate) {
            selectedDate?.let { dateFormatter.formatShortDate(it) }
        }

        OutlinedTextField(
            value = formattedDate.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "Date") },
            modifier = Modifier.fillMaxWidth(),
        )

        if (showDateDialog) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate?.let { date ->
                    remember { date.toEpochMillis() }
                },
            )

            DatePickerDialog(
                onDismissRequest = { showDateDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDateDialog = false

                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = Instant.fromEpochMilliseconds(millis)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .date
                                onDateSelected(date)
                            }
                        },
                    ) {
                        Text("Confirm")
                    }
                },
            ) {
                DatePicker(
                    state = datePickerState,
                    dateValidator = { epoch ->
                        // Only allow dates in the past
                        epoch < System.currentTimeMillis()
                    },
                )
            }
        }
    }
}

private fun LocalDate.toEpochMillis(): Long {
    return LocalDateTime(this, midday)
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
}

private val midday: LocalTime = LocalTime(12, 0, 0, 0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTextField(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    is24Hour: Boolean = TimeTextFieldDefaults.is24Hour,
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier.clickable { showPicker = true }) {
        val dateFormatter = LocalTiviDateFormatter.current
        val formattedTime = remember(dateFormatter, selectedTime) {
            selectedTime?.let { dateFormatter.formatShortTime(it) }
        }

        OutlinedTextField(
            value = formattedTime.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "Time") },
            modifier = Modifier.fillMaxWidth(),
        )

        if (showPicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = selectedTime?.hour ?: 0,
                initialMinute = selectedTime?.minute ?: 0,
                is24Hour = is24Hour,
            )

            TimePickerDialog(
                onDismissRequest = { showPicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPicker = false

                            onTimeSelected(
                                LocalTime(
                                    hour = timePickerState.hour,
                                    minute = timePickerState.minute,
                                    second = 0,
                                    nanosecond = 0,
                                ),
                            )
                        },
                    ) {
                        Text(text = "Confirm")
                    }
                },
            ) {
                Box(Modifier.padding(24.dp)) {
                    TimePicker(state = timePickerState)
                }
            }
        }
    }
}

object TimeTextFieldDefaults {
    val is24Hour: Boolean
        @Composable get() {
            val context = LocalContext.current
            return remember { DateFormat.is24HourFormat(context) }
        }
}
