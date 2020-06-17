/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.util

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import app.tivi.settings.TiviPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AndroidPowerController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: TiviPreferences
) : PowerController {
    private val powerManager: PowerManager = context.getSystemService()!!
    private val connectivityManager: ConnectivityManager = context.getSystemService()!!

    override fun observeShouldSaveData(ignorePreference: Boolean): Flow<SaveData> {
        return merge(
            context.flowBroadcasts(IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)),
            context.flowBroadcasts(IntentFilter(ConnectivityManager.ACTION_RESTRICT_BACKGROUND_CHANGED))
        ).map { _ ->
            shouldSaveData()
        }.onStart {
            emit(shouldSaveData())
        }
    }

    override fun shouldSaveData(): SaveData = when {
        preferences.useLessData -> {
            SaveData.Enabled(SaveDataReason.PREFERENCE)
        }
        powerManager.isPowerSaveMode -> {
            SaveData.Enabled(SaveDataReason.SYSTEM_POWER_SAVER)
        }
        Build.VERSION.SDK_INT >= 24 && isBackgroundDataRestricted() -> {
            SaveData.Enabled(SaveDataReason.SYSTEM_DATA_SAVER)
        }
        else -> SaveData.Disabled
    }

    @RequiresApi(24)
    private fun isBackgroundDataRestricted(): Boolean {
        return connectivityManager.restrictBackgroundStatus ==
            ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
    }
}
