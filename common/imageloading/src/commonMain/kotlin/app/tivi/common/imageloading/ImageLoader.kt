// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.app.ApplicationInfo
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
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
        logger(Logger.asCoilLogger())
      }
    }
    .build()
}

private fun Logger.asCoilLogger(): coil3.util.Logger = object : coil3.util.Logger {

  override var minLevel: Level = Level.Debug

  override fun log(tag: String, level: Level, message: String?, throwable: Throwable?) {
    this@asCoilLogger.log(level.toSeverity(), "Coil", throwable, message.orEmpty())
  }
}

private fun Level.toSeverity(): Severity = when (this) {
  Level.Verbose -> Severity.Verbose
  Level.Debug -> Severity.Debug
  Level.Info -> Severity.Info
  Level.Warn -> Severity.Warn
  Level.Error -> Severity.Error
}
