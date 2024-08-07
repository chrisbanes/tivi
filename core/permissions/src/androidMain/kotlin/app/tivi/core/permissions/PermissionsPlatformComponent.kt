// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.permissions

import android.app.Application
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface PermissionsPlatformComponent {
  @Provides
  @ApplicationScope
  fun providePermissionController(application: Application): PermissionsController {
    return MokoPermissionControllerWrapper(
      mokoPermissionController = dev.icerock.moko.permissions.PermissionsController(application),
    )
  }
}
