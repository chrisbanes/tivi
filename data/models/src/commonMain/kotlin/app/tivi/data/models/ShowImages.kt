// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

import app.tivi.extensions.unsafeLazy

data class ShowImages(
    val showId: Long,
    val images: List<ShowTmdbImage>,
) {
    val backdrop by unsafeLazy { findHighestRatedForType(ImageType.BACKDROP) }

    val poster by unsafeLazy { findHighestRatedForType(ImageType.POSTER) }

    private fun findHighestRatedForType(type: ImageType): ShowTmdbImage? {
        return images.filter { it.type == type }
            .maxByOrNull { it.rating + (if (it.isPrimary) 10f else 0f) }
    }
}
