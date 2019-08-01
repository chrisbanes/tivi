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

package app.tivi.extensions

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.view.doOnLayout
import app.tivi.common.ui.R
import app.tivi.data.entities.ImageType
import app.tivi.data.entities.TmdbImageEntity
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.ui.glide.GlideApp
import app.tivi.ui.glide.GlideRequest
import app.tivi.ui.glide.GlideRequests
import java.util.Objects

fun GlideRequests.optSaturateOnLoad(saturateOnLoad: Boolean): GlideRequest<Drawable> = when {
    saturateOnLoad -> saturateOnLoad()
    else -> asDrawable()
}

fun ImageView.loadTmdbImage(
    image: TmdbImageEntity,
    urlProvider: TmdbImageUrlProvider,
    saturateOnLoad: Boolean = true
) {
    fun invokeGlide() {
        GlideApp.with(this)
                .optSaturateOnLoad(saturateOnLoad)
                .load(image.toUrl(urlProvider, width))
                .thumbnail(GlideApp.with(this)
                        .optSaturateOnLoad(saturateOnLoad)
                        .load(image.toUrl(urlProvider, 0)))
                .into(this)
    }

    val requestKey = Objects.hash(image, urlProvider, saturateOnLoad)

    if (getTag(R.id.image_key) == requestKey) {
        // Image is already in progress
        return
    }

    GlideApp.with(this).clear(this)
    setTag(R.id.image_key, requestKey)

    if (isLaidOut && isAttachedToWindow) {
        invokeGlide()
    } else {
        doOnAttach {
            if (getTag(R.id.image_key) == requestKey) {
                doOnLayout {
                    if (getTag(R.id.image_key) == requestKey) {
                        invokeGlide()
                    }
                }
            }
        }
    }
}

fun ImageView.clearRequest() {
    GlideApp.with(this).clear(this)
    setTag(R.id.image_key, null)
}

fun TmdbImageEntity.toUrl(urlProvider: TmdbImageUrlProvider, width: Int) = when (type) {
    ImageType.BACKDROP -> urlProvider.getBackdropUrl(path, width)
    ImageType.POSTER -> urlProvider.getPosterUrl(path, width)
}