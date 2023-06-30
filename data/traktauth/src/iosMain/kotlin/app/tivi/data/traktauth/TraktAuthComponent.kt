// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import app.tivi.data.traktauth.store.AuthStore
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface TraktAuthComponent {
    @ApplicationScope
    @Provides
    fun provideAuthStore(store: IosAuthStore): AuthStore = store

    @ApplicationScope
    @Provides
    fun provideRefreshTraktTokensInteractor(impl: IosRefreshTraktTokensInteractor): RefreshTraktTokensInteractor = impl

    @Provides
    @ApplicationScope
    fun provideLoginToTraktInteractor(impl: IosLoginToTraktInteractor): LoginToTraktInteractor = impl
}
