package com.vanpra.composematerialdialogs.datetime.time

import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Represents the colors used by a [timepicker] and its parts in different states
 *
 * See [TimePickerDefaults.colors] for the default implementation
 */
interface TimePickerColors {
    val border: BorderStroke

    /**
     * Gets the background color dependant on if the item is active or not
     *
     * @param active true if the component/item is selected and false otherwise
     * @return background color as a State
     */
    @Composable
    fun backgroundColor(active: Boolean): State<Color>

    /**
     * Gets the text color dependant on if the item is active or not
     *
     * @param active true if the component/item is selected and false otherwise
     * @return text color as a State
     */
    @Composable
    fun textColor(active: Boolean): State<Color>

    /**
     * Get the color of clock hand and color of text in clock hand
     */
    fun selectorColor(): Color
    fun selectorTextColor(): Color

    /**
     * Get color of title text
     */
    fun headerTextColor(): Color

    @Composable
    fun periodBackgroundColor(active: Boolean): State<Color>
}

internal class DefaultTimePickerColors(
    private val activeBackgroundColor: Color,
    private val inactiveBackgroundColor: Color,
    private val activeTextColor: Color,
    private val inactiveTextColor: Color,
    private val inactivePeriodBackground: Color,
    private val selectorColor: Color,
    private val selectorTextColor: Color,
    private val headerTextColor: Color,
    borderColor: Color
) : TimePickerColors {
    override val border = BorderStroke(1.dp, borderColor)

    @Composable
    override fun backgroundColor(active: Boolean): State<Color> {
        return rememberUpdatedState(if (active) activeBackgroundColor else inactiveBackgroundColor)
    }

    @Composable
    override fun textColor(active: Boolean): State<Color> {
        return rememberUpdatedState(if (active) activeTextColor else inactiveTextColor)
    }

    override fun selectorColor(): Color {
        return selectorColor
    }

    override fun selectorTextColor(): Color {
        return selectorTextColor
    }

    override fun headerTextColor(): Color {
        return headerTextColor
    }

    @Composable
    override fun periodBackgroundColor(active: Boolean): State<Color> {
        return rememberUpdatedState(if (active) activeBackgroundColor else inactivePeriodBackground)
    }
}
