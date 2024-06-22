// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.imagemodels

import app.tivi.data.models.Season

data class SeasonImageModel(val id: Long) : ImageModel

fun Season.asImageModel(): SeasonImageModel = SeasonImageModel(id = id)
