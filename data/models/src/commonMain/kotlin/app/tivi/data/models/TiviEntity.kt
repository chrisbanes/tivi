// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

interface TiviEntity {
    val id: Long
}

interface TraktIdEntity {
    val traktId: Int?
}

interface TmdbIdEntity {
    val tmdbId: Int?
}

interface TmdbImageEntity : TiviEntity {
    val path: String
    val type: ImageType
    val language: String?
    val rating: Float
    val isPrimary: Boolean
}

enum class ImageType(val storageKey: String) {
    BACKDROP("backdrop"),
    POSTER("poster"),
    LOGO("logo"),
}
