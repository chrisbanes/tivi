package com.vanpra.composematerialdialogs.datetime.date

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDate

internal class DatePickerState(
    initialDate: LocalDate,
    val colors: DatePickerColors,
    val yearRange: IntRange,
) {
    var selected by mutableStateOf(initialDate)
    var yearPickerShowing by mutableStateOf(false)
}
