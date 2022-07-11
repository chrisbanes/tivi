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

package app.tivi.home

import android.app.Application
import app.tivi.ContentViewSetter
import app.tivi.NetworkBehaviorSimulatorInterceptor
import au.com.gridstone.debugdrawer.DebugDrawer
import au.com.gridstone.debugdrawer.DeviceInfoModule
import au.com.gridstone.debugdrawer.okhttplogs.HttpLogger
import au.com.gridstone.debugdrawer.okhttplogs.OkHttpLoggerModule
import au.com.gridstone.debugdrawer.retrofit.DebugRetrofitConfig
import au.com.gridstone.debugdrawer.retrofit.Endpoint
import au.com.gridstone.debugdrawer.retrofit.RetrofitModule
import au.com.gridstone.debugdrawer.timber.TimberModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import retrofit2.mock.NetworkBehavior
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object QaContentViewModule {
    @Provides
    fun provideContentViewSetter(
        httpLogger: HttpLogger,
        debugRetrofitConfig: DebugRetrofitConfig
    ): ContentViewSetter = ContentViewSetter { activity, view ->
        DebugDrawer.with(activity)
            .addSectionTitle("Network")
            .addModule(RetrofitModule(debugRetrofitConfig))
            .addSectionTitle("Logs")
            .addModule(OkHttpLoggerModule(httpLogger))
            .addModule(TimberModule())
            .addSectionTitle("Device information")
            .addModule(DeviceInfoModule())
            .buildMainContainer()
            .apply { addView(view) }
    }

    @Provides
    @Singleton
    fun provideHttpLogger(application: Application): HttpLogger = HttpLogger(application)

    @Provides
    @IntoSet
    fun provideHttpLoggerInterceptor(httpLogger: HttpLogger): Interceptor = httpLogger.interceptor

    @Provides
    @Singleton
    fun provideNetworkBehavior(): NetworkBehavior = NetworkBehavior.create()

    @Provides
    @IntoSet
    @Singleton
    fun provideNetworkBehaviorInterceptor(networkBehavior: NetworkBehavior): Interceptor {
        return NetworkBehaviorSimulatorInterceptor(networkBehavior)
    }

    @Provides
    @Singleton
    fun provideDebugRetrofitConfig(
        application: Application,
        networkBehavior: NetworkBehavior
    ): DebugRetrofitConfig = DebugRetrofitConfig(
        context = application,
        endpoints = listOf(Endpoint(name = "Default", url = "blah", isMock = true)),
        networkBehavior = networkBehavior
    )
}
