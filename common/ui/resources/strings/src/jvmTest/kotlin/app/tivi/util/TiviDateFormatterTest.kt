// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import java.util.Locale
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class TiviDateFormatterTest {
  private val tiviDateFormatter = TiviDateFormatter(
    locale = Locale.UK,
    timeZone = TimeZone.UTC,
  )

  @Test
  fun formatShortDate() {
    assertThat(tiviDateFormatter.formatShortDate(instant)).isEqualTo("01/06/2023")
  }

  @Test
  fun formatShortTime() {
    assertThat(tiviDateFormatter.formatShortTime(localTime)).isEqualTo("01:23")
  }

  @Test
  fun formatMediumDate() {
    assertThat(tiviDateFormatter.formatMediumDate(instant)).isEqualTo("1 Jun 2023")
  }

  @Test
  fun formatMediumDateTime() {
    assertThat(tiviDateFormatter.formatMediumDateTime(instant))
      .isEqualTo("1 Jun 2023, 01:23:45")
  }

  @Test
  fun formatShortRelativeTime() {
    assertThat(tiviDateFormatter.formatShortRelativeTime(instant, instant + 21.days))
      .isEqualTo("01/06/2023")

    assertThat(tiviDateFormatter.formatShortRelativeTime(instant, instant + 2.days))
      .isEqualTo("01/06/2023")

    assertThat(tiviDateFormatter.formatShortRelativeTime(instant, instant - 2.days))
      .isEqualTo("01/06/2023")

    assertThat(tiviDateFormatter.formatShortRelativeTime(instant, instant - 21.days))
      .isEqualTo("01/06/2023")
  }

  companion object {
    val localTime = LocalTime.parse("01:23:45")
    val localDateTime = LocalDateTime.parse("2023-06-01T01:23:45")
    val instant = localDateTime.toInstant(TimeZone.UTC)
  }
}
