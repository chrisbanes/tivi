// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import android.annotation.SuppressLint
import android.app.Application
import android.net.ConnectivityManager
import android.os.PowerManager
import androidx.core.content.getSystemService
import app.tivi.settings.TiviPreferences
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidPowerController(
  private val context: Application,
  private val preferences: Lazy<TiviPreferences>,
) : PowerController {
  private val powerManager: PowerManager by lazy { context.getSystemService()!! }
  private val connectivityManager: ConnectivityManager by lazy { context.getSystemService()!! }

  override suspend fun shouldSaveData(): SaveData = when {
    preferences.value.useLessData.get() -> {
      SaveData.Enabled(SaveDataReason.PREFERENCE)
    }

    powerManager.isPowerSaveMode -> {
      SaveData.Enabled(SaveDataReason.SYSTEM_POWER_SAVER)
    }

    isBackgroundDataRestricted() -> {
      SaveData.Enabled(SaveDataReason.SYSTEM_DATA_SAVER)
    }

    else -> SaveData.Disabled
  }

  @SuppressLint("NewApi")
  private fun isBackgroundDataRestricted(): Boolean {
    return connectivityManager.restrictBackgroundStatus ==
      ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
  }
}
