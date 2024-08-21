// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.common.ui.resources.Res
import app.tivi.common.ui.resources.episode_number
import app.tivi.common.ui.resources.followed_watch_stats_complete
import app.tivi.common.ui.resources.followed_watch_stats_to_watch
import app.tivi.common.ui.resources.getPluralStringBlocking
import app.tivi.common.ui.resources.getStringBlocking
import app.tivi.common.ui.resources.next_prefix
import app.tivi.common.ui.resources.season_episode_number
import app.tivi.common.ui.resources.season_summary_to_air
import app.tivi.common.ui.resources.season_summary_to_watch
import app.tivi.common.ui.resources.season_summary_watched
import app.tivi.common.ui.resources.season_title_fallback
import app.tivi.common.ui.resources.status_active
import app.tivi.common.ui.resources.status_ended
import app.tivi.common.ui.resources.status_in_production
import app.tivi.common.ui.resources.status_planned
import app.tivi.data.models.Episode
import app.tivi.data.models.Genre
import app.tivi.data.models.Season
import app.tivi.data.models.ShowStatus
import app.tivi.data.models.TiviShow
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect class TiviTextCreator : CommonTiviTextCreator

interface CommonTiviTextCreator {
  val dateFormatter: TiviDateFormatter

  fun showTitle(
    show: TiviShow,
  ): String = buildString {
    append(show.title)

    show.firstAired?.toLocalDateTime(TimeZone.currentSystemDefault())?.also { firstAired ->
      append(" ")
      append("(")
      append(firstAired.year.toString())
      append(")")
    }
  }

  fun followedShowEpisodeWatchStatus(
    episodeCount: Int,
    watchedEpisodeCount: Int,
  ): String = when {
    watchedEpisodeCount < episodeCount -> {
      val count = episodeCount - watchedEpisodeCount
      getPluralStringBlocking(
        resource = Res.plurals.followed_watch_stats_to_watch,
        quantity = count,
        count,
      )
    }

    watchedEpisodeCount > 0 -> getStringBlocking(Res.string.followed_watch_stats_complete)
    else -> ""
  }

  fun seasonEpisodeTitleText(season: Season?, episode: Episode?): String = when {
    season != null && episode != null -> {
      getStringBlocking(Res.string.season_episode_number, season.number!!, episode.number!!)
    }

    else -> ""
  }

  fun seasonTitle(
    season: Season,
  ): String = when {
    season.title != null -> season.title!!
    season.number != null -> getStringBlocking(Res.string.season_title_fallback, season.number!!)
    else -> ""
  }

  fun seasonSummaryText(
    watched: Int,
    toWatch: Int,
    toAir: Int,
    nextToAirDate: Instant? = null,
  ): CharSequence = buildString {
    if (watched > 0) {
      append(getStringBlocking(Res.string.season_summary_watched, watched))
    }
    if (toWatch > 0) {
      if (isNotEmpty()) append(" \u2022 ")
      append(getStringBlocking(Res.string.season_summary_to_watch, toWatch))
    }
    if (toAir > 0) {
      if (isNotEmpty()) append(" \u2022 ")
      append(getStringBlocking(Res.string.season_summary_to_air, toAir))

      if (nextToAirDate != null) {
        append(". ")
        append(
          getStringBlocking(
            Res.string.next_prefix,
            dateFormatter.formatShortRelativeTime(nextToAirDate),
          ),
        )
      }
    }
  }

  fun episodeNumberText(episode: Episode): CharSequence = buildString {
    append(getStringBlocking(Res.string.episode_number, episode.number!!))
    episode.firstAired?.also {
      append(" \u2022 ")
      append(dateFormatter.formatShortRelativeTime(date = it))
    }
  }

  fun genreLabel(genre: Genre): String = buildString {
    append(getStringBlocking(getGenreLabel(genre)))
    append("\u00A0") // nbsp
    append(GenreStringer.getEmoji(genre))
  }

  fun genreContentDescription(genres: List<Genre>?): String? {
    return genres?.joinToString(", ") {
      getStringBlocking(getGenreLabel(it))
    }
  }

  fun airsText(show: TiviShow): String?

  // TODO: change the string here, check if planned is still in Trakt
  fun showStatusText(status: ShowStatus): String = getStringBlocking(
    when (status) {
      ShowStatus.CANCELED, ShowStatus.ENDED -> Res.string.status_ended
      ShowStatus.RETURNING -> Res.string.status_active
      ShowStatus.IN_PRODUCTION -> Res.string.status_in_production
      ShowStatus.PLANNED -> Res.string.status_planned
    },
  )

  fun seasonEpisodeLabel(seasonNumber: Int, episodeNumber: Int): String {
    return "S${seasonNumber.toSeasonEpisodeString()}E${episodeNumber.toSeasonEpisodeString()}"
  }
}

private fun Int.toSeasonEpisodeString(): String = toString().padStart(2, '0')
