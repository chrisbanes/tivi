/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.tmdb

private val IMAGE_SIZE_PATTERN = "w(\\d+)$".toRegex()

data class TmdbImageUrlProvider(
    private val baseImageUrl: String = TmdbImageSizes.baseImageUrl,
    private val posterSizes: List<String> = TmdbImageSizes.posterSizes,
    private val backdropSizes: List<String> = TmdbImageSizes.backdropSizes,
    private val logoSizes: List<String> = TmdbImageSizes.logoSizes
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
