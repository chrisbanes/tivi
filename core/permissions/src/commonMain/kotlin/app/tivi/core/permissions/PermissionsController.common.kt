// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.permissions

import app.tivi.core.permissions.Permission.REMOTE_NOTIFICATION

interface PermissionsController {
  /**
   * Check is permission already granted and if not - request permission from user.
   *
   * @param permission what permission we want to provide
   */
  suspend fun providePermission(permission: Permission): PermissionState

  /**
   * @return true if permission already granted. In all other cases - false.
   */
  suspend fun isPermissionGranted(permission: Permission): Boolean

  /**
   * Returns current state of permission. Can be suspended because on
   * Android detection of `Denied`/`NotDetermined` requires a bound FragmentManager.
   *
   * @param permission state of what permission we want
   *
   * @return current state. On Android can't be `DeniedAlways` (except push notifications).
   * On iOS can't be `Denied`.
   * @see PermissionState for a detailed description.
   */
  suspend fun getPermissionState(permission: Permission): PermissionState

  /**
   * Open system UI of application settings to change permissions state
   */
  fun openAppSettings()
}

suspend fun PermissionsController.requestPermissionWithRetry(permission: Permission): PermissionState {
  var lastResult: PermissionState = getPermissionState(permission)
  for (index in 0 until 2) {
    if (lastResult.canRequest()) {
      lastResult = providePermission(permission)
    } else {
      break
    }
  }

  // TODO: Do we want to call openAppSettings() if not granted?

  return lastResult
}

suspend fun PermissionsController.performPermissionedAction(
  permission: Permission,
  block: suspend (PermissionState) -> Unit,
) {
  val permissionState = getPermissionState(permission)
  if (permissionState == PermissionState.Granted) {
    block(permissionState)
  } else {
    block(requestPermissionWithRetry(REMOTE_NOTIFICATION))
  }
}

enum class Permission {
  CAMERA,
  GALLERY,
  STORAGE,
  WRITE_STORAGE,
  LOCATION,
  COARSE_LOCATION,
  BACKGROUND_LOCATION,
  BLUETOOTH_LE,
  REMOTE_NOTIFICATION,
  RECORD_AUDIO,
  BLUETOOTH_SCAN,
  BLUETOOTH_ADVERTISE,
  BLUETOOTH_CONNECT,
  CONTACTS,
  MOTION,
}

enum class PermissionState {

  /**
   * Starting state for each permission.
   */
  NotDetermined,

  /**
   * Android-only. This could mean [NotDetermined] or [DeniedAlways], but the OS doesn't
   * expose which of the two it is in all scenarios.
   */
  NotGranted,

  Granted,

  /**
   * Android-only.
   */
  Denied,

  /**
   * On Android only applicable to Push Notifications.
   */
  DeniedAlways,
}

fun PermissionState.canRequest(): Boolean = when (this) {
  PermissionState.Granted -> false
  PermissionState.DeniedAlways -> false
  else -> true
}
