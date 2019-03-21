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

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import app.tivi.extensions.resolveThemeReference
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.ui.MaxLinesToggleClickListener
import app.tivi.ui.glide.GlideApp
import app.tivi.util.ScrimUtil
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

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
fun maxLinesClickListener(view: TextView, oldCollapsedMaxLines: Int, newCollapsedMaxLines: Int) {
    if (oldCollapsedMaxLines != newCollapsedMaxLines) {
        // Default to collapsed
        view.maxLines = newCollapsedMaxLines
        // Now set click listener
        view.setOnClickListener(MaxLinesToggleClickListener(newCollapsedMaxLines))
    }
}

@BindingAdapter("backgroundScrim")
fun backgroundScrim(view: View, color: Int) {
    view.background = ScrimUtil.makeCubicGradientScrimDrawable(color, 16, Gravity.BOTTOM)
}

@BindingAdapter("foregroundScrim")
fun foregroundScrim(view: View, color: Int) {
    view.foreground = ScrimUtil.makeCubicGradientScrimDrawable(color, 16, Gravity.BOTTOM)
}

@BindingAdapter("materialBackdropBackgroundRadius")
fun materialBackdropBackground(view: View, radius: Float) {
    view.background = MaterialShapeDrawable().apply {
        fillColor = ColorStateList.valueOf(Color.WHITE)
        shapeAppearanceModel.setTopLeftCorner(CornerFamily.ROUNDED, radius.toInt())
        shapeAppearanceModel.setTopRightCorner(CornerFamily.ROUNDED, radius.toInt())
    }
}

@BindingAdapter("textAppearanceAttr")
fun textAppearanceAttr(view: TextView, textAppearanceStyleAttr: Int) {
    view.setTextAppearance(view.context.resolveThemeReference(textAppearanceStyleAttr))
}