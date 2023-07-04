// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.Material3Dialog
import app.tivi.common.ui.resources.MR
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.Clock
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
    dialogTitle: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        val dateFormatter = LocalTiviDateFormatter.current
        val formattedDate = remember(dateFormatter, selectedDate) {
            selectedDate?.let { dateFormatter.formatShortDate(it) }
        }

        val dialogState = rememberMaterialDialogState()

        ClickableReadOnlyOutlinedTextField(
            value = formattedDate.orEmpty(),
            label = { Text(text = stringResource(MR.strings.date_label)) },
            onClick = { dialogState.show() },
            modifier = Modifier.fillMaxWidth(),
        )

        var date by remember { mutableStateOf(selectedDate) }

        Material3Dialog(
            dialogState = dialogState,
            buttons = {
                positiveButton(
                    text = stringResource(MR.strings.button_confirm),
                    textStyle = MaterialTheme.typography.labelLarge,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    onClick = { date?.let(onDateSelected) },
                )
            },
        ) {
            datepicker(
                title = dialogTitle,
                initialDate = selectedDate ?: remember {
                    Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date
                },
                colors = DatePickerDefaults.colors(
                    headerBackgroundColor = MaterialTheme.colorScheme.primary,
                    headerTextColor = MaterialTheme.colorScheme.onPrimary,
                    calendarHeaderTextColor = MaterialTheme.colorScheme.onBackground,
                    dateActiveBackgroundColor = MaterialTheme.colorScheme.primary,
                    dateActiveTextColor = MaterialTheme.colorScheme.onPrimary,
                    dateInactiveTextColor = MaterialTheme.colorScheme.onBackground,
                ),
                allowedDateValidator = { date ->
                    // Only allow dates in the past
                    date.toInstant() < Clock.System.now()
                },
                onDateChange = { date = it },
            )
        }
    }
}

private fun LocalDate.toInstant(): Instant {
    return LocalDateTime(this, midday)
        .toInstant(TimeZone.currentSystemDefault())
}

private val midday: LocalTime by lazy { LocalTime(12, 0, 0, 0) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTextField(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    dialogTitle: String,
    modifier: Modifier = Modifier,
    is24Hour: Boolean = false,
) {
    Box(modifier) {
        val dateFormatter = LocalTiviDateFormatter.current
        val formattedTime = remember(dateFormatter, selectedTime) {
            selectedTime?.let { dateFormatter.formatShortTime(it) }
        }

        val dialogState = rememberMaterialDialogState()

        ClickableReadOnlyOutlinedTextField(
            value = formattedTime.orEmpty(),
            label = { Text(text = stringResource(MR.strings.time_label)) },
            onClick = { dialogState.show() },
            modifier = Modifier.fillMaxWidth(),
        )

        var time by remember { mutableStateOf(selectedTime) }

        Material3Dialog(
            dialogState = dialogState,
            buttons = {
                positiveButton(
                    text = stringResource(MR.strings.button_confirm),
                    textStyle = MaterialTheme.typography.labelLarge,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    onClick = { time?.let(onTimeSelected) },
                )
            },
        ) {
            timepicker(
                title = dialogTitle,
                initialTime = selectedTime ?: remember {
                    Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .time
                },
                colors = TimePickerDefaults.colors(
                    activeBackgroundColor = MaterialTheme.colorScheme.primary.copy(0.6f),
                    inactiveBackgroundColor = MaterialTheme.colorScheme.onBackground.copy(0.15f),
                    inactiveTextColor = MaterialTheme.colorScheme.onBackground,
                    selectorColor = MaterialTheme.colorScheme.primary,
                    selectorTextColor = MaterialTheme.colorScheme.onPrimary,
                    headerTextColor = MaterialTheme.colorScheme.onBackground,
                    borderColor = MaterialTheme.colorScheme.onBackground,
                ),
                is24HourClock = is24Hour,
                onTimeChange = { time = it },
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
