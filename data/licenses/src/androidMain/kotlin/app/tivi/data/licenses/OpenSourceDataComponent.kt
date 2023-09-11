// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses

import app.tivi.data.licenses.store.AndroidOpenSourceStore
import app.tivi.data.licenses.store.OpenSourceStore
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface OpenSourceDataComponent {

    @ApplicationScope
    @Provides
    fun provideOpenSourceStore(store: AndroidOpenSourceStore): OpenSourceStore = store
}
