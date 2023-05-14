/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.inject

import android.app.Application
import au.com.gridstone.debugdrawer.okhttplogs.HttpLogger
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

    @ApplicationScope
    @Provides
    fun provideHttpLogger(application: Application): HttpLogger = HttpLogger(application)

    @Provides
    @IntoSet
    fun provideHttpLoggerInterceptor(httpLogger: HttpLogger): Interceptor = httpLogger.interceptor
}
