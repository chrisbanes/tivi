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
import android.net.ConnectivityManager
import android.os.PowerManager
import androidx.core.content.getSystemService
import androidx.core.net.ConnectivityManagerCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AndroidPowerController @Inject constructor(
    context: Context
) : PowerController {
    private val powerManager: PowerManager = context.getSystemService()!!
    private val connectivityManager: ConnectivityManager = context.getSystemService()!!

    override fun canFetchHighResolutionImages(): Boolean {
        if (powerManager.isPowerSaveMode) {
            return false
        }

        if (ConnectivityManagerCompat.getRestrictBackgroundStatus(connectivityManager)
            == ConnectivityManagerCompat.RESTRICT_BACKGROUND_STATUS_ENABLED) {
            return false
        }

        // Otherwise return true
        return true
    }
}
