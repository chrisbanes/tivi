package com.vanpra.composematerialdialogs.datetime.time

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Object to hold default values used by [timepicker]
 */
object TimePickerDefaults {
    /**
     * Initialises a [TimePickerColors] object which represents the different colors used by
     * the [timepicker] composable
     *
     * @param activeBackgroundColor background color of selected time unit or period (AM/PM)
     * @param inactiveBackgroundColor background color of inactive items in the dialog including
     * the clock face
     * @param activeTextColor color of text on the activeBackgroundColor
     * @param inactiveTextColor color of text on the inactiveBackgroundColor
     * @param inactivePeriodBackground background color of the inactive period (AM/PM) selector
     * @param selectorColor color of clock hand/selector
     * @param selectorTextColor color of text on selectedColor
     * @param headerTextColor  Get color of title text
     * @param borderColor border color of the period (AM/PM) selector
     */
    @Composable
    fun colors(
        activeBackgroundColor: Color = MaterialTheme.colorScheme.primary.copy(0.3f),
        inactiveBackgroundColor: Color = MaterialTheme.colorScheme.onBackground.copy(0.3f),
        activeTextColor: Color = MaterialTheme.colorScheme.onPrimary,
        inactiveTextColor: Color = MaterialTheme.colorScheme.onBackground,
        inactivePeriodBackground: Color = Color.Transparent,
        selectorColor: Color = MaterialTheme.colorScheme.primary,
        selectorTextColor: Color = MaterialTheme.colorScheme.onPrimary,
        headerTextColor: Color = MaterialTheme.colorScheme.onBackground,
        borderColor: Color = MaterialTheme.colorScheme.onBackground,
    ): TimePickerColors {
        return DefaultTimePickerColors(
            activeBackgroundColor = activeBackgroundColor,
            inactiveBackgroundColor = inactiveBackgroundColor,
            activeTextColor = activeTextColor,
            inactiveTextColor = inactiveTextColor,
            inactivePeriodBackground = inactivePeriodBackground,
            selectorColor = selectorColor,
            selectorTextColor = selectorTextColor,
            headerTextColor = headerTextColor,
            borderColor = borderColor
        )
    }
}
