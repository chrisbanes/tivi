// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import android.content.Context
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.ImageLoaderConfigBuilder
import com.seiko.imageloader.cache.memory.maxSizePercent
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.option.androidContext
import okio.Path.Companion.toOkioPath

internal class AndroidImageLoaderFactory(
    private val context: Context,
) : ImageLoaderFactory {
    override fun create(
        block: ImageLoaderConfigBuilder.() -> Unit,
    ): ImageLoader = ImageLoader {
        options {
            androidContext(context.applicationContext)
        }
        components {
            setupDefaultComponents()
        }
        interceptor {
            memoryCacheConfig {
                maxSizePercent(context.applicationContext)
            }
            diskCacheConfig {
                // We can't use `image_cache` as that is what Coil uses, and therefore users
                // migrating from old versions will have a broken cache.
                directory(context.cacheDir.resolve("image_loader_cache").toOkioPath())
                maxSizeBytes(512L * 1024 * 1024) // 512MB
            }
        }

        block()
    }
}
