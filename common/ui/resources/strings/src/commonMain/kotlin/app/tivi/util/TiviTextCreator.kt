// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.common.ui.resources.TiviStrings
import app.tivi.data.models.Episode
import app.tivi.data.models.Genre
import app.tivi.data.models.Season
import app.tivi.data.models.ShowStatus
import app.tivi.data.models.TiviShow
import app.tivi.ui.GenreStringer
import app.tivi.ui.getGenreLabel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

expect class TiviTextCreator : CommonTiviTextCreator

interface CommonTiviTextCreator {
  val dateFormatter: TiviDateFormatter
  val strings: TiviStrings

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
      filtered -> strings.headerShowCountFiltered(count)
      else -> strings.headerShowCount(count)
    }
  }

  fun followedShowEpisodeWatchStatus(
    episodeCount: Int,
    watchedEpisodeCount: Int,
  ): CharSequence = when {
    watchedEpisodeCount < episodeCount -> {
      strings.followedWatchStatsToWatch(episodeCount - watchedEpisodeCount)
    }

    watchedEpisodeCount > 0 -> strings.followedWatchStatsComplete
    else -> ""
  }

  fun seasonEpisodeTitleText(season: Season?, episode: Episode?): String = when {
    season != null && episode != null -> {
      strings.seasonEpisodeNumber(season.number!!, episode.number!!)
    }

    else -> ""
  }

  fun seasonTitle(
    season: Season,
  ): String = when {
    season.title != null -> season.title!!
    season.number != null -> strings.seasonTitleFallback(season.number!!)
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
      text.append(strings.seasonSummaryWatched(watched))
    }
    if (toWatch > 0) {
      if (text.isNotEmpty()) text.append(" \u2022 ")
      text.append(strings.seasonSummaryToWatch(toWatch))
    }
    if (toAir > 0) {
      if (text.isNotEmpty()) text.append(" \u2022 ")
      text.append(strings.seasonSummaryToAir(toAir))

      if (nextToAirDate != null) {
        text.append(". ")
        text.append(
          strings.nextPrefix(dateFormatter.formatShortRelativeTime(nextToAirDate)),
        )
      }
    }
    return text
  }

  fun episodeNumberText(episode: Episode): CharSequence {
    val text = StringBuilder()
    text.append(strings.episodeNumber(episode.number!!))

    episode.firstAired?.also {
      text.append(" \u2022 ")
      text.append(dateFormatter.formatShortRelativeTime(date = it))
    }

    return text
  }

  fun genreLabel(genre: Genre): String = buildString {
    append(strings.getGenreLabel(genre))
    append("\u00A0") // nbsp
    append(GenreStringer.getEmoji(genre))
  }

  fun genreContentDescription(genres: List<Genre>?): CharSequence? {
    return genres?.joinToString(", ") {
      strings.getGenreLabel(it)
    }
  }

  fun airsText(show: TiviShow): CharSequence?

  // TODO: change the string here, check if planned is still in Trakt
  fun showStatusText(status: ShowStatus): CharSequence = when (status) {
    ShowStatus.CANCELED, ShowStatus.ENDED -> strings.statusEnded
    ShowStatus.RETURNING -> strings.statusActive
    ShowStatus.IN_PRODUCTION -> strings.statusInProduction
    ShowStatus.PLANNED -> strings.statusPlanned
  }

  fun seasonEpisodeLabel(seasonNumber: Int, episodeNumber: Int): String {
    return "S${seasonNumber.toSeasonEpisodeString()}E${episodeNumber.toSeasonEpisodeString()}"
  }
}

private fun Int.toSeasonEpisodeString(): String = toString().padStart(2, '0')
