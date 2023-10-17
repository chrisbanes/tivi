// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.home.TiviContent
import me.tatarka.inject.annotations.Component

@ActivityScope
@Component
abstract class WindowComponent(
  @Component val applicationComponent: DesktopApplicationComponent,
) : ProdUiComponent {
  abstract val tiviContent: TiviContent

  companion object
}
