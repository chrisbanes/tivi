// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.ImageLoaderConfigBuilder
import com.seiko.imageloader.cache.memory.maxSizePercent
import com.seiko.imageloader.component.setupDefaultComponents
import java.io.File
import okio.Path.Companion.toOkioPath

internal object DesktopImageLoaderFactory : ImageLoaderFactory {
    private fun getCacheDir(): File = when (currentOperatingSystem) {
        OperatingSystem.Windows -> File(System.getenv("AppData"), "tivi/cache")
        OperatingSystem.Linux -> File(System.getProperty("user.home"), ".cache/tivi")
        OperatingSystem.MacOS -> File(System.getProperty("user.home"), "Library/Caches/tivi")
        else -> throw IllegalStateException("Unsupported operating system")
    }

    override fun create(
        block: ImageLoaderConfigBuilder.() -> Unit,
    ): ImageLoader = ImageLoader {
        components {
            setupDefaultComponents()
        }
        interceptor {
            memoryCacheConfig { maxSizePercent() }
            diskCacheConfig {
                directory(getCacheDir().resolve("image_cache").toOkioPath())
                maxSizeBytes(512L * 1024 * 1024) // 512MB
            }
        }

        block()
    }
}

internal enum class OperatingSystem {
    Windows, Linux, MacOS, Unknown
}

private val currentOperatingSystem: OperatingSystem
    get() {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("win") -> OperatingSystem.Windows
            os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
                OperatingSystem.Linux
            }

            os.contains("mac") -> OperatingSystem.MacOS
            else -> OperatingSystem.Unknown
        }
    }
