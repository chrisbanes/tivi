// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.permissions

import me.tatarka.inject.annotations.Provides

actual interface PermissionsPlatformComponent {
  @Provides
  fun providePermissionController(): PermissionsController = EmptyPermissionController
}

internal object EmptyPermissionController : PermissionsController {
  override suspend fun providePermission(permission: Permission) = getPermissionState(permission)

  override suspend fun isPermissionGranted(permission: Permission): Boolean {
    return false
  }

  override suspend fun getPermissionState(permission: Permission): PermissionState {
    return PermissionState.NotDetermined
  }

  override fun openAppSettings() {
    // no-op
  }
}
