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

package app.tivi.home

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.text.parseAsHtml
import app.tivi.R
import app.tivi.data.entities.TiviShow
import app.tivi.data.views.FollowedShowsWatchStats
import app.tivi.inject.PerActivity
import app.tivi.ui.text.TypefaceSpan
import app.tivi.ui.text.textAppearanceSpanForAttribute
import javax.inject.Inject

class HomeTextCreator @Inject constructor(
    @PerActivity private val context: Context
) {
    private val barlowTypefaceSpan = TypefaceSpan(ResourcesCompat.getFont(context, R.font.barlow_400))

    fun showTitle(show: TiviShow): CharSequence = buildSpannedString {
        append(show.title)

        show.firstAired?.also { firstAired ->
            append(" ")
            inSpans(textAppearanceSpanForAttribute(context, R.attr.textAppearanceCaption), barlowTypefaceSpan) {
                append("(")
                append(firstAired.year.toString())
                append(")")
            }
        }
    }

    fun showHeaderCount(count: Int, filtered: Boolean = false): CharSequence = when {
        filtered -> context.resources.getQuantityString(R.plurals.header_show_count_filtered, count, count)
        else -> context.resources.getQuantityString(R.plurals.header_show_count, count, count)
    }.parseAsHtml()

    fun followedShowEpisodeWatchStatus(stats: FollowedShowsWatchStats): CharSequence = when {
        stats.watchedEpisodeCount < stats.episodeCount -> {
            context.getString(R.string.followed_watch_stats_to_watch,
                    stats.episodeCount - stats.watchedEpisodeCount).parseAsHtml()
        }
        else -> {
            context.getString(R.string.followed_watch_stats_complete)
        }
    }
}