// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import android.app.Activity
import app.tivi.core.permissions.PermissionsController
import app.tivi.home.TiviContent
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@ActivityScope
@Component
abstract class AndroidActivityComponent(
  @get:Provides override val activity: Activity,
  @Component val applicationComponent: AndroidApplicationComponent,
) : SharedActivityComponent,
  ProdUiComponent {
  abstract val tiviContent: TiviContent
  abstract val permissionsController: PermissionsController

  companion object
}
