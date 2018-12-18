/*
 * Copyright 2018 Google LLC
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

package app.tivi.ui.databinding

import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.ui.MaxLinesToggleClickListener
import app.tivi.ui.glide.GlideApp
import app.tivi.util.ScrimUtil

@BindingAdapter("tmdbPosterPath", "tmdbImageUrlProvider", "imageSaturateOnLoad")
fun loadPoster(view: ImageView, path: String?, urlProvider: TmdbImageUrlProvider?, saturateOnLoad: Boolean?) {
    if (path != null && urlProvider != null) {
        view.doOnLayout {
            GlideApp.with(view)
                    .let { r -> if (saturateOnLoad == true) r.saturateOnLoad() else r.asDrawable() }
                    .load(urlProvider.getPosterUrl(path, it.width))
                    .thumbnail(GlideApp.with(view).load(urlProvider.getPosterUrl(path, 0)))
                    .into(view)
        }
    } else {
        GlideApp.with(view).clear(view)
    }
}

@BindingAdapter("tmdbPosterPath", "tmdbImageUrlProvider")
fun loadPoster(view: ImageView, path: String?, urlProvider: TmdbImageUrlProvider?) {
    loadPoster(view, path, urlProvider, true)
}

@BindingAdapter("tmdbBackdropPath", "tmdbImageUrlProvider")
fun loadBackdrop(view: ImageView, path: String?, urlProvider: TmdbImageUrlProvider?) {
    loadBackdrop(view, path, urlProvider, true)
}

@BindingAdapter("tmdbBackdropPath", "tmdbImageUrlProvider", "imageSaturateOnLoad")
fun loadBackdrop(view: ImageView, path: String?, urlProvider: TmdbImageUrlProvider?, saturateOnLoad: Boolean?) {
    if (path != null && urlProvider != null) {
        view.doOnLayout {
            GlideApp.with(view)
                    .let { r -> if (saturateOnLoad == true) r.saturateOnLoad() else r.asDrawable() }
                    .load(urlProvider.getBackdropUrl(path, it.width))
                    .thumbnail(GlideApp.with(view).load(urlProvider.getBackdropUrl(path, 0)))
                    .into(view)
        }
    } else {
        GlideApp.with(view).clear(view)
    }
}

@BindingAdapter("visibleIfNotNull")
fun visibleIfNotNull(view: View, target: Any?) {
    view.isVisible = target != null
}

@BindingAdapter("visible")
fun visible(view: View, value: Boolean) {
    view.isVisible = value
}

@BindingAdapter("srcRes")
fun imageViewSrcRes(view: ImageView, drawableRes: Int) {
    if (drawableRes != 0) {
        view.setImageResource(drawableRes)
    } else {
        view.setImageDrawable(null)
    }
}

@BindingAdapter("maxLinesToggle")
fun maxLinesClickListener(view: TextView, collapsedMaxLines: Int) {
    // Default to collapsed
    view.maxLines = collapsedMaxLines
    // Now set click listener
    view.setOnClickListener(MaxLinesToggleClickListener(collapsedMaxLines))
}

@BindingAdapter("backgroundScrim")
fun backgroundScrim(view: View, color: Int) {
    view.background = ScrimUtil.makeCubicGradientScrimDrawable(color, 16, Gravity.BOTTOM)
}

@BindingAdapter("foregroundScrim")
fun foregroundScrim(view: View, color: Int) {
    view.foreground = ScrimUtil.makeCubicGradientScrimDrawable(color, 16, Gravity.BOTTOM)
}
