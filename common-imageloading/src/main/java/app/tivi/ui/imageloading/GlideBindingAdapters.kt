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

package app.tivi.ui.imageloading

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import app.tivi.data.entities.ImageType
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TmdbImageEntity
import app.tivi.tmdb.TmdbImageUrlProvider
import coil.api.loadAny

@BindingAdapter(
        "tmdbBackdropPath",
        "tmdbImageUrlProvider",
        "imageSaturateOnLoad",
        requireAll = false
)
fun loadBackdrop(
    view: ImageView,
    oldPath: String?,
    oldUrlProvider: TmdbImageUrlProvider?,
    oldSaturateOnLoad: Boolean?,
    path: String?,
    urlProvider: TmdbImageUrlProvider?,
    saturateOnLoad: Boolean?
) {
    if (oldPath == path && oldUrlProvider == urlProvider && oldSaturateOnLoad == saturateOnLoad) {
        return
    }
    if (urlProvider != null && path != null) {
        view.loadAny(ShowTmdbImage(path = path, type = ImageType.BACKDROP, showId = 0))
    } else {
        view.setImageDrawable(null)
    }
}

@BindingAdapter(
        "image",
        "tmdbImageUrlProvider",
        "imageSaturateOnLoad",
        requireAll = false
)
fun loadImage(
    view: ImageView,
    oldImage: TmdbImageEntity?,
    oldUrlProvider: TmdbImageUrlProvider?,
    oldSaturateOnLoad: Boolean?,
    image: TmdbImageEntity?,
    urlProvider: TmdbImageUrlProvider?,
    saturateOnLoad: Boolean?
) {
    if (oldImage == image && oldUrlProvider == urlProvider && oldSaturateOnLoad == saturateOnLoad) {
        return
    }
    if (urlProvider != null && image != null) {
        view.loadAny(image)
    } else {
        view.setImageDrawable(null)
    }
}
