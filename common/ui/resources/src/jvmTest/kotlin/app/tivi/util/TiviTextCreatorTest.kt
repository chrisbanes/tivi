// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.data.models.TiviShow
import assertk.assertThat
import assertk.assertions.isEqualTo
import java.util.Locale
import kotlin.test.Test
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

class TiviTextCreatorTest {
    private val tiviDateFormatter = TiviDateFormatter(Locale.UK)
    private val textCreator = TiviTextCreator(tiviDateFormatter)

    private val tiviShow = TiviShow(
        title = "My Show",
        airsDay = DayOfWeek.SUNDAY,
        airsTime = LocalTime(20, 0, 0, 0),
        airsTimeZone = TimeZone.UTC,
    )

    @Test
    fun airsDate() {
        assertThat(textCreator.airsText(tiviShow)).isEqualTo("Sun at 21:00")
    }
}
