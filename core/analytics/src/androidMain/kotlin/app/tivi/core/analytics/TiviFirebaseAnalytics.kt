// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
