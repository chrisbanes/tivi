// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.appinitializers.AppInitializers
import me.tatarka.inject.annotations.Component

@Component
@ApplicationScope
abstract class DesktopApplicationComponent : SharedApplicationComponent, QaApplicationComponent {
  abstract val initializers: AppInitializers
  companion object
}
