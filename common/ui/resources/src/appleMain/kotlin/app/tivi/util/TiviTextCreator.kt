// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.common.ui.resources.MR
import app.tivi.data.models.TiviShow
import app.tivi.inject.ActivityScope
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.format
import kotlinx.cinterop.convert
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toNSTimeZone
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarMatchStrictly
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter

@ActivityScope
@Inject
actual class TiviTextCreator(
    override val dateFormatter: TiviDateFormatter,
) : CommonTiviTextCreator {

    private val dayOfWeekFormatter by lazy {
        NSDateFormatter().apply {
            setDateFormat("EEEE")
            locale = dateFormatter.locale
            calendar = NSCalendar.currentCalendar
        }
    }

    override fun airsText(show: TiviShow): CharSequence? {
        val airTime = show.airsTime ?: return null
        val airTz = show.airsTimeZone ?: return null
        val airDay = show.airsDay ?: return null

        val calendar = NSCalendar.currentCalendar
        calendar.timeZone = airTz.toNSTimeZone()

        val date = NSDateComponents()
            .apply {
                hour = airTime.hour.convert()
                minute = airTime.minute.convert()
                second = airTime.second.convert()
                weekday = airDay.toNSWeekdayUnit().convert()
            }
            .let { component ->
                calendar.nextDateAfterDate(
                    date = NSDate(),
                    matchingComponents = component,
                    options = NSCalendarMatchStrictly,
                )
            } ?: return null

        val localTime = date.toKotlinInstant()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .time

        return MR.strings.airs_text.format(
            dayOfWeekFormatter.stringFromDate(date),
            dateFormatter.formatShortTime(localTime),
        ).asString()
    }

    override fun StringDesc.asString(): String = localized()

    private fun DayOfWeek.toNSWeekdayUnit(): Int {
        // NSCalendar: 1 = Sunday, whereas ISO 1 = Monday
        return (isoDayNumber + 1) % 7
    }
}
