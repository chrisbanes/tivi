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

package app.tivi.tmdb

import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.copyDynamic
import app.tivi.extensions.fetchBodyWithRetry
import com.uwetrottmann.tmdb2.Tmdb
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbShowFetcher @Inject constructor(
    private val showDao: TiviShowDao,
    private val tmdb: Tmdb,
    private val entityInserter: EntityInserter
) {
    suspend fun updateShow(tmdbId: Int) {
        val tmdbShow = tmdb.tvService().tv(tmdbId).fetchBodyWithRetry()

        val show = (showDao.getShowWithTmdbId(tmdbShow.id) ?: TiviShow()).copyDynamic {
            this.tmdbId = tmdbShow.id
            if (title.isNullOrEmpty()) title = tmdbShow.name
            if (summary.isNullOrEmpty()) summary = tmdbShow.overview
            tmdbBackdropPath = tmdbShow.backdrop_path
            tmdbPosterPath = tmdbShow.poster_path
            if (homepage.isNullOrEmpty()) homepage = tmdbShow.homepage
        }

        entityInserter.insertOrUpdate(showDao, show)
    }
}
