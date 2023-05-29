// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

data class ShowTmdbImage(
    override val id: Long = 0,
    val showId: Long,
    override val path: String,
    override val type: ImageType,
    override val language: String? = null,
    override val rating: Float = 0f,
    override val isPrimary: Boolean = false,
) : TiviEntity, TmdbImageEntity
