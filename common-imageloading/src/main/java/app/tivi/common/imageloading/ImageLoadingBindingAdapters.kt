/*
 * Copyright 2019 Google LLC
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

package app.tivi.common.imageloading

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import app.tivi.data.entities.ImageType
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TmdbImageEntity
import coil.api.loadAny
import coil.transform.RoundedCornersTransformation

@BindingAdapter(
        "tmdbBackdropPath",
        "imageSaturateOnLoad",
        requireAll = false
)
fun ImageView.loadBackdrop(
    oldPath: String?,
    oldSaturateOnLoad: Boolean?,
    path: String?,
    saturateOnLoad: Boolean?
) {
    if (oldPath != path || oldSaturateOnLoad != saturateOnLoad) {
        loadImage(
                null,
                oldSaturateOnLoad,
                0f,
                path?.let { ShowTmdbImage(path = path, type = ImageType.BACKDROP, showId = 0) },
                saturateOnLoad,
                0f
        )
    }
}

@BindingAdapter(
        "tmdbLogoPath",
        "imageSaturateOnLoad",
        requireAll = false
)
fun ImageView.loadLogo(
    oldPath: String?,
    oldSaturateOnLoad: Boolean?,
    path: String?,
    saturateOnLoad: Boolean?
) {
    if (oldPath != path || oldSaturateOnLoad != saturateOnLoad) {
        loadImage(
                null,
                oldSaturateOnLoad,
                0f,
                path?.let { ShowTmdbImage(path = path, type = ImageType.LOGO, showId = 0) },
                saturateOnLoad,
                0f
        )
    }
}

@BindingAdapter(
        "image",
        "imageSaturateOnLoad",
        "imageCornerRadius",
        requireAll = false
)
fun ImageView.loadImage(
    oldImage: TmdbImageEntity?,
    oldSaturateOnLoad: Boolean?,
    oldCornerRadius: Float,
    image: TmdbImageEntity?,
    saturateOnLoad: Boolean?,
    cornerRadius: Float
) {
    if (oldImage == image &&
            oldSaturateOnLoad == saturateOnLoad &&
            oldCornerRadius == cornerRadius) return

    loadAny(image) {
        if (saturateOnLoad == null || saturateOnLoad == true) {
            val saturatingTarget = SaturatingImageViewTarget(this@loadImage)
            target(saturatingTarget)
            listener(saturatingTarget)
        }
        if (cornerRadius > 0) {
            transformations(RoundedCornersTransformation(cornerRadius))
        }
    }
}
