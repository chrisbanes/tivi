// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.app.Flavor
import me.tatarka.inject.annotations.Provides

interface ProdApplicationComponent {
  @Provides
  fun provideFlavor(): Flavor = Flavor.Standard
}
