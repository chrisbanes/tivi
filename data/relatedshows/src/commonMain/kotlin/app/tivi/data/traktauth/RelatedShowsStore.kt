// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import app.tivi.data.daos.RelatedShowsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.RelatedShowEntry
import app.tivi.inject.ApplicationScope
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder

@ApplicationScope
@Inject
class RelatedShowsStore(
    traktDataSource: TraktRelatedShowsDataSource,
    tmdbDataSource: TmdbRelatedShowsDataSource,
    relatedShowsDao: RelatedShowsDao,
    showDao: TiviShowDao,
    lastRequestStore: RelatedShowsLastRequestStore,
    transactionRunner: DatabaseTransactionRunner,
) : Store<Long, List<RelatedShowEntry>> by StoreBuilder.from(
    fetcher = Fetcher.of { showId: Long ->
        val tmdbResult = runCatching { tmdbDataSource(showId) }
        if (tmdbResult.isSuccess) {
            lastRequestStore.updateLastRequest(showId)
            return@of tmdbResult.getOrThrow()
        }

        val traktResult = runCatching { traktDataSource(showId) }
        if (traktResult.isSuccess) {
            lastRequestStore.updateLastRequest(showId)
        }
        traktResult.getOrThrow()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { showId ->
            relatedShowsDao.entriesObservable(showId).map { entries ->
                when {
                    // Store only treats null as 'no value', so convert to null
                    entries.isEmpty() -> null
                    // If the request is expired, our data is stale
                    lastRequestStore.isRequestExpired(showId, 28.days) -> null
                    // Otherwise, our data is fresh and valid
                    else -> entries
                }
            }
        },
        writer = { showId, response ->
            transactionRunner {
                val entries = response.map { (show, entry) ->
                    entry.copy(
                        showId = showId,
                        otherShowId = showDao.getIdOrSavePlaceholder(show),
                    )
                }
                relatedShowsDao.deleteWithShowId(showId)
                relatedShowsDao.upsert(entries)
            }
        },
        delete = relatedShowsDao::deleteWithShowId,
        deleteAll = relatedShowsDao::deleteAll,
    ),
).build()
