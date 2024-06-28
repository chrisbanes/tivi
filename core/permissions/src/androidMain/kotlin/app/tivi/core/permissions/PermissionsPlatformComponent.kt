// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.permissions

import android.app.Application
import app.tivi.inject.ApplicationScope
import app.tivi.util.Logger
import me.tatarka.inject.annotations.Provides

actual interface PermissionsPlatformComponent {
  @Provides
  @ApplicationScope
  fun providePermissionController(application: Application, logger: Logger): PermissionsController {
    return MokoPermissionControllerWrapper(
      mokoPermissionController = dev.icerock.moko.permissions.PermissionsController(application),
      logger = logger,
    )
  }
}
