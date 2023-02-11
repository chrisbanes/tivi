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

package app.tivi.util

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
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
        label: String,
        route: String?,
        arguments: Any?,
    ) {
        try {
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, label)
                if (route != null) param("screen_route", route)

                // Expand out the rest of the parameters
                when {
                    arguments is Bundle -> {
                        for (key in arguments.keySet()) {
                            @Suppress("DEPRECATION")
                            val value = arguments.get(key)

                            // We don't want to include the label or route twice
                            if (value != label || value != route) {
                                param("screen_arg_$key", value.toString())
                            }
                        }
                    }

                    arguments != null -> param("screen_arg", arguments.toString())
                }
            }
        } catch (t: Throwable) {
            // Ignore, Firebase might not be setup for this project
        }
    }
}
