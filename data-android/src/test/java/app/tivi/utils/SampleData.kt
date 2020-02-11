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
import app.tivi.data.entities.ImageType
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Season
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

const val showId = 1L
val show = TiviShow(id = showId, title = "Down Under", traktId = 243)

const val show2Id = 2L
val show2 = TiviShow(id = show2Id, title = "G'day mate", traktId = 546)

internal suspend fun insertShow(db: TiviDatabase) = db.showDao().insert(show)

internal suspend fun deleteShow(db: TiviDatabase) = db.showDao().deleteEntity(show)

const val s1_id = 1L
val s1 = Season(
    id = s1_id,
    showId = showId,
    title = "Season 1",
    number = 1,
    traktId = 5443
)

const val s2_id = 2L
val s2 = Season(
    id = s2_id,
    showId = showId,
    title = "Season 2",
    number = 2,
    traktId = 5434
)

const val s0_id = 3L
val s0 = Season(
    id = s0_id,
    showId = showId,
    title = "Specials",
    number = Season.NUMBER_SPECIALS,
    traktId = 7042
)

private val s1e1AirDate = OffsetDateTime.of(2000, 7, 1, 18, 0, 0, 0, ZoneOffset.UTC)

val s1e1 = Episode(id = 1, title = "Kangaroo Court", seasonId = s1.id, number = 0, traktId = 59830, firstAired = s1e1AirDate)
val s1e2 = Episode(id = 2, title = "Bushtucker", seasonId = s1.id, number = 1, traktId = 33435, firstAired = s1e1AirDate.plusWeeks(1))
val s1e3 = Episode(id = 3, title = "Wallaby Bungee", seasonId = s1.id, number = 2, traktId = 44542, firstAired = s1e1AirDate.plusWeeks(2))

val s2e1 = Episode(id = 4, title = "Noosa Pool", seasonId = s2.id, number = 0, traktId = 5656, firstAired = s1e1AirDate.plusWeeks(3))
val s2e2 = Episode(id = 5, title = "Alice Springer", seasonId = s2.id, number = 1, traktId = 8731, firstAired = s1e1AirDate.plusWeeks(4))

val s1_episodes = listOf(s1e1, s1e2, s1e3)
val s2_episodes = listOf(s2e1, s2e2)

const val s1e1w_id = 1L
val s1e1w = EpisodeWatchEntry(
    id = s1e1w_id,
    watchedAt = OffsetDateTime.now(),
    episodeId = s1e1.id,
    traktId = 435214
)

const val s1e1w2_id = 2L
val s1e1w2 = s1e1w.copy(id = s1e1w2_id, traktId = 4385783)

val episodeWatch2PendingSend = s1e1w2.copy(pendingAction = PendingAction.UPLOAD)
val episodeWatch2PendingDelete = s1e1w2.copy(pendingAction = PendingAction.DELETE)

internal suspend fun insertFollowedShow(db: TiviDatabase) = db.followedShowsDao().insert(followedShow1Local)

const val followedShowId = 1L
val followedShow1Network = FollowedShowEntry(0, showId, traktId = 100)
val followedShow1Local = followedShow1Network.copy(id = followedShowId)
val followedShow1PendingDelete = followedShow1Local.copy(pendingAction = PendingAction.DELETE)
val followedShow1PendingUpload = followedShow1Local.copy(pendingAction = PendingAction.UPLOAD)

const val followedShow2Id = 2L
val followedShow2Network = FollowedShowEntry(0, show2Id, traktId = 101)
val followedShow2Local = followedShow2Network.copy(id = followedShow2Id)

val showPoster = ShowTmdbImage(showId = 0, path = "/folder/fake.jpg", type = ImageType.POSTER)
