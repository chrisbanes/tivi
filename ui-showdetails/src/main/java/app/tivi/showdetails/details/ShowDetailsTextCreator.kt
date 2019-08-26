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
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.data.views.FollowedShowsWatchStats
import app.tivi.inject.PerActivity
import app.tivi.ui.GenreStringer
import app.tivi.util.TiviDateFormatter
import javax.inject.Inject

class ShowDetailsTextCreator @Inject constructor(
    @PerActivity private val context: Context,
    private val tiviDateFormatter: TiviDateFormatter
) {
    fun seasonSummaryText(season: SeasonWithEpisodesAndWatches): CharSequence {
        val toWatch = season.numberAiredToWatch
        val toAir = season.numberToAir
        val watched = season.numberWatched

        val text = StringBuilder()
        if (watched > 0) {
            text.append(context.getString(R.string.season_summary_watched, watched))
        }
        if (toWatch > 0) {
            if (text.isNotEmpty()) {
                text.append(" \u2022 ")
            }
            text.append(context.getString(R.string.season_summary_to_watch, toWatch))
        }
        if (toAir > 0) {
            if (text.isNotEmpty()) {
                text.append(" \u2022 ")
            }
            text.append(context.getString(R.string.season_summary_to_air, toAir))

            val nextToAir = season.nextToAir
            if (nextToAir != null) {
                text.append(". ")
                text.append(context.getString(
                        R.string.next_prefix,
                        tiviDateFormatter.formatShortRelativeTime(nextToAir.firstAired)
                ))
            }
        }
        return text
    }

    fun seasonEpisodeTitleText(season: Season, episode: Episode): String {
        return context.getString(R.string.season_episode_number, season.number, episode.number)
    }

    fun episodeNumberText(episode: Episode): CharSequence? {
        return context.getString(R.string.episode_number, episode.number)
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
            context.getString(R.string.followed_watch_stats_to_watch,
                    stats.episodeCount - stats.watchedEpisodeCount).parseAsHtml()
        } else if (stats != null && stats.watchedEpisodeCount > 0) {
            context.getString(R.string.followed_watch_stats_complete)
        } else {
            return ""
        }
    }
}