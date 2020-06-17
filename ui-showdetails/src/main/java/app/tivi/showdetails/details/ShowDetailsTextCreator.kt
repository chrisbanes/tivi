/*
 * Copyright 2020 Google LLC
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

package app.tivi.showdetails.details

import android.content.Context
import android.graphics.Color
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.parseAsHtml
import androidx.emoji.text.EmojiCompat
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Genre
import app.tivi.data.entities.Season
import app.tivi.data.entities.ShowStatus
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.EpisodeWithWatches
import app.tivi.data.resultentities.nextToAir
import app.tivi.data.resultentities.numberAiredToWatch
import app.tivi.data.resultentities.numberToAir
import app.tivi.data.resultentities.numberWatched
import app.tivi.data.views.FollowedShowsWatchStats
import app.tivi.ui.GenreStringer
import app.tivi.util.TiviDateFormatter
import dagger.hilt.android.qualifiers.ActivityContext
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.TextStyle
import java.util.Locale
import javax.inject.Inject

class ShowDetailsTextCreator @Inject constructor(
    @ActivityContext private val context: Context,
    private val tiviDateFormatter: TiviDateFormatter
) {
    fun seasonSummaryText(watches: List<EpisodeWithWatches>): CharSequence {
        val toWatch = watches.numberAiredToWatch
        val toAir = watches.numberToAir
        val watched = watches.numberWatched

        val text = StringBuilder()
        if (watched > 0) {
            text.append(context.getString(R.string.season_summary_watched, watched))
        }
        if (toWatch > 0) {
            if (text.isNotEmpty()) text.append(" \u2022 ")
            text.append(context.getString(R.string.season_summary_to_watch, toWatch))
        }
        if (toAir > 0) {
            if (text.isNotEmpty()) text.append(" \u2022 ")
            text.append(context.getString(R.string.season_summary_to_air, toAir))

            val nextToAir = watches.nextToAir
            if (nextToAir?.firstAired != null) {
                text.append(". ")
                text.append(
                    context.getString(
                        R.string.next_prefix,
                        tiviDateFormatter.formatShortRelativeTime(nextToAir.firstAired!!)
                    )
                )
            }
        }
        return text
    }

    fun seasonEpisodeTitleText(season: Season, episode: Episode): String {
        return context.getString(R.string.season_episode_number, season.number, episode.number)
    }

    fun episodeNumberText(episode: Episode): CharSequence? {
        val text = StringBuilder()
        text.append(context.getString(R.string.episode_number, episode.number))
        if (episode.firstAired?.isAfter(OffsetDateTime.now()) == true) {
            text.append(" \u2022 ")
            text.append(tiviDateFormatter.formatShortRelativeTime(episode.firstAired!!))
        }
        return text
    }

    fun genreString(genres: List<Genre>?): CharSequence? {
        if (genres != null && genres.isNotEmpty()) {
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
            return when {
                emojiCompat.loadState == EmojiCompat.LOAD_STATE_SUCCEEDED -> emojiCompat.process(spanned)
                else -> spanned
            }
        }
        return null
    }

    fun genreContentDescription(genres: List<Genre>?): CharSequence? {
        return genres?.joinToString(", ") { context.getString(GenreStringer.getLabel(it)) }
    }

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

    fun airsText(show: TiviShow): CharSequence? {
        val airTime = show.airsTime
        val airTz = show.airsTimeZone
        val airDay = show.airsDay

        if (airTime == null || airTz == null || airDay == null) {
            // If we don't have all the necessary info, return null
            return null
        }

        val local = ZonedDateTime.now()
            .withZoneSameLocal(airTz)
            .with(show.airsDay)
            .with(airTime)
            .withZoneSameInstant(ZoneId.systemDefault())

        return context.getString(
            R.string.airs_text,
            local.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            tiviDateFormatter.formatShortTime(local.toLocalTime())
        )
    }

    fun showStatusText(status: ShowStatus): CharSequence = when (status) {
        ShowStatus.CANCELED, ShowStatus.ENDED -> context.getString(R.string.status_ended)
        ShowStatus.RETURNING -> context.getString(R.string.status_active)
        ShowStatus.IN_PRODUCTION -> context.getString(R.string.status_in_production)
    }
}
