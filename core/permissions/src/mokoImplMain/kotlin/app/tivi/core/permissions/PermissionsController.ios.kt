// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.permissions

import app.tivi.util.Logger
import dev.icerock.moko.permissions.Permission as MokoPermission
import dev.icerock.moko.permissions.PermissionState as MokoPermissionState
import dev.icerock.moko.permissions.PermissionsController as MokoPermissionsController

internal class MokoPermissionControllerWrapper(
  internal val mokoPermissionController: MokoPermissionsController,
  private val logger: Logger,
) : PermissionsController {
  override suspend fun providePermission(permission: Permission): PermissionState {
    try {
      mokoPermissionController.providePermission(permission.toMokoPermission())
    } catch (e: Exception) {
      logger.i(e) { "Exception thrown during providePermission for $permission" }
    }
    return getPermissionState(permission)
  }

  override suspend fun isPermissionGranted(permission: Permission): Boolean {
    return mokoPermissionController.isPermissionGranted(permission.toMokoPermission())
  }

  override suspend fun getPermissionState(permission: Permission): PermissionState {
    return mokoPermissionController.getPermissionState(permission.toMokoPermission())
      .toPermissionState()
  }

  override fun openAppSettings() {
    mokoPermissionController.openAppSettings()
  }
}

internal fun Permission.toMokoPermission(): MokoPermission = when (this) {
  Permission.CAMERA -> MokoPermission.CAMERA
  Permission.GALLERY -> MokoPermission.GALLERY
  Permission.STORAGE -> MokoPermission.STORAGE
  Permission.WRITE_STORAGE -> MokoPermission.WRITE_STORAGE
  Permission.LOCATION -> MokoPermission.LOCATION
  Permission.COARSE_LOCATION -> MokoPermission.COARSE_LOCATION
  Permission.BACKGROUND_LOCATION -> MokoPermission.BACKGROUND_LOCATION
  Permission.BLUETOOTH_LE -> MokoPermission.BLUETOOTH_LE
  Permission.REMOTE_NOTIFICATION -> MokoPermission.REMOTE_NOTIFICATION
  Permission.RECORD_AUDIO -> MokoPermission.RECORD_AUDIO
  Permission.BLUETOOTH_SCAN -> MokoPermission.BLUETOOTH_SCAN
  Permission.BLUETOOTH_ADVERTISE -> MokoPermission.BLUETOOTH_ADVERTISE
  Permission.BLUETOOTH_CONNECT -> MokoPermission.BLUETOOTH_CONNECT
  Permission.CONTACTS -> MokoPermission.CONTACTS
  Permission.MOTION -> MokoPermission.MOTION
}

internal fun MokoPermissionState.toPermissionState(): PermissionState = when (this) {
  MokoPermissionState.NotDetermined -> PermissionState.NotDetermined
  MokoPermissionState.NotGranted -> PermissionState.NotGranted
  MokoPermissionState.Granted -> PermissionState.Granted
  MokoPermissionState.Denied -> PermissionState.Denied
  MokoPermissionState.DeniedAlways -> PermissionState.DeniedAlways
}
