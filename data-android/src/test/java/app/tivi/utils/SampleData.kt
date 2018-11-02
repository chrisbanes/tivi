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

package app.tivi.utils

import app.tivi.data.TiviDatabase
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.FollowedShowEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import org.threeten.bp.OffsetDateTime

const val showId = 1L
val show = TiviShow(id = showId, title = "Down Under", traktId = 243)

const val show2Id = 2L
val show2 = TiviShow(id = show2Id, title = "G'day mate", traktId = 546)

fun insertShow(db: TiviDatabase) = db.showDao().insert(show)

fun deleteShow(db: TiviDatabase) = db.showDao().delete(show)

const val seasonSpecialsId = 1L
val seasonSpecials = Season(
        id = seasonSpecialsId,
        showId = showId,
        title = "Specials",
        number = Season.NUMBER_SPECIALS,
        traktId = 7042
)

const val seasonOneId = 2L
val seasonOne = Season(
        id = seasonOneId,
        showId = showId,
        title = "Season 1",
        number = 1,
        traktId = 5443
)

const val seasonTwoId = 3L
val seasonTwo = Season(
        id = seasonTwoId,
        showId = showId,
        title = "Season 2",
        number = 2,
        traktId = 5434
)

fun deleteSeason(db: TiviDatabase) = db.seasonsDao().delete(seasonOne)

fun insertSeason(db: TiviDatabase) = db.seasonsDao().insert(seasonOne)

val episodeOne = Episode(id = 1, title = "Kangaroo Court", seasonId = seasonOne.id, number = 0, traktId = 59830)
val episodeTwo = Episode(id = 2, title = "Bushtucker", seasonId = seasonOne.id, number = 1, traktId = 33435)
val episodeThree = Episode(id = 3, title = "Wallaby Bungee", seasonId = seasonOne.id, number = 2, traktId = 44542)

val episodes = listOf(episodeOne, episodeTwo, episodeThree)

fun insertEpisodes(db: TiviDatabase) = episodes.forEach { db.episodesDao().insert(it) }

fun deleteEpisodes(db: TiviDatabase) = episodes.forEach { db.episodesDao().delete(it) }

const val episodeWatch1Id = 1L
val episodeWatch1 = EpisodeWatchEntry(
        id = episodeWatch1Id,
        watchedAt = OffsetDateTime.now(),
        episodeId = episodeOne.id,
        traktId = 435214
)

const val episodeWatch2Id = 2L
val episodeWatch2 = episodeWatch1.copy(id = episodeWatch2Id, traktId = 4385783)

val episodeWatch2PendingSend = episodeWatch2.copy(pendingAction = PendingAction.UPLOAD)
val episodeWatch2PendingDelete = episodeWatch2.copy(pendingAction = PendingAction.DELETE)

fun insertFollowedShow(db: TiviDatabase) = db.followedShowsDao().insert(followedShow1)

const val followedShowId = 1L
val followedShow1 = FollowedShowEntry(followedShowId, showId)
val followedShow1PendingDelete = followedShow1.copy(pendingAction = PendingAction.DELETE)
val followedShow1PendingUpload = followedShow1.copy(pendingAction = PendingAction.UPLOAD)

const val followedShow2Id = 2L
val followedShow2 = FollowedShowEntry(followedShow2Id, show2Id)