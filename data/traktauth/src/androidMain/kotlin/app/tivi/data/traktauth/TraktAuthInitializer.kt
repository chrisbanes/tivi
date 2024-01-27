// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import app.tivi.appinitializers.AppInitializer
import me.tatarka.inject.annotations.Inject

@Inject
class TraktAuthInitializer(
  private val traktLoginAction: Lazy<AndroidTraktLoginAction>,
) : AppInitializer {
  override fun initialize() {
    traktLoginAction.value.registerActivityWatcher()
  }
}
