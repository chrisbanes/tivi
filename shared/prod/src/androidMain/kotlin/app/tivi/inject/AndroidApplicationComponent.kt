// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import android.app.Application
import app.tivi.appinitializers.AppInitializers
import app.tivi.tasks.TiviWorkerFactory
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import okhttp3.Interceptor

@Component
@ApplicationScope
abstract class AndroidApplicationComponent(
    @get:Provides val application: Application,
) : SharedApplicationComponent, ProdApplicationComponent {

    abstract val initializers: AppInitializers
    abstract val workerFactory: TiviWorkerFactory

    /**
     * We have no interceptors in the standard release currently
     */
    @Provides
    fun provideInterceptors(): Set<Interceptor> = emptySet()

    companion object
}
