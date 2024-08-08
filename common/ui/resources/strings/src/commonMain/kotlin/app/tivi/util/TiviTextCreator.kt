// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.common.ui.resources.strings.Res
import app.tivi.common.ui.resources.strings.episodeNumber
import app.tivi.common.ui.resources.strings.followedWatchStatsComplete
import app.tivi.common.ui.resources.strings.followedWatchStatsToWatch
import app.tivi.common.ui.resources.strings.nextPrefix
import app.tivi.common.ui.resources.strings.seasonEpisodeNumber
import app.tivi.common.ui.resources.strings.seasonSummaryToAir
import app.tivi.common.ui.resources.strings.seasonSummaryToWatch
import app.tivi.common.ui.resources.strings.seasonSummaryWatched
import app.tivi.common.ui.resources.strings.seasonTitleFallback
import app.tivi.common.ui.resources.strings.statusActive
import app.tivi.common.ui.resources.strings.statusEnded
import app.tivi.common.ui.resources.strings.statusInProduction
import app.tivi.common.ui.resources.strings.statusPlanned
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

  fun followedShowEpisodeWatchStatus(
    episodeCount: Int,
    watchedEpisodeCount: Int,
  ): CharSequence = when {
    watchedEpisodeCount < episodeCount -> {
      getPluralStringBlocking(
        resource = Res.plurals.followedWatchStatsToWatch,
        quantity = episodeCount - watchedEpisodeCount,
      )
    }

    watchedEpisodeCount > 0 -> getStringBlocking(Res.string.followedWatchStatsComplete)
    else -> ""
  }

  fun seasonEpisodeTitleText(season: Season?, episode: Episode?): String = when {
    season != null && episode != null -> {
      getStringBlocking(Res.string.seasonEpisodeNumber, season.number!!, episode.number!!)
    }

    else -> ""
  }

  fun seasonTitle(
    season: Season,
  ): String = when {
    season.title != null -> season.title!!
    season.number != null -> getStringBlocking(Res.string.seasonTitleFallback, season.number!!)
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
      text.append(getStringBlocking(Res.string.seasonSummaryWatched, watched))
    }
    if (toWatch > 0) {
      if (text.isNotEmpty()) text.append(" \u2022 ")
      text.append(getStringBlocking(Res.string.seasonSummaryToWatch, toWatch))
    }
    if (toAir > 0) {
      if (text.isNotEmpty()) text.append(" \u2022 ")
      text.append(getStringBlocking(Res.string.seasonSummaryToAir, toAir))

      if (nextToAirDate != null) {
        text.append(". ")
        text.append(
          getStringBlocking(
            Res.string.nextPrefix,
            dateFormatter.formatShortRelativeTime(nextToAirDate),
          ),
        )
      }
    }
    return text
  }

  fun episodeNumberText(episode: Episode): CharSequence {
    val text = StringBuilder()
    text.append(getStringBlocking(Res.string.episodeNumber, episode.number!!))
    episode.firstAired?.also {
      text.append(" \u2022 ")
      text.append(dateFormatter.formatShortRelativeTime(date = it))
    }
    return text
  }

  fun genreLabel(genre: Genre): String = buildString {
    append(getStringBlocking(getGenreLabel(genre)))
    append("\u00A0") // nbsp
    append(GenreStringer.getEmoji(genre))
  }

  fun genreContentDescription(genres: List<Genre>?): CharSequence? {
    return genres?.joinToString(", ") {
      getStringBlocking(getGenreLabel(it))
    }
  }

  fun airsText(show: TiviShow): CharSequence?

  // TODO: change the string here, check if planned is still in Trakt
  fun showStatusText(status: ShowStatus): CharSequence = when (status) {
    ShowStatus.CANCELED, ShowStatus.ENDED -> Res.string.statusEnded
    ShowStatus.RETURNING -> Res.string.statusActive
    ShowStatus.IN_PRODUCTION -> Res.string.statusInProduction
    ShowStatus.PLANNED -> Res.string.statusPlanned
  }.let(::getStringBlocking)

  fun seasonEpisodeLabel(seasonNumber: Int, episodeNumber: Int): String {
    return "S${seasonNumber.toSeasonEpisodeString()}E${episodeNumber.toSeasonEpisodeString()}"
  }
}

private fun Int.toSeasonEpisodeString(): String = toString().padStart(2, '0')
