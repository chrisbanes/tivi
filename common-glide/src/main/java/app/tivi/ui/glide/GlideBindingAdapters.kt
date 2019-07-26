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

package app.tivi.ui.glide

import android.widget.ImageView
import androidx.core.view.doOnLayout
import androidx.databinding.BindingAdapter
import app.tivi.common.ui.R
import app.tivi.data.entities.ImageType
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TmdbImageEntity
import app.tivi.extensions.optSaturateOnLoad
import app.tivi.tmdb.TmdbImageUrlProvider
import java.util.Objects

@BindingAdapter(
        "tmdbBackdropPath",
        "tmdbImageUrlProvider",
        "imageSaturateOnLoad",
        requireAll = false
)
fun loadBackdrop(
    view: ImageView,
    path: String?,
    urlProvider: TmdbImageUrlProvider?,
    saturateOnLoad: Boolean?
) {
    val image = if (path != null) ShowTmdbImage(path = path, type = ImageType.BACKDROP, showId = 0) else null
    loadImage(view, null, urlProvider, saturateOnLoad, image, urlProvider, saturateOnLoad)
}

@Suppress("UNUSED_PARAMETER")
@BindingAdapter(
        "image",
        "tmdbImageUrlProvider",
        "imageSaturateOnLoad",
        requireAll = false
)
fun loadImage(
    view: ImageView,
    previousImage: TmdbImageEntity?,
    previousUrlProvider: TmdbImageUrlProvider?,
    previousSaturateOnLoad: Boolean?,
    image: TmdbImageEntity?,
    urlProvider: TmdbImageUrlProvider?,
    saturateOnLoad: Boolean?
) {
    val requestKey = Objects.hash(image, urlProvider)
    view.setTag(R.id.image_key, requestKey)

    if (urlProvider != null && image != null) {
        if (previousUrlProvider == urlProvider && previousImage == image) {
            return
        }

        view.setImageDrawable(null)

        view.doOnLayout {
            if (it.getTag(R.id.image_key) != requestKey) {
                // The request key is different, exit now since there's we've probably be rebound to a different
                // item
                return@doOnLayout
            }

            fun toUrl(image: TmdbImageEntity, width: Int) = when (image.type) {
                ImageType.BACKDROP -> urlProvider.getBackdropUrl(image.path, width)
                ImageType.POSTER -> urlProvider.getPosterUrl(image.path, width)
            }

            GlideApp.with(view).clear(view)

            GlideApp.with(it)
                    .optSaturateOnLoad(saturateOnLoad == null || saturateOnLoad)
                    .load(toUrl(image, view.width))
                    .thumbnail(GlideApp.with(view)
                            .optSaturateOnLoad(saturateOnLoad == null || saturateOnLoad)
                            .load(toUrl(image, 0)))
                    .into(view)
        }
    } else {
        GlideApp.with(view).clear(view)
        view.setImageDrawable(null)
    }
}
