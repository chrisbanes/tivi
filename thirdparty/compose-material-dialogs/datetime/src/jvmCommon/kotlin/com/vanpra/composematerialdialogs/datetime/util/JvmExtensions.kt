package com.vanpra.composematerialdialogs.datetime.util

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinLocalTime
import java.time.format.TextStyle

internal actual val LocalDate.isLeapYear: Boolean
    get() = toJavaLocalDate().isLeapYear

internal actual fun LocalTime.minusHours(hoursToSubtract: Long): LocalTime = toJavaLocalTime().minusHours(hoursToSubtract).toKotlinLocalTime()
internal actual fun LocalTime.plusHours(hoursToAdd: Long): LocalTime = toJavaLocalTime().plusHours(hoursToAdd).toKotlinLocalTime()

internal fun Locale.toPlatform() = java.util.Locale.forLanguageTag(toLanguageTag())

internal actual fun Month.getShortLocalName(locale: Locale): String =
    this.getDisplayName(TextStyle.SHORT_STANDALONE, locale.toPlatform())

internal actual fun Month.getFullLocalName(locale: Locale) =
    this.getDisplayName(TextStyle.FULL_STANDALONE, locale.toPlatform())

internal actual fun DayOfWeek.getShortLocalName(locale: Locale) =
    this.getDisplayName(TextStyle.SHORT_STANDALONE, locale.toPlatform())

internal actual fun Month.testLength(year: Int, isLeapYear: Boolean): Int = this.length(isLeapYear)

internal actual fun DayOfWeek.plusDays(days: Long): DayOfWeek = plus(days)

internal actual fun DayOfWeek.getNarrowDisplayName(locale: Locale): String = getDisplayName(TextStyle.NARROW, locale.toPlatform())
