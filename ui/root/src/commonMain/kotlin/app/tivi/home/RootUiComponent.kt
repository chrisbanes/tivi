// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import app.tivi.inject.ActivityScope
import me.tatarka.inject.annotations.Provides

interface RootUiComponent {
  @Provides
  @ActivityScope
  fun bindTiviContent(impl: DefaultTiviContent): TiviContent = impl
}
