/*
 * Copyright 2017 Google LLC
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

package app.tivi

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import app.tivi.home.HomeActivity
import app.tivi.trakt.TraktConstants
import javax.inject.Inject

open class TiviAppNavigator @Inject constructor(
    private val context: Context
) : AppNavigator {
    override fun provideAuthHandleResponseIntent(requestCode: Int): PendingIntent {
        val intent = Intent(context, HomeActivity::class.java).apply {
            action = TraktConstants.INTENT_ACTION_HANDLE_AUTH_RESPONSE
        }
        return PendingIntent.getActivity(context, requestCode, intent, 0)
    }

    override fun openAccount() {
        throw IllegalArgumentException("This app navigator can't handle login calls")
    }
}
