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

package app.tivi.interactors

import app.tivi.ShowFetcher
import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.RelatedShowsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.RelatedShowEntry
import app.tivi.data.entities.Request
import app.tivi.extensions.fetchBodyWithRetry
import app.tivi.extensions.parallelForEach
import app.tivi.util.AppCoroutineDispatchers
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Shows
import kotlinx.coroutines.experimental.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Provider

class FetchRelatedShowsInteractor @Inject constructor(
    private val lastRequests: LastRequestDao,
    private val showDao: TiviShowDao,
    private val entryDao: RelatedShowsDao,
    private val transactionRunner: DatabaseTransactionRunner,
    private val showsService: Provider<Shows>,
    private val dispatchers: AppCoroutineDispatchers,
    private val showFetcher: ShowFetcher
) : Interactor<FetchRelatedShowsInteractor.Params> {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend operator fun invoke(param: Params) {
        val traktId = showDao.getTraktIdForShowId(param.showId)?.toString()

        val results = showsService.get().related(traktId, 0, 10, Extended.NOSEASONS)
                .fetchBodyWithRetry()

        val related = results.mapIndexed { index, relatedShow ->
            // Now insert a placeholder for each show if needed
            val relatedShowId = showFetcher.insertPlaceholderIfNeeded(relatedShow)
            // Map to related show entry
            RelatedShowEntry(showId = param.showId, otherShowId = relatedShowId, orderIndex = index)
        }.also {
            // Save map entities to db
            transactionRunner.runInTransaction {
                entryDao.deleteWithShowId(param.showId)
                entryDao.insertAll(it)
            }
        }

        // Finally refresh each show
        related.parallelForEach(dispatcher) {
            // Now trigger a refresh of each show if it hasn't been refreshed before
            if (lastRequests.hasNotBeenRequested(Request.SHOW_DETAILS, it.otherShowId)) {
                showFetcher.update(it.otherShowId)
            }
        }
    }

    data class Params(val showId: Long, val forceLoad: Boolean)
}