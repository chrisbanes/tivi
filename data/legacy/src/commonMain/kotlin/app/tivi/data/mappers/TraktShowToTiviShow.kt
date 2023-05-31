// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktShow
import app.tivi.data.models.TiviShow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import me.tatarka.inject.annotations.Inject

@Inject
class TraktShowToTiviShow(
    private val statusMapper: TraktStatusToShowStatus,
) : Mapper<TraktShow, TiviShow> {

    override fun map(from: TraktShow) = TiviShow(
        traktId = from.ids?.trakt,
        tmdbId = from.ids?.tmdb,
        imdbId = from.ids?.imdb,
        title = from.title,
        summary = from.overview,
        homepage = from.homepage,
        traktRating = from.rating?.toFloat(),
        traktVotes = from.votes,
        certification = from.certification,
        runtime = from.runtime,
        network = from.network,
        country = from.country,
        firstAired = from.firstAired,
        _genres = from.genres.joinToString(","),
        status = from.status?.let { statusMapper.map(it) },
        airsDay = from.airs?.day?.parseToDayOfWeek(),
        airsTime = from.airs?.time?.let {
            try {
                LocalTime.parse(it)
            } catch (e: Exception) {
                null
            }
        },
        airsTimeZone = from.airs?.timezone?.let {
            try {
                TimeZone.of(it)
            } catch (e: Exception) {
                null
            }
        },
    )
}

fun String.parseToDayOfWeek(): DayOfWeek? = when (this.lowercase()) {
    "monday" -> DayOfWeek.MONDAY
    "tuesday" -> DayOfWeek.TUESDAY
    "wednesday" -> DayOfWeek.WEDNESDAY
    "thursday" -> DayOfWeek.THURSDAY
    "friday" -> DayOfWeek.FRIDAY
    "saturday" -> DayOfWeek.SATURDAY
    "sunday" -> DayOfWeek.SUNDAY
    else -> null
}
