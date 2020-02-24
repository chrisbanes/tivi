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
import android.os.PowerManager
import androidx.core.content.getSystemService
import androidx.core.net.ConnectivityManagerCompat
import app.tivi.settings.TiviPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart

@Singleton
internal class AndroidPowerController @Inject constructor(
    private val context: Context,
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

    override fun shouldSaveData(): SaveData {
        if (preferences.useLessData) {
            return SaveData.Enabled(SaveDataReason.PREFERENCE)
        }
        if (powerManager.isPowerSaveMode) {
            return SaveData.Enabled(SaveDataReason.SYSTEM_POWER_SAVER)
        }
        if (ConnectivityManagerCompat.getRestrictBackgroundStatus(connectivityManager)
            == ConnectivityManagerCompat.RESTRICT_BACKGROUND_STATUS_ENABLED) {
            return SaveData.Enabled(SaveDataReason.SYSTEM_DATA_SAVER)
        }
        return SaveData.Disabled
    }
}
