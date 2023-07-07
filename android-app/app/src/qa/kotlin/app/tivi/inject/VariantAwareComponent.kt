// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

interface VariantAwareComponent {
    @ApplicationScope
    @IntoSet
    @Provides
    fun provideHttpLoggingInterceptor(): Interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    @ApplicationScope
    @IntoSet
    @Provides
    fun provideChuckerInterceptor(
        context: Application,
    ): Interceptor = ChuckerInterceptor.Builder(context)
        .redactHeaders(
            "trakt-api-key",
            "Authorization",
        )
        .build()
}
