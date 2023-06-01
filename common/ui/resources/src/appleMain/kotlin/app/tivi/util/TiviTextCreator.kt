// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.common.ui.resources.MR
import app.tivi.data.models.TiviShow
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.format
import kotlinx.cinterop.convert
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toNSTimeZone
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitWeekday
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponentsFormatter

actual class TiviTextCreator(
    override val dateFormatter: TiviDateFormatter,
) : CommonTiviTextCreator {

    private val shortTime by lazy {
        NSDateComponentsFormatter().apply {
            calendar = NSCalendar.currentCalendar
        }
    }

    override fun airsText(show: TiviShow): CharSequence? {
        val airTime = show.airsTime ?: return null
        val airTz = show.airsTimeZone ?: return null
        val airDay = show.airsDay ?: return null

        val calendar = NSCalendar.currentCalendar
        calendar.timeZone = airTz.toNSTimeZone()

        val date = NSDate()
        calendar.dateBySettingUnit(NSCalendarUnitSecond, airTime.second.convert(), date, 0)
        calendar.dateBySettingUnit(NSCalendarUnitMinute, airTime.minute.convert(), date, 0)
        calendar.dateBySettingUnit(NSCalendarUnitHour, airTime.hour.convert(), date, 0)
        calendar.dateBySettingUnit(NSCalendarUnitWeekday, airDay.toNSWeekdayUnit().convert(), date, 0)

        val localTime = date.toKotlinInstant()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .time

        return MR.strings.airs_text.format(
            shortTime.stringFromDateComponents(calendar.components(NSCalendarUnitWeekday, date))!!,
            dateFormatter.formatShortTime(localTime),
        ).asString()
    }

    override fun StringDesc.asString(): String = localized()

    private fun DayOfWeek.toNSWeekdayUnit(): Int {
        // NSCalendar: 1 = Sunday, whereas ISO 1 = Monday
        return (isoDayNumber + 1) % 7
    }
}
