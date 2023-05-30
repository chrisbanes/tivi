// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.shows

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.getShowWithIdOrThrow
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.TiviShow
import app.tivi.data.util.mergeShows
import app.tivi.data.util.storeBuilder
import app.tivi.inject.ApplicationScope
import kotlin.time.Duration.Companion.days
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.Validator

@ApplicationScope
@Inject
class ShowStore(
    showDao: TiviShowDao,
    lastRequestStore: ShowLastRequestStore,
    traktDataSource: TraktShowDataSource,
    tmdbDataSource: TmdbShowDataSource,
    transactionRunner: DatabaseTransactionRunner,
) : Store<Long, TiviShow> by storeBuilder(
    fetcher = Fetcher.of { id: Long ->
        val savedShow = showDao.getShowWithIdOrThrow(id)

        val traktResult = runCatching { traktDataSource.getShow(savedShow) }
        if (traktResult.isSuccess) {
            lastRequestStore.updateLastRequest(id)
            return@of traktResult.getOrThrow()
        }

        // If trakt fails, try TMDb
        val tmdbResult = runCatching { tmdbDataSource.getShow(savedShow) }
        if (tmdbResult.isSuccess) {
            lastRequestStore.updateLastRequest(id)
            return@of tmdbResult.getOrThrow()
        }

        throw traktResult.exceptionOrNull()!!
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { showId -> showDao.getShowWithIdFlow(showId) },
        writer = { id, response ->
            transactionRunner {
                showDao.upsert(
                    mergeShows(local = showDao.getShowWithIdOrThrow(id), trakt = response),
                )
            }
        },
        delete = showDao::delete,
        deleteAll = showDao::deleteAll,
    ),
).validator(
    Validator.by { lastRequestStore.isRequestValid(it.id, 14.days) },
).build()
