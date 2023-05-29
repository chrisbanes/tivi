// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import me.tatarka.inject.annotations.Provides
import okhttp3.Interceptor

interface VariantAwareComponent {
    /**
     * We have no interceptors in the standard release currently
     */
    @Provides
    fun provideInterceptors(): Set<Interceptor> = emptySet()
}
