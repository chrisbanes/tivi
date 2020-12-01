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
import androidx.core.text.parseAsHtml
import app.tivi.common.ui.R
import app.tivi.data.entities.TiviShow
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class HomeTextCreator @Inject constructor(
    @ActivityContext private val context: Context
) {
    fun showTitle(
        show: TiviShow
    ): CharSequence = StringBuilder()
        .append(show.title)
        .apply {
            show.firstAired?.also { firstAired ->
                append(" ")
                append("(")
                append(firstAired.year.toString())
                append(")")
            }
        }.toString()

    fun showHeaderCount(count: Int, filtered: Boolean = false): CharSequence = when {
        filtered -> context.resources.getQuantityString(R.plurals.header_show_count_filtered, count, count)
        else -> context.resources.getQuantityString(R.plurals.header_show_count, count, count)
    }.parseAsHtml()

    fun followedShowEpisodeWatchStatus(
        episodeCount: Int,
        watchedEpisodeCount: Int
    ): CharSequence = when {
        watchedEpisodeCount < episodeCount -> {
            context.getString(
                R.string.followed_watch_stats_to_watch,
                episodeCount - watchedEpisodeCount
            ).parseAsHtml()
        }
        watchedEpisodeCount > 0 -> {
            context.getString(R.string.followed_watch_stats_complete)
        }
        else -> ""
    }
}
