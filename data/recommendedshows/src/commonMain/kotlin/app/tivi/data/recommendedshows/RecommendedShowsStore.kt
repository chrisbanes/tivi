// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.recommendedshows

import app.tivi.data.daos.RecommendedDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.daos.updatePage
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.RecommendedShowEntry
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
class RecommendedShowsStore(
    dataSource: RecommendedShowsDataSource,
    recommendedDao: RecommendedDao,
    showDao: TiviShowDao,
    lastRequestStore: RecommendedShowsLastRequestStore,
    transactionRunner: DatabaseTransactionRunner,
) : Store<Int, List<RecommendedShowEntry>> by StoreBuilder.from(
    fetcher = Fetcher.of { page: Int ->
        dataSource(page, 20)
            .also {
                if (page == 0) {
                    lastRequestStore.updateLastRequest()
                }
            }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { page ->
            recommendedDao.entriesForPage(page).map { entries ->
                when {
                    // Store only treats null as 'no value', so convert to null
                    entries.isEmpty() -> null
                    // If the request is expired, our data is stale
                    lastRequestStore.isRequestExpired(3.days) -> null
                    // Otherwise, our data is fresh and valid
                    else -> entries
                }
            }
        },
        writer = { page, response ->
            transactionRunner {
                val entries = response.map { show ->
                    val showId = showDao.getIdOrSavePlaceholder(show)
                    RecommendedShowEntry(showId = showId, page = page)
                }
                if (page == 0) {
                    // If we've requested page 0, remove any existing entries first
                    recommendedDao.deleteAll()
                    recommendedDao.upsert(entries)
                } else {
                    recommendedDao.updatePage(page, entries)
                }
            }
        },
        delete = recommendedDao::deletePage,
        deleteAll = recommendedDao::deleteAll,
    ),
).build()
