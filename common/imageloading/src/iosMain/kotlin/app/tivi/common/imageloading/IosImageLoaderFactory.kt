// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.ImageLoaderConfigBuilder
import com.seiko.imageloader.cache.memory.maxSizePercent
import com.seiko.imageloader.component.setupDefaultComponents
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal object IosImageLoaderFactory : ImageLoaderFactory {
    private val cacheDir: Path by lazy {
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSCachesDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )!!.path.orEmpty().toPath()
    }

    override fun create(
        block: ImageLoaderConfigBuilder.() -> Unit,
    ): ImageLoader = ImageLoader {
        components {
            setupDefaultComponents()
        }
        interceptor {
            memoryCacheConfig {
                // Set the max size to 25% of the app's available memory.
                maxSizePercent(0.25)
            }
            diskCacheConfig {
                directory(cacheDir.resolve("image_cache"))
                maxSizeBytes(512L * 1024 * 1024) // 512MB
            }
        }

        block()
    }
}
