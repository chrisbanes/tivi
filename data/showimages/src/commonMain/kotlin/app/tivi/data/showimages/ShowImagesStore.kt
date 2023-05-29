// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.showimages

import app.tivi.data.daos.ShowTmdbImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.saveImages
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.ShowTmdbImage
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
class ShowImagesStore(
    showTmdbImagesDao: ShowTmdbImagesDao,
    showDao: TiviShowDao,
    lastRequestStore: ShowImagesLastRequestStore,
    dataSource: ShowImagesDataSource,
    transactionRunner: DatabaseTransactionRunner,
) : Store<Long, List<ShowTmdbImage>> by StoreBuilder.from(
    fetcher = Fetcher.of { showId: Long ->
        val show = showDao.getShowWithId(showId)
        if (show?.tmdbId != null) {
            dataSource.getShowImages(show)
                .also { lastRequestStore.updateLastRequest(showId) }
                .map { it.copy(showId = showId) }
        } else {
            emptyList()
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { showId ->
            showTmdbImagesDao.getImagesForShowId(showId).map { entries ->
                when {
                    // Store only treats null as 'no value', so convert to null
                    entries.isEmpty() -> null
                    // If the request is expired, our data is stale
                    lastRequestStore.isRequestExpired(showId, 180.days) -> null
                    // Otherwise, our data is fresh (enough) and valid
                    else -> entries
                }
            }
        },
        writer = { showId, images ->
            transactionRunner {
                showTmdbImagesDao.saveImages(showId, images)
            }
        },
        delete = showTmdbImagesDao::deleteForShowId,
        deleteAll = showTmdbImagesDao::deleteAll,
    ),
).build()
