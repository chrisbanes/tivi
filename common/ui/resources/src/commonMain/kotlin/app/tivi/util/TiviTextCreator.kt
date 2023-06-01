// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.common.ui.resources.MR
import app.tivi.data.models.Episode
import app.tivi.data.models.Genre
import app.tivi.data.models.Season
import app.tivi.data.models.ShowStatus
import app.tivi.data.models.TiviShow
import app.tivi.ui.GenreStringer
import dev.icerock.moko.resources.desc.StringDesc
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect class TiviTextCreator : CommonTiviTextCreator

interface CommonTiviTextCreator {
    val dateFormatter: TiviDateFormatter

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
            filtered -> MR.plurals.header_show_count_filtered.format(count, count).asString()
            else -> MR.plurals.header_show_count.format(count, count).asString()
        }
    }

    fun followedShowEpisodeWatchStatus(
        episodeCount: Int,
        watchedEpisodeCount: Int,
    ): CharSequence = when {
        watchedEpisodeCount < episodeCount -> {
            MR.strings.followed_watch_stats_to_watch
                .format(episodeCount - watchedEpisodeCount)
                .asString()
        }
        watchedEpisodeCount > 0 -> {
            MR.strings.followed_watch_stats_complete.desc().asString()
        }
        else -> ""
    }

    fun seasonEpisodeTitleText(season: Season?, episode: Episode?): String {
        return if (season != null && episode != null) {
            MR.strings.season_episode_number
                .format(season.number!!, episode.number!!)
                .asString()
        } else {
            ""
        }
    }

    fun seasonTitle(
        season: Season,
    ): String = when {
        season.title != null -> season.title!!
        season.number != null -> {
            MR.strings.season_title_fallback.format(season.number!!).asString()
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
            text.append(MR.strings.season_summary_watched.format(watched).asString())
        }
        if (toWatch > 0) {
            if (text.isNotEmpty()) text.append(" \u2022 ")
            text.append(MR.strings.season_summary_to_watch.format(toWatch).asString())
        }
        if (toAir > 0) {
            if (text.isNotEmpty()) text.append(" \u2022 ")
            text.append(MR.strings.season_summary_to_air.format(toAir).asString())

            if (nextToAirDate != null) {
                text.append(". ")
                text.append(
                    MR.strings.next_prefix
                        .format(dateFormatter.formatShortRelativeTime(date = nextToAirDate))
                        .asString(),
                )
            }
        }
        return text
    }

    fun episodeNumberText(episode: Episode): CharSequence {
        val text = StringBuilder()
        text.append(MR.strings.episode_number.format(episode.number!!).asString())

        episode.firstAired?.also {
            text.append(" \u2022 ")
            text.append(dateFormatter.formatShortRelativeTime(date = it))
        }

        return text
    }

    fun genreString(genres: List<Genre>?): CharSequence? {
        if (!genres.isNullOrEmpty()) {
            return buildString {
                for (i in genres.indices) {
                    val genre = genres[i]
                    append(GenreStringer.getLabel(genre).desc().asString())
                    append("\u00A0") // nbsp
                    append(GenreStringer.getEmoji(genre))
                    if (i < genres.size - 1) append(" \u2022 ")
                }
            }
        }
        return null
    }

    fun genreContentDescription(genres: List<Genre>?): CharSequence? {
        return genres?.joinToString(", ") {
            GenreStringer.getLabel(it).desc().asString()
        }
    }

    fun airsText(show: TiviShow): CharSequence?

    // TODO: change the string here, check if planned is still in Trakt
    fun showStatusText(status: ShowStatus): CharSequence = when (status) {
        ShowStatus.CANCELED, ShowStatus.ENDED -> MR.strings.status_ended.desc().asString()
        ShowStatus.RETURNING -> MR.strings.status_active.desc().asString()
        ShowStatus.IN_PRODUCTION -> MR.strings.status_in_production.desc().asString()
        ShowStatus.PLANNED -> MR.strings.status_planned.desc().asString()
    }

    fun StringDesc.asString(): String
}
