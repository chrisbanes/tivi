/*
 * Copyright 2021 Google LLC
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

package app.tivi.core.analytics

import android.annotation.SuppressLint
import android.app.Application
import app.tivi.extensions.unsafeLazy
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import me.tatarka.inject.annotations.Inject

@Inject
class TiviFirebaseAnalytics(
    private val context: Application,
) : Analytics {
    // False positive. Permissions are added via manifest
    @delegate:SuppressLint("MissingPermission")
    private val firebaseAnalytics: FirebaseAnalytics by unsafeLazy {
        FirebaseAnalytics.getInstance(context)
    }

    override fun trackScreenView(
        name: String,
        arguments: Map<String, *>?,
    ) {
        try {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, name)
                arguments?.let {
                    for (entry in arguments) {
                        param("screen_arg_${entry.key}", entry.value.toString())
                    }
                }
            }
        } catch (t: Throwable) {
            // Ignore, Firebase might not be setup for this project
        }
    }
}
