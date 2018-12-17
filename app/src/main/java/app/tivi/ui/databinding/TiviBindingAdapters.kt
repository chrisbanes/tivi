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
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.text.italic
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.emoji.text.EmojiCompat
import app.tivi.R
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Genre
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.ui.GenreStringer
import app.tivi.ui.MaxLinesToggleClickListener
import app.tivi.ui.glide.GlideApp
import app.tivi.ui.text.textAppearanceSpanForAttribute
import app.tivi.util.ScrimUtil
import org.threeten.bp.OffsetDateTime

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

@BindingAdapter("genreString")
fun genreString(view: TextView, genres: List<Genre>?) {
    if (genres != null && genres.isNotEmpty()) {
        val spanned = buildSpannedString {
            for (i in 0 until genres.size) {
                val genre = genres[i]
                inSpans(textAppearanceSpanForAttribute(view.context, R.attr.textAppearanceCaption)) {
                    append(view.context.getString(GenreStringer.getLabel(genre)))
                }
                append("\u00A0") // nbsp
                append(GenreStringer.getEmoji(genre))
                if (i < genres.size - 1) append(" \u2022 ")
            }
        }

        val emojiCompat = EmojiCompat.get()
        view.text = if (emojiCompat.loadState == EmojiCompat.LOAD_STATE_SUCCEEDED) {
            emojiCompat.process(spanned)
        } else {
            spanned
        }
    }
}

@BindingAdapter("genreContentDescriptionString")
fun genreContentDescriptionString(view: TextView, genres: List<Genre>?) {
    val genreContentDescription = genres?.joinToString(", ") {
        view.context.getString(GenreStringer.getLabel(it))
    }
    view.contentDescription = genreContentDescription
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

@BindingAdapter("showTitle")
fun showTitle(view: TextView, show: TiviShow) {
    view.text = buildSpannedString {
        inSpans(textAppearanceSpanForAttribute(view.context, R.attr.textAppearanceHeadline6)) {
            append(show.title)
        }
        show.firstAired?.also { firstAired ->
            append(" ")
            inSpans(textAppearanceSpanForAttribute(view.context, R.attr.textAppearanceCaption)) {
                append("(")
                append(firstAired.year.toString())
                append(")")
            }
        }
    }
}

@BindingAdapter("seasonTitleText")
fun episodeTitleText(view: TextView, episode: Episode) {
    val firstAired = episode.firstAired
    if (firstAired == null || firstAired.isAfter(OffsetDateTime.now())) {
        view.text = buildSpannedString {
            episode.title?.also { title ->
                italic {
                    append(title)
                }
            }
        }
    } else {
        view.text = episode.title
    }
}

@BindingAdapter("seasonSummaryText")
fun seasonSummaryText(view: TextView, season: SeasonWithEpisodesAndWatches) {
    val toWatch = season.numberAiredToWatch
    val toAir = season.numberToAir
    val watched = season.numberWatched

    val text = StringBuilder()
    if (toWatch > 0) {
        text.append(view.resources.getString(R.string.season_summary_to_watch, toWatch))
    }
    if (toAir > 0) {
        if (text.isNotEmpty()) {
            text.append(" \u2022 ")
        }
        text.append(view.resources.getString(R.string.season_summary_to_air, toAir))
    }
    if (watched > 0) {
        if (text.isNotEmpty()) {
            text.append(" \u2022 ")
        }
        text.append(view.resources.getString(R.string.season_summary_watched, watched))
    }
    view.text = text
}
