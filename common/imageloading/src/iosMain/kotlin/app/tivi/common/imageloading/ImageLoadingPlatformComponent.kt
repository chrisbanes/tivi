// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.util.Logger
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.intercept.Interceptor
import me.tatarka.inject.annotations.Provides

actual interface ImageLoadingPlatformComponent {
  @Provides
  fun provideImageLoader(
    interceptors: Set<Interceptor>,
    logger: Logger,
  ): ImageLoader = IosImageLoaderFactory.create {
    this.logger = logger.asImageLoaderLogger()
    interceptor {
      addInterceptors(interceptors)
    }
  }
}
