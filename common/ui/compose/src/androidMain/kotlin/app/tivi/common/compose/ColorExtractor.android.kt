// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.allowRgb565

internal actual fun ImageRequest.Builder.prepareForColorExtractor(): ImageRequest.Builder {
  return allowHardware(false)
    .allowRgb565(true)
}
