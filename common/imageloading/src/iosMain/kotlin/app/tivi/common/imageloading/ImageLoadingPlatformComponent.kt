// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import coil3.PlatformContext
import me.tatarka.inject.annotations.Provides
import okio.FileSystem

actual interface ImageLoadingPlatformComponent {
  @Provides
  fun providePlatformContext(): PlatformContext = PlatformContext.INSTANCE

  @Provides
  fun provideFileSystem(): FileSystem = FileSystem.SYSTEM
}
