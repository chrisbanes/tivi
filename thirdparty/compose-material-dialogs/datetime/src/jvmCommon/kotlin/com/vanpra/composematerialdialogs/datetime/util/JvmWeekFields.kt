package com.vanpra.composematerialdialogs.datetime.util

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek
import java.time.temporal.WeekFields as jWeekFields

internal actual class WeekFields(private val weekFields: jWeekFields) {
    actual val firstDayOfWeek: DayOfWeek
        get() = weekFields.firstDayOfWeek

    actual companion object {
        actual fun of(locale: Locale): WeekFields {
            return WeekFields(jWeekFields.of(locale.toPlatform()))
        }
    }
}