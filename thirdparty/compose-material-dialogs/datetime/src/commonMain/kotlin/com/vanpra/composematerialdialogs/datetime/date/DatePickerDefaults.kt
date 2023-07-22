package com.vanpra.composematerialdialogs.datetime.date

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Object to hold default values used by [datepicker]
 */
object DatePickerDefaults {
    /**
     * Initialises a [DatePickerColors] object which represents the different colors used by
     * the [datepicker] composable
     * @param headerBackgroundColor background color of header
     * @param headerTextColor color of text on the header
     * @param calendarHeaderTextColor color of text on the calendar header (year selector
     * and days of week)
     * @param dateActiveBackgroundColor background color of date when selected
     * @param dateActiveTextColor color of date text when selected
     * @param dateInactiveBackgroundColor background color of date when not selected
     * @param dateInactiveTextColor color of date text when not selected
     */
    @Composable
    fun colors(
        headerBackgroundColor: Color = MaterialTheme.colorScheme.primary,
        headerTextColor: Color = MaterialTheme.colorScheme.onPrimary,
        calendarHeaderTextColor: Color = MaterialTheme.colorScheme.onBackground,
        dateActiveBackgroundColor: Color = MaterialTheme.colorScheme.primary,
        dateInactiveBackgroundColor: Color = Color.Transparent,
        dateActiveTextColor: Color = MaterialTheme.colorScheme.onPrimary,
        dateInactiveTextColor: Color = MaterialTheme.colorScheme.onBackground
    ): DatePickerColors {
        return DefaultDatePickerColors(
            headerBackgroundColor = headerBackgroundColor,
            headerTextColor = headerTextColor,
            calendarHeaderTextColor = calendarHeaderTextColor,
            dateActiveBackgroundColor = dateActiveBackgroundColor,
            dateInactiveBackgroundColor = dateInactiveBackgroundColor,
            dateActiveTextColor = dateActiveTextColor,
            dateInactiveTextColor = dateInactiveTextColor,
        )
    }
}
