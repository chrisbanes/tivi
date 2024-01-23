// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.benchmark

import android.Manifest
import android.os.Build
import androidx.test.uiautomator.UiDevice

fun UiDevice.allowNotifications(packageName: String) {
  if (Build.VERSION.SDK_INT >= 33) {
    executeShellCommand("pm grant $packageName ${Manifest.permission.POST_NOTIFICATIONS}")
  }
}
