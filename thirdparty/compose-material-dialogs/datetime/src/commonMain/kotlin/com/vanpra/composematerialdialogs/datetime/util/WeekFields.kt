package com.vanpra.composematerialdialogs.datetime.util

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek

internal expect class WeekFields {
    val firstDayOfWeek: DayOfWeek
    companion object {
        fun of(locale: Locale): WeekFields
    }
}