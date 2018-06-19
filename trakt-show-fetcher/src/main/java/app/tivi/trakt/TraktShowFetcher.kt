/*
 * Copyright 2017 Google, Inc.
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

package app.tivi.trakt

import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.copyDynamic
import app.tivi.extensions.fetchBodyWithRetry
import com.uwetrottmann.trakt5.entities.Show
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Shows
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TraktShowFetcher @Inject constructor(
    private val showDao: TiviShowDao,
    private val showService: Provider<Shows>,
    private val entityInserter: EntityInserter
) {
    suspend fun updateShow(traktId: Int) {
        val response = showService.get().summary(traktId.toString(), Extended.FULL).fetchBodyWithRetry()
        upsertShow(response, true)
    }

    fun insertPlaceholderIfNeeded(show: Show): Long = upsertShow(show)

    private fun upsertShow(traktShow: Show, updateTime: Boolean = false): Long {
        return (showDao.getShowWithTraktId(traktShow.ids.trakt) ?: TiviShow()).copyDynamic {
            traktId = traktShow.ids.trakt
            tmdbId = traktShow.ids.tmdb
            title = traktShow.title
            summary = traktShow.overview
            homepage = traktShow.homepage
            rating = traktShow.rating?.toFloat()
            certification = traktShow.certification
            runtime = traktShow.runtime
            network = traktShow.network
            country = traktShow.country
            _genres = traktShow.genres?.joinToString(",")
            if (updateTime) lastTraktUpdate = OffsetDateTime.now()
        }.let {
            entityInserter.insertOrUpdate(showDao, it)
        }
    }
}