// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tmdb

private val IMAGE_SIZE_PATTERN = "w(\\d+)$".toRegex()

data class TmdbImageUrlProvider(
    private val baseImageUrl: String = TmdbImageSizes.baseImageUrl,
    private val posterSizes: List<String> = TmdbImageSizes.posterSizes,
    private val backdropSizes: List<String> = TmdbImageSizes.backdropSizes,
    private val logoSizes: List<String> = TmdbImageSizes.logoSizes,
) {
    fun getPosterUrl(path: String, imageWidth: Int): String {
        return "$baseImageUrl${selectSize(posterSizes, imageWidth)}$path"
    }

    fun getBackdropUrl(path: String, imageWidth: Int): String {
        return "$baseImageUrl${selectSize(backdropSizes, imageWidth)}$path"
    }

    fun getLogoUrl(path: String, imageWidth: Int): String {
        return "$baseImageUrl${selectSize(logoSizes, imageWidth)}$path"
    }

    private fun selectSize(sizes: List<String>, imageWidth: Int): String {
        var previousSize: String? = null
        var previousWidth = 0

        for (i in sizes.indices) {
            val size = sizes[i]
            val sizeWidth = extractWidthAsIntFrom(size) ?: continue

            if (sizeWidth > imageWidth) {
                if (previousSize != null && imageWidth > (previousWidth + sizeWidth) / 2) {
                    return size
                } else if (previousSize != null) {
                    return previousSize
                }
            } else if (i == sizes.size - 1) {
                // If we get here then we're larger than the last bucket
                if (imageWidth < sizeWidth * 2) {
                    return size
                }
            }

            previousSize = size
            previousWidth = sizeWidth
        }

        return previousSize ?: sizes.last()
    }

    private fun extractWidthAsIntFrom(size: String): Int? {
        return IMAGE_SIZE_PATTERN.matchEntire(size)?.groups?.get(1)?.value?.toInt()
    }
}
