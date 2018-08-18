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

package app.tivi.data.repositories.shows

import app.tivi.data.RetrofitRunner
import app.tivi.data.entities.ErrorResult
import app.tivi.data.entities.Result
import app.tivi.data.entities.TiviShow
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.extensions.bodyOrThrow
import app.tivi.extensions.executeWithRetry
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.enums.IdType
import com.uwetrottmann.trakt5.enums.Type
import com.uwetrottmann.trakt5.services.Search
import com.uwetrottmann.trakt5.services.Shows
import javax.inject.Inject
import javax.inject.Provider

class TraktShowDataSource @Inject constructor(
    private val showService: Provider<Shows>,
    private val searchService: Provider<Search>,
    private val mapper: TraktShowToTiviShow,
    private val retrofitRunner: RetrofitRunner
) : ShowDataSource {
    override suspend fun getShow(show: TiviShow): Result<TiviShow> {
        var traktId = show.traktId

        if (traktId == null && show.tmdbId != null) {
            try {
                // We need to fetch the search for the trakt id
                val response = searchService.get().idLookup(IdType.TMDB, show.tmdbId.toString(),
                        Type.SHOW, Extended.NOSEASONS, 1, 1).executeWithRetry()
                if (response.isSuccessful) {
                    val body = response.bodyOrThrow()
                    if (body.isNotEmpty()) {
                        traktId = body[0].show.ids.trakt
                    }
                }
            } catch (e: Exception) {
                return ErrorResult(e)
            }
        }

        return if (traktId != null) {
            fetchFromTrakt(traktId.toString())
        } else {
            ErrorResult(IllegalArgumentException("TraktId for show does not exist: [$show]"))
        }
    }

    private suspend fun fetchFromTrakt(traktId: String) = retrofitRunner.executeForResponse(mapper) {
        showService.get().summary(traktId, Extended.FULL).executeWithRetry()
    }
}