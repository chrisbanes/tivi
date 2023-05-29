// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.ui.resources.MR
import dev.icerock.moko.resources.compose.stringResource
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
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier) {
        val dateFormatter = LocalTiviDateFormatter.current
        val formattedDate = remember(dateFormatter, selectedDate) {
            selectedDate?.let { dateFormatter.formatShortDate(it) }
        }

        ClickableReadOnlyOutlinedTextField(
            value = formattedDate.orEmpty(),
            label = { Text(text = stringResource(MR.strings.date_label)) },
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth(),
        )

        if (showPicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate?.let { date ->
                    remember { date.toEpochMillis() }
                },
            )

            DatePickerDialog(
                onDismissRequest = { showPicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPicker = false

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

    Box(modifier) {
        val dateFormatter = LocalTiviDateFormatter.current
        val formattedTime = remember(dateFormatter, selectedTime) {
            selectedTime?.let { dateFormatter.formatShortTime(it) }
        }

        ClickableReadOnlyOutlinedTextField(
            value = formattedTime.orEmpty(),
            label = { Text(text = stringResource(MR.strings.time_label)) },
            onClick = { showPicker = true },
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

@ExperimentalMaterial3Api
@Composable
fun ClickableReadOnlyOutlinedTextField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    shape: Shape = TextFieldDefaults.outlinedShape,
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
