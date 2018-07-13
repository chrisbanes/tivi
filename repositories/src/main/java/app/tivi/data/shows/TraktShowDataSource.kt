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

package app.tivi.data.shows

import app.tivi.data.entities.TiviShow
import app.tivi.extensions.fetchBodyWithRetry
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Shows
import javax.inject.Inject
import javax.inject.Provider

class TraktShowDataSource @Inject constructor(
    private val traktIdMapper: ShowTraktIdMapper,
    private val showService: Provider<Shows>
) : ShowDataSource {
    override suspend fun getShow(showId: Long): TiviShow {
        val traktId = traktIdMapper.map(showId)!!
        val traktShow = showService.get().summary(traktId.toString(), Extended.FULL).fetchBodyWithRetry()
        
        return TiviShow(
            traktId = traktShow.ids.trakt,
            tmdbId = traktShow.ids.tmdb,
            title = traktShow.title,
            summary = traktShow.overview,
            homepage = traktShow.homepage,
            rating = traktShow.rating?.toFloat(),
            certification = traktShow.certification,
            runtime = traktShow.runtime,
            network = traktShow.network,
            country = traktShow.country,
            firstAired = traktShow.first_aired,
            _genres = traktShow.genres?.joinToString(",")
        )
    }
}