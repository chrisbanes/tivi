// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.util.Logger
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.ImageLoaderConfigBuilder
import com.seiko.imageloader.util.LogPriority

internal fun interface ImageLoaderFactory {
  fun create(
    block: ImageLoaderConfigBuilder.() -> Unit,
  ): ImageLoader
}

internal fun Logger.asImageLoaderLogger(): com.seiko.imageloader.util.Logger {
  return object : com.seiko.imageloader.util.Logger {
    override fun isLoggable(priority: LogPriority): Boolean = true

    override fun log(
      priority: LogPriority,
      tag: String,
      data: Any?,
      throwable: Throwable?,
      message: String,
    ) {
      when (priority) {
        LogPriority.VERBOSE -> v(throwable) { message }
        LogPriority.DEBUG -> d(throwable) { message }
        LogPriority.INFO -> i(throwable) { message }
        LogPriority.WARN -> i(throwable) { message }
        LogPriority.ERROR -> e(throwable) { message }
        LogPriority.ASSERT -> e(throwable) { message }
      }
    }
  }
}
