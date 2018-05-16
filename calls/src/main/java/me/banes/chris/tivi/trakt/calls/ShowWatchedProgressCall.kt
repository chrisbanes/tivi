/*
 * Copyright 2018 Google, Inc.
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

package me.banes.chris.tivi.trakt.calls

import com.uwetrottmann.trakt5.TraktV2
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.enums.HistoryType
import io.reactivex.Flowable
import kotlinx.coroutines.experimental.withContext
import me.banes.chris.tivi.calls.Call
import me.banes.chris.tivi.data.DatabaseTransactionRunner
import me.banes.chris.tivi.data.daos.EpisodeWatchEntryDao
import me.banes.chris.tivi.data.daos.EpisodesDao
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.entities.EpisodeWatchEntry
import me.banes.chris.tivi.extensions.fetchBodyWithRetry
import me.banes.chris.tivi.util.AppCoroutineDispatchers
import javax.inject.Inject

class ShowWatchedProgressCall @Inject constructor(
    private val episodeWatchEntryDao: EpisodeWatchEntryDao,
    private val episodesDao: EpisodesDao,
    private val tiviShowDao: TiviShowDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val trakt: TraktV2,
    private val databaseTransactionRunner: DatabaseTransactionRunner
) : Call<Long, Unit> {

    override fun data(param: Long): Flowable<Unit> = Flowable.empty()

    override suspend fun refresh(param: Long) {
        val show = withContext(dispatchers.database) {
            tiviShowDao.getShowWithId(param)
        } ?: throw IllegalArgumentException("Show with id: $param does not exist")

        // TODO fetch all un-synced watches from DB and send to Trakt

        // Fetch watched progress for show
        val watchedProgress = withContext(dispatchers.network) {
            // TODO use start and end dates
            trakt.users().history(UserSlug.ME, HistoryType.SHOWS, show.traktId!!,
                    0, 10000, Extended.NOSEASONS, null, null).fetchBodyWithRetry()
        }

        withContext(dispatchers.database) {
            databaseTransactionRunner.runInTransaction {
                // Probably want to delete all current

                watchedProgress.forEach { historyEntry ->
                    // We only care about episode watch entries
                    val traktEpisode = historyEntry.episode ?: return@forEach

                    // Fetch our entity for the history entry
                    val currentEntry = episodeWatchEntryDao.entryWithTraktId(historyEntry.id)

                    // Now fetch the episode entity from the database
                    val episode = episodesDao.episodeWithTraktId(traktEpisode.ids.trakt)
                    if (episode == null) {
                        throw IllegalArgumentException("Episode with id ${traktEpisode.ids.trakt} does not exist.")
                    }

                    if (currentEntry != null) {
                        // TODO update?
                    } else {
                        val entry = EpisodeWatchEntry(
                                episodeId = episode.id!!,
                                traktId = historyEntry.id,
                                watchedAt = historyEntry.watched_at,
                                source = EpisodeWatchEntry.SOURCE_TRAKT
                        )
                        episodeWatchEntryDao.insert(entry)
                    }
                }
            }
        }
    }
}