package com.vanpra.composematerialdialogs.datetime.util

import androidx.compose.ui.text.intl.Locale
import kotlinx.cinterop.convert
import kotlinx.datetime.DayOfWeek
import platform.Foundation.NSCalendar

internal actual class WeekFields(private val calendar: NSCalendar) {
    actual val firstDayOfWeek: DayOfWeek
        get() = DayOfWeek(calendar.firstWeekday.convert())

    actual companion object {
        actual fun of(locale: Locale): WeekFields {
            return WeekFields(getCalendar(locale))
        }
    }
}