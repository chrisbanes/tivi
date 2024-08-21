// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.common.ui.resources.Res
import app.tivi.common.ui.resources.airs_text
import app.tivi.common.ui.resources.getStringBlocking
import app.tivi.data.models.TiviShow
import app.tivi.inject.ActivityScope
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toKotlinLocalTime
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
actual class TiviTextCreator(
  override val dateFormatter: TiviDateFormatter,
) : CommonTiviTextCreator {

  override fun airsText(show: TiviShow): String? {
    val airTime = show.airsTime ?: return null
    val airTz = show.airsTimeZone ?: return null
    val airDay = show.airsDay ?: return null

    val localDateTime = java.time.ZonedDateTime.now(airTz.toJavaZoneId())
      .with(airDay)
      .with(airTime.toJavaLocalTime())
      .withZoneSameInstant(dateFormatter.timeZone.toJavaZoneId())

    return getStringBlocking(
      Res.string.airs_text,
      localDateTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
      dateFormatter.formatShortTime(localDateTime.toLocalTime().toKotlinLocalTime()),
    )
  }
}
