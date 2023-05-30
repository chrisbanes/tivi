// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.relatedshows

import app.tivi.data.daos.RelatedShowsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.RelatedShowEntry
import app.tivi.data.util.storeBuilder
import app.tivi.inject.ApplicationScope
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.Validator

@ApplicationScope
@Inject
class RelatedShowsStore(
    traktDataSource: TraktRelatedShowsDataSource,
    tmdbDataSource: TmdbRelatedShowsDataSource,
    relatedShowsDao: RelatedShowsDao,
    showDao: TiviShowDao,
    lastRequestStore: RelatedShowsLastRequestStore,
    transactionRunner: DatabaseTransactionRunner,
) : Store<Long, RelatedShows> by storeBuilder(
    fetcher = Fetcher.of { showId: Long ->
        runCatching { tmdbDataSource(showId) }
            .let { tmdbResult ->
                when {
                    tmdbResult.isSuccess -> tmdbResult
                    else -> runCatching { traktDataSource(showId) }
                }
            }
            .getOrThrow()
            .let { result ->
                transactionRunner {
                    lastRequestStore.updateLastRequest(showId)

                    result.map { (show, entry) ->
                        entry.copy(
                            showId = showId,
                            otherShowId = showDao.getIdOrSavePlaceholder(show),
                        )
                    }
                }
            }
            .let { RelatedShows(showId, it) }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { showId ->
            relatedShowsDao.entriesObservable(showId)
                .map { RelatedShows(showId, it) }
        },
        writer = { showId, response ->
            transactionRunner {
                relatedShowsDao.deleteWithShowId(showId)
                relatedShowsDao.upsert(response.related)
            }
        },
        delete = relatedShowsDao::deleteWithShowId,
        deleteAll = relatedShowsDao::deleteAll,
    ),
).validator(
    Validator.by { lastRequestStore.isRequestValid(it.showId, 28.days) },
).build()

data class RelatedShows(val showId: Long, val related: List<RelatedShowEntry>)
