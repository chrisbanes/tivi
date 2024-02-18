// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

internal object AndroidSetCrashReportingEnabledAction : SetCrashReportingEnabledAction {
  override fun invoke(enabled: Boolean) {
    try {
      FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
    } catch (e: IllegalStateException) {
      // Crashlytics is probably not setup
      Log.e(
        "SetCrashReportingEnabledAction",
        "Error while setting crash reporting enabled: $enabled",
        e,
      )
    }
  }
}
