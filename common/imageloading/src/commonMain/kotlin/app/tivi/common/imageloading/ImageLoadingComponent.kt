// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.app.ApplicationInfo
import app.tivi.appinitializers.AppInitializer
import app.tivi.util.Logger
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.intercept.Interceptor
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

expect interface ImageLoadingPlatformComponent

interface ImageLoadingComponent : ImageLoadingPlatformComponent {

  val imageLoader: ImageLoader

  @Provides
  fun provideImageLoader(
    context: PlatformContext,
    interceptors: Set<Interceptor>,
    info: ApplicationInfo,
    logger: Logger,
  ): ImageLoader = newImageLoader(
    context = context,
    interceptors = interceptors,
    logger = logger,
    debug = info.debugBuild,
    applicationInfo = info,
  )

  @Provides
  @IntoSet
  fun bindImageLoaderCleanupInitializer(initializer: ImageLoaderCleanupInitializer): AppInitializer = initializer

  @Provides
  @IntoSet
  fun provideShowCoilInterceptor(interceptor: ShowImageModelInterceptor): Interceptor = interceptor

  @Provides
  @IntoSet
  fun provideTmdbImageEntityCoilInterceptor(interceptor: TmdbImageEntityCoilInterceptor): Interceptor = interceptor

  @Provides
  @IntoSet
  fun provideEpisodeInterceptor(interceptor: EpisodeImageModelInterceptor): Interceptor = interceptor

  @Provides
  @IntoSet
  fun provideSeasonInterceptor(interceptor: SeasonImageModelInterceptor): Interceptor = interceptor

  @Provides
  @IntoSet
  fun provideHideArtworkInterceptor(interceptor: HideArtworkInterceptor): Interceptor = interceptor
}
