// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.common.ui.resources.TiviStrings
import app.tivi.data.models.TiviShow
import app.tivi.inject.ActivityScope
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toNSTimeZone
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSCalendarMatchNextTime
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents

@OptIn(ExperimentalForeignApi::class)
@ActivityScope
@Inject
actual class TiviTextCreator(
  override val dateFormatter: TiviDateFormatter,
  override val strings: TiviStrings,
) : CommonTiviTextCreator {
  override fun airsText(show: TiviShow): CharSequence? {
    val airTime = show.airsTime ?: return null
    val airTz = show.airsTimeZone ?: return null
    val airDay = show.airsDay ?: return null

    val calendar = dateFormatter.calendar
    calendar.timeZone = airTz.toNSTimeZone()

    val components = NSDateComponents().apply {
      hour = airTime.hour.convert()
      minute = airTime.minute.convert()
      second = airTime.second.convert()
      weekday = airDay.toNSWeekdayUnit().convert()
    }

    val localDateTime = calendar.nextDateAfterDate(
      date = NSDate(),
      matchingComponents = components,
      options = NSCalendarMatchNextTime,
    )
      ?.toKotlinInstant()
      ?.toLocalDateTime(dateFormatter.overrideTimeZone ?: TimeZone.currentSystemDefault())
      ?: return null

    return strings.airsText(
      dateFormatter.formatDayOfWeek(localDateTime.dayOfWeek),
      dateFormatter.formatShortTime(localDateTime.time),
    )
  }
}

internal fun DayOfWeek.toNSWeekdayUnit(): Int {
  // NSCalendar: 1 = Sunday, whereas ISO 1 = Monday
  return when (this) {
    DayOfWeek.SUNDAY -> 1
    DayOfWeek.MONDAY -> 2
    DayOfWeek.TUESDAY -> 3
    DayOfWeek.WEDNESDAY -> 4
    DayOfWeek.THURSDAY -> 5
    DayOfWeek.FRIDAY -> 6
    DayOfWeek.SATURDAY -> 7
  }
}
