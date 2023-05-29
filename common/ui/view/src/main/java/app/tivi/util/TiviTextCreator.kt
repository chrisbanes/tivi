// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import android.app.Activity
import android.graphics.Color
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.parseAsHtml
import androidx.emoji.text.EmojiCompat
import app.tivi.common.ui.resources.MR
import app.tivi.data.models.Episode
import app.tivi.data.models.Genre
import app.tivi.data.models.Season
import app.tivi.data.models.ShowStatus
import app.tivi.data.models.TiviShow
import app.tivi.ui.GenreStringer
import dev.icerock.moko.resources.format
import java.util.Locale
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toKotlinLocalTime
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

    fun showHeaderCount(count: Int, filtered: Boolean = false): CharSequence {
        return when {
            filtered -> MR.plurals.header_show_count_filtered.format(count, count).toString(context)
            else -> MR.plurals.header_show_count.format(count, count).toString(context)
        }.parseAsHtml()
    }

    fun followedShowEpisodeWatchStatus(
        episodeCount: Int,
        watchedEpisodeCount: Int,
    ): CharSequence = when {
        watchedEpisodeCount < episodeCount -> {
            MR.strings.followed_watch_stats_to_watch
                .format(episodeCount - watchedEpisodeCount)
                .toString(context)
                .parseAsHtml()
        }

        watchedEpisodeCount > 0 -> {
            MR.strings.followed_watch_stats_complete.getString(context)
        }

        else -> ""
    }

    fun seasonEpisodeTitleText(season: Season?, episode: Episode?): String {
        return if (season != null && episode != null) {
            MR.strings.season_episode_number
                .format(season.number!!, episode.number!!)
                .toString(context)
        } else {
            ""
        }
    }

    fun seasonTitle(
        season: Season,
    ): String = when {
        season.title != null -> season.title!!
        season.number != null -> {
            MR.strings.season_title_fallback.format(season.number!!).toString(context)
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
            text.append(MR.strings.season_summary_watched.format(watched).toString(context))
        }
        if (toWatch > 0) {
            if (text.isNotEmpty()) text.append(" \u2022 ")
            text.append(MR.strings.season_summary_to_watch.format(toWatch).toString(context))
        }
        if (toAir > 0) {
            if (text.isNotEmpty()) text.append(" \u2022 ")
            text.append(MR.strings.season_summary_to_air.format(toAir).toString(context))

            if (nextToAirDate != null) {
                text.append(". ")
                text.append(
                    MR.strings.next_prefix
                        .format(tiviDateFormatter.formatShortRelativeTime(nextToAirDate))
                        .toString(context),
                )
            }
        }
        return text
    }

    fun episodeNumberText(episode: Episode): CharSequence {
        val text = StringBuilder()
        text.append(MR.strings.episode_number.format(episode.number!!).toString(context))

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
                    append(GenreStringer.getLabel(genre).getString(context))
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
        return genres?.joinToString(", ") {
            GenreStringer.getLabel(it).getString(context)
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

        val localDateTime = java.time.ZonedDateTime.now(airTz.toJavaZoneId())
            .with(show.airsDay)
            .with(airTime.toJavaLocalTime())
            .withZoneSameInstant(java.time.ZoneId.systemDefault())

        return MR.strings.airs_text.format(
            localDateTime.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()),
            tiviDateFormatter.formatShortTime(localDateTime.toLocalTime().toKotlinLocalTime()),
        ).toString(context)
    }

    // TODO: change the string here, check if planned is still in Trakt
    fun showStatusText(status: ShowStatus): CharSequence = when (status) {
        ShowStatus.CANCELED, ShowStatus.ENDED -> MR.strings.status_ended.getString(context)
        ShowStatus.RETURNING -> MR.strings.status_active.getString(context)
        ShowStatus.IN_PRODUCTION -> MR.strings.status_in_production.getString(context)
        ShowStatus.PLANNED -> MR.strings.status_planned.getString(context)
    }
}
