// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.imagemodels

import app.tivi.data.models.ImageType
import app.tivi.data.models.TiviShow

data class ShowImageModel(
    val id: Long,
    val imageType: ImageType = ImageType.BACKDROP,
)

fun TiviShow.asImageModel(
    imageType: ImageType,
): ShowImageModel = ShowImageModel(id = id, imageType = imageType)
