// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import android.app.Application
import coil3.PlatformContext
import me.tatarka.inject.annotations.Provides
import okio.FileSystem

actual interface ImageLoadingPlatformComponent {
  @Provides
  fun providePlatformContext(application: Application): PlatformContext = application

  @Provides
  fun provideFileSystem(): FileSystem = FileSystem.SYSTEM
}
