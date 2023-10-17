// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.intercept.Interceptor
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

expect interface ImageLoadingPlatformComponent

interface ImageLoadingComponent : ImageLoadingPlatformComponent {

  val imageLoader: ImageLoader

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
