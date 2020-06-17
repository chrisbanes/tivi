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
import android.content.res.Resources
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.text.parseAsHtml
import app.tivi.common.ui.R
import app.tivi.data.entities.TiviShow
import app.tivi.data.views.FollowedShowsWatchStats
import app.tivi.ui.text.TypefaceSpan
import app.tivi.ui.text.textAppearanceSpanForAttribute
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class HomeTextCreator @Inject constructor(
    @ActivityContext private val context: Context
) {
    private var bodyTypeface: Typeface = Typeface.DEFAULT
        get() {
            if (field == Typeface.DEFAULT) {
                try {
                    ResourcesCompat.getFont(context, R.font.rubik_400)?.also { field = it }
                } catch (nfe: Resources.NotFoundException) {
                    // getFont will throw a NFE if the device if offline or doesn't have
                    // Play Services. Lets not crash
                }
            }
            return field
        }

    @JvmOverloads
    fun showTitle(
        context: Context = this.context,
        show: TiviShow
    ): CharSequence = buildSpannedString {
        append(show.title)

        show.firstAired?.also { firstAired ->
            append(" ")
            inSpans(
                textAppearanceSpanForAttribute(context, R.attr.textAppearanceCaption),
                TypefaceSpan(bodyTypeface)
            ) {
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

    fun followedShowEpisodeWatchStatus(stats: FollowedShowsWatchStats?): CharSequence {
        return if (stats != null && stats.watchedEpisodeCount < stats.episodeCount) {
            context.getString(
                R.string.followed_watch_stats_to_watch,
                stats.episodeCount - stats.watchedEpisodeCount
            ).parseAsHtml()
        } else if (stats != null && stats.watchedEpisodeCount > 0) {
            context.getString(R.string.followed_watch_stats_complete)
        } else {
            return ""
        }
    }
}
