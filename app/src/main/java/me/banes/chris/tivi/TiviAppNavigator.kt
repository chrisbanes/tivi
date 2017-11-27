/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import me.banes.chris.tivi.details.ShowDetailsActivity
import me.banes.chris.tivi.settings.SettingsActivity
import me.banes.chris.tivi.trakt.TraktConstants

internal open class TiviAppNavigator(private val context: Context) : AppNavigator {
    override fun provideAuthHandleResponseIntent(requestCode: Int): PendingIntent {
        val intent = Intent(TraktConstants.INTENT_ACTION_HANDLE_AUTH_RESPONSE)
        return PendingIntent.getActivity(context, requestCode, intent, 0)
    }

    override fun startShowDetails(id: Long, sharedElements: SharedElementHelper?) {
        context.startActivity(ShowDetailsActivity.createIntent(context, id))
    }

    override fun startSettings() {
        context.startActivity(Intent(context, SettingsActivity::class.java))
    }
}

internal class TiviAppActivityNavigator(private val activity: Activity) : TiviAppNavigator(activity) {
    override fun startShowDetails(id: Long, sharedElements: SharedElementHelper?) {
        activity.startActivity(ShowDetailsActivity.createIntent(activity, id), sharedElements?.applyToIntent(activity))
    }
}