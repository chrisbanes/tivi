// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.app.ApplicationInfo
import app.tivi.util.Logger
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.intercept.Interceptor
import coil3.memory.MemoryCache
import coil3.util.Logger.Level
import okio.Path.Companion.toPath

internal fun newImageLoader(
  context: PlatformContext,
  interceptors: Set<Interceptor>,
  applicationInfo: ApplicationInfo,
  logger: Logger,
  debug: Boolean = false,
): ImageLoader {
  return ImageLoader.Builder(context)
    .components {
      interceptors.forEach(::add)
    }
    .memoryCache {
      MemoryCache.Builder()
        .maxSizePercent(context, percent = 0.25)
        .build()
    }
    .diskCache {
      DiskCache.Builder()
        .directory(applicationInfo.cachePath().toPath().resolve("coil_cache"))
        .build()
    }
    .apply {
      if (debug) {
        logger(logger.asCoilLogger())
      }
    }
    .build()
}

private fun Logger.asCoilLogger(): coil3.util.Logger = object : coil3.util.Logger {

  override var minLevel: Level = Level.Debug

  override fun log(
    tag: String,
    level: Level,
    message: String?,
    throwable: Throwable?,
  ) {
    when (level) {
      Level.Verbose -> v(throwable) { message.orEmpty() }
      Level.Debug -> d(throwable) { message.orEmpty() }
      Level.Info -> i(throwable) { message.orEmpty() }
      Level.Warn -> i(throwable) { message.orEmpty() }
      Level.Error -> e(throwable) { message.orEmpty() }
    }
  }
}
