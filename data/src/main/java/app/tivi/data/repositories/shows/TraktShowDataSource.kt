/*
 * Copyright 2018 Google LLC
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

import app.tivi.data.entities.TiviShow
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.extensions.bodyOrThrow
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.enums.IdType
import com.uwetrottmann.trakt5.enums.Type
import com.uwetrottmann.trakt5.services.Search
import com.uwetrottmann.trakt5.services.Shows
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Provider

class TraktShowDataSource @Inject constructor(
    private val showService: Provider<Shows>,
    private val searchService: Provider<Search>,
    private val mapper: TraktShowToTiviShow,
) : ShowDataSource {
    override suspend fun getShow(show: TiviShow): TiviShow {
        var traktId = show.traktId

        if (traktId == null && show.tmdbId != null) {
            // We need to fetch the search for the trakt id
            traktId = searchService.get()
                .idLookup(
                    IdType.TMDB,
                    show.tmdbId.toString(),
                    Type.SHOW,
                    Extended.NOSEASONS,
                    1,
                    1
                )
                .awaitResponse()
                .let { it.body()?.getOrNull(0)?.show?.ids?.trakt }
        }

        if (traktId == null) {
            traktId = searchService.get()
                .textQueryShow(
                    show.title, null /* years */, null /* genres */,
                    null /* lang */, show.country /* countries */, null /* runtime */, null /* ratings */,
                    null /* certs */, show.network /* networks */, null /* status */,
                    Extended.NOSEASONS, 1, 1
                )
                .awaitResponse()
                .let { it.body()?.firstOrNull()?.show?.ids?.trakt }
        }

        return if (traktId != null) {
            showService.get()
                .summary(traktId.toString(), Extended.FULL)
                .awaitResponse()
                .let { mapper.map(it.bodyOrThrow()) }
        } else {
            throw IllegalArgumentException("Trakt ID for show does not exist: [$show]")
        }
    }
}
