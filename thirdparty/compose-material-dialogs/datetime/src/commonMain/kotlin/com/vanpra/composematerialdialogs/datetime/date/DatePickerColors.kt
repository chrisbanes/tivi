package com.vanpra.composematerialdialogs.datetime.date

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color

/**
 * Represents the colors used by a [timepicker] and its parts in different states
 *
 * See [DatePickerDefaults.colors] for the default implementation
 */
interface DatePickerColors {
    val headerBackgroundColor: Color
    val headerTextColor: Color
    val calendarHeaderTextColor: Color

    /**
     * Gets the background color dependant on if the item is active or not
     *
     * @param active true if the component/item is selected and false otherwise
     * @return background color as a State
     */
    @Composable
    fun dateBackgroundColor(active: Boolean): State<Color>

    /**
     * Gets the text color dependant on if the item is active or not
     *
     * @param active true if the component/item is selected and false otherwise
     * @return text color as a State
     */
    @Composable
    fun dateTextColor(active: Boolean, enabled: Boolean): State<Color>
}

internal class DefaultDatePickerColors(
    override val headerBackgroundColor: Color,
    override val headerTextColor: Color,
    override val calendarHeaderTextColor: Color,
    private val dateActiveBackgroundColor: Color,
    private val dateInactiveBackgroundColor: Color,
    private val dateActiveTextColor: Color,
    private val dateInactiveTextColor: Color
) : DatePickerColors {
    @Composable
    override fun dateBackgroundColor(active: Boolean): State<Color> {
        return rememberUpdatedState(if (active) dateActiveBackgroundColor else dateInactiveBackgroundColor)
    }

    @Composable
    override fun dateTextColor(active: Boolean, enabled: Boolean): State<Color> {
        return rememberUpdatedState(
            when {
                active -> dateActiveTextColor
                enabled -> dateInactiveTextColor
                else -> dateInactiveTextColor.copy(alpha = 0.4f)
            }
        )
    }
}
