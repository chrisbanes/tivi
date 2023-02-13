/*
 * Copyright 2021 Google LLC
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

package app.tivi.util

import android.app.Activity
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.parseAsHtml
import androidx.emoji.text.EmojiCompat
import app.tivi.common.ui.resources.R as UiR
import app.tivi.data.models.Episode
import app.tivi.data.models.Genre
import app.tivi.data.models.Season
import app.tivi.data.models.ShowStatus
import app.tivi.data.models.TiviShow
import app.tivi.ui.GenreStringer
import java.util.Locale
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@Inject
class TiviTextCreator(
    private val context: Activity,
    private val tiviDateFormatter: TiviDateFormatter,
) {
    fun showTitle(
        show: TiviShow,
    ): CharSequence = StringBuilder()
        .append(show.title)
        .apply {
            show.firstAired
                ?.toLocalDateTime(TimeZone.currentSystemDefault())
                ?.also { firstAired ->
                    append(" ")
                    append("(")
                    append(firstAired.year.toString())
                    append(")")
                }
        }.toString()

    fun showHeaderCount(count: Int, filtered: Boolean = false): CharSequence = when {
        filtered -> context.resources.getQuantityString(UiR.plurals.header_show_count_filtered, count, count)
        else -> context.resources.getQuantityString(UiR.plurals.header_show_count, count, count)
    }.parseAsHtml()

    fun followedShowEpisodeWatchStatus(
        episodeCount: Int,
        watchedEpisodeCount: Int,
    ): CharSequence = when {
        watchedEpisodeCount < episodeCount -> {
            context.getString(
                UiR.string.followed_watch_stats_to_watch,
                episodeCount - watchedEpisodeCount,
            ).parseAsHtml()
        }

        watchedEpisodeCount > 0 -> {
            context.getString(UiR.string.followed_watch_stats_complete)
        }

        else -> ""
    }

    fun seasonEpisodeTitleText(season: Season?, episode: Episode?): String {
        return if (season != null && episode != null) {
            context.getString(UiR.string.season_episode_number, season.number, episode.number)
        } else {
            ""
        }
    }

    fun seasonTitle(
        season: Season,
    ): String = when {
        season.title != null -> season.title!!
        season.number != null -> {
            context.getString(UiR.string.season_title_fallback, season.number)
        }

        else -> ""
    }

    fun seasonSummaryText(
        watched: Int,
        toWatch: Int,
        toAir: Int,
        nextToAirDate: Instant? = null,
    ): CharSequence {
        val text = StringBuilder()
        if (watched > 0) {
            text.append(context.getString(UiR.string.season_summary_watched, watched))
        }
        if (toWatch > 0) {
            if (text.isNotEmpty()) text.append(" \u2022 ")
            text.append(context.getString(UiR.string.season_summary_to_watch, toWatch))
        }
        if (toAir > 0) {
            if (text.isNotEmpty()) text.append(" \u2022 ")
            text.append(context.getString(UiR.string.season_summary_to_air, toAir))

            if (nextToAirDate != null) {
                text.append(". ")
                text.append(
                    context.getString(
                        UiR.string.next_prefix,
                        tiviDateFormatter.formatShortRelativeTime(nextToAirDate),
                    ),
                )
            }
        }
        return text
    }

    fun episodeNumberText(episode: Episode): CharSequence {
        val text = StringBuilder()
        text.append(context.getString(UiR.string.episode_number, episode.number))

        episode.firstAired?.also {
            text.append(" \u2022 ")
            text.append(tiviDateFormatter.formatShortRelativeTime(it))
        }

        return text
    }

    fun genreString(genres: List<Genre>?): CharSequence? {
        if (!genres.isNullOrEmpty()) {
            val spanned = buildSpannedString {
                for (i in genres.indices) {
                    val genre = genres[i]
                    append(context.getString(GenreStringer.getLabel(genre)))
                    append("\u00A0") // nbsp
                    color(Color.BLACK) {
                        append(GenreStringer.getEmoji(genre))
                    }
                    if (i < genres.size - 1) append(" \u2022 ")
                }
            }

            val emojiCompat = EmojiCompat.get()
            return when (emojiCompat.loadState) {
                EmojiCompat.LOAD_STATE_SUCCEEDED -> emojiCompat.process(spanned)
                else -> spanned
            }
        }
        return null
    }

    fun genreContentDescription(genres: List<Genre>?): CharSequence? {
        return genres?.joinToString(", ") { context.getString(GenreStringer.getLabel(it)) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun airsText(show: TiviShow): CharSequence? {
        val airTime = show.airsTime
        val airTz = show.airsTimeZone
        val airDay = show.airsDay

        if (airTime == null || airTz == null || airDay == null) {
            // If we don't have all the necessary info, return null
            return null
        }

        val local = java.time.ZonedDateTime.now()
            .withZoneSameLocal(airTz.toJavaZoneId())
            .with(show.airsDay)
            .with(airTime.toJavaLocalTime())
            .withZoneSameInstant(java.time.ZoneId.systemDefault())

        return context.getString(
            UiR.string.airs_text,
            local.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()),
            /*tiviDateFormatter.formatShortTime(local.toLocalTime()) */
            "12:34",
        )
    }

    fun showStatusText(status: ShowStatus): CharSequence = when (status) {
        ShowStatus.CANCELED, ShowStatus.ENDED -> context.getString(UiR.string.status_ended)
        ShowStatus.RETURNING -> context.getString(UiR.string.status_active)
        ShowStatus.IN_PRODUCTION -> context.getString(UiR.string.status_in_production)
    }
}
