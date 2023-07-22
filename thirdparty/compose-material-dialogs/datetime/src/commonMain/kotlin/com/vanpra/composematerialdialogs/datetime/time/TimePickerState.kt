package com.vanpra.composematerialdialogs.datetime.time

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.vanpra.composematerialdialogs.datetime.util.isAM
import kotlinx.datetime.LocalTime

internal enum class ClockScreen {
    Hour,
    Minute;

    fun isHour() = this == Hour
    fun isMinute() = this == Minute
}

internal class TimePickerState(
    val colors: TimePickerColors,
    selectedTime: LocalTime,
    currentScreen: ClockScreen = ClockScreen.Hour,
    clockInput: Boolean = true,
    timeRange: ClosedRange<LocalTime>,
    is24Hour: Boolean,
) {
    var selectedTime by mutableStateOf(selectedTime)
    var timeRange by mutableStateOf(timeRange)
    var is24Hour by mutableStateOf(is24Hour)
    var currentScreen by mutableStateOf(currentScreen)
    var clockInput by mutableStateOf(clockInput)

    private fun minimumMinute(isAM: Boolean, hour: Int): Int {
        return when {
            isAM == timeRange.start.isAM ->
                if (timeRange.start.hour == hour) {
                    timeRange.start.minute
                } else {
                    0
                }
            isAM -> 61
            else -> 0
        }
    }

    private fun maximumMinute(isAM: Boolean, hour: Int): Int {
        return when {
            isAM == timeRange.endInclusive.isAM ->
                if (timeRange.endInclusive.hour == hour) {
                    timeRange.endInclusive.minute
                } else {
                    60
                }
            isAM -> 60
            else -> 0
        }
    }

    fun hourRange() = timeRange.start.hour..timeRange.endInclusive.hour

    fun minuteRange(isAM: Boolean, hour: Int) = minimumMinute(isAM, hour)..maximumMinute(isAM, hour)
}
