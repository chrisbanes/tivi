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

package app.tivi.showdetails.episodedetails

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import app.tivi.data.daos.EpisodeWatchEntryDao
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.datasources.trakt.EpisodeDetailsDataSource
import app.tivi.datasources.trakt.EpisodeWatchesDataSource
import app.tivi.interactors.RefreshEpisodeDetailsInteractor
import app.tivi.interactors.SyncShowWatchedEpisodesInteractor
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import app.tivi.util.TiviViewModel
import io.reactivex.Flowable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.plusAssign
import kotlinx.coroutines.experimental.withContext
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

class EpisodeDetailsViewModel @Inject constructor(
    private val episodeDetailsDataSource: EpisodeDetailsDataSource,
    private val episodeDetailsInteractor: RefreshEpisodeDetailsInteractor,
    private val episodeWatchesCall: EpisodeWatchesDataSource,
    private val tmdbManager: TmdbManager,
    private val logger: Logger,
    private val episodesDao: EpisodesDao,
    private val episodeWatchEntryDao: EpisodeWatchEntryDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val dateTimeFormatter: DateTimeFormatter,
    private val syncShowWatchedEpisodes: SyncShowWatchedEpisodesInteractor
) : TiviViewModel() {

    var episodeId: Long? = null
        set(value) {
            if (field != value) {
                field = value
                setupLiveData(value!!)
                refresh()
            }
        }

    private val _data = MutableLiveData<EpisodeDetailsViewState>()
    val data: LiveData<EpisodeDetailsViewState>
        get() = _data

    private fun refresh() {
        val epId = episodeId
        if (epId != null) {
            launchInteractor(episodeDetailsInteractor, epId)
        } else {
            _data.value = null
        }
    }

    private fun setupLiveData(episodeId: Long) {
        disposables.clear()

        val watches = episodeWatchesCall.data(episodeId)

        disposables += Flowables.combineLatest(
                episodeDetailsDataSource.data(episodeId),
                watches,
                tmdbManager.imageProvider,
                watches.map {
                    if (it.isEmpty()) {
                        EpisodeDetailsViewState.Action.WATCH
                    } else {
                        EpisodeDetailsViewState.Action.UNWATCH
                    }
                },
                Flowable.just(dateTimeFormatter),
                ::EpisodeDetailsViewState
        ).subscribe(_data::postValue, logger::e)
    }

    fun markWatched() {
        val epId = episodeId!!
        launchWithParent {
            withContext(dispatchers.io) {
                val entry = EpisodeWatchEntry(
                        episodeId = episodeId!!,
                        watchedAt = OffsetDateTime.now(),
                        pendingAction = EpisodeWatchEntry.PENDING_ACTION_UPLOAD
                )
                episodeWatchEntryDao.insert(entry)
            }
            syncShowWatchedEpisodes(episodesDao.showIdForEpisodeId(epId))
        }
    }

    fun markUnwatched() {
        val epId = episodeId!!
        launchWithParent {
            withContext(dispatchers.io) {
                val entries = episodeWatchEntryDao.watchesForEpisode(epId)
                entries.forEach {
                    // We have a trakt id, so we need to do a sync
                    if (it.pendingAction != EpisodeWatchEntry.PENDING_ACTION_DELETE) {
                        // If it is not set to be deleted, update it now
                        val copy = it.copy(pendingAction = EpisodeWatchEntry.PENDING_ACTION_DELETE)
                        episodeWatchEntryDao.update(copy)
                    }
                }
            }
            syncShowWatchedEpisodes(episodesDao.showIdForEpisodeId(epId))
        }
    }
}