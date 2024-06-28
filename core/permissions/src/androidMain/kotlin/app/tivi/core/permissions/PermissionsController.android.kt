// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.permissions

import androidx.activity.ComponentActivity

fun PermissionsController.bind(activity: ComponentActivity) {
  if (this is MokoPermissionControllerWrapper) {
    mokoPermissionController.bind(activity)
  } else {
    error("PermissionsController does not wrap Moko Permissions")
  }
}
