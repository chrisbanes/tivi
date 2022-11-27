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

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

@InstallIn(SingletonComponent::class)
@Module
object QaNetworkModule {
    @Provides
    @Singleton
    @IntoSet
    fun provideHttpLoggingInterceptor(): Interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    @Provides
    @Singleton
    @IntoSet
    fun provideChuckerInterceptor(
        @ApplicationContext context: Context
    ): Interceptor = ChuckerInterceptor.Builder(context)
        .redactHeaders(
            "trakt-api-key",
            "Authorization"
        )
        .build()
}
