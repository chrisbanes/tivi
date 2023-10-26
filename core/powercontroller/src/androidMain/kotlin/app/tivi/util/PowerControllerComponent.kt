// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface PowerControllerComponent {
  @Provides
  @ApplicationScope
  fun providePowerController(bind: AndroidPowerController): PowerController = bind
}
