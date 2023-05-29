// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.imagemodels

import app.tivi.data.models.Episode

data class EpisodeImageModel(val id: Long)

fun Episode.asImageModel(): EpisodeImageModel = EpisodeImageModel(id = id)
