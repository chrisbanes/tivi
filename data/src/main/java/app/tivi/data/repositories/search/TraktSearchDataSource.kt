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

package app.tivi.data.repositories.search

import app.tivi.data.RetrofitRunner
import app.tivi.data.entities.Result
import app.tivi.data.entities.TiviShow
import app.tivi.data.mappers.TraktSearchResultToTiviShow
import app.tivi.data.mappers.toListMapper
import app.tivi.extensions.executeWithRetry
import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Search
import javax.inject.Inject
import javax.inject.Provider

class TraktSearchDataSource @Inject constructor(
    private val tmdb: Tmdb,
    private val searchService: Provider<Search>,
    private val mapper: TraktSearchResultToTiviShow,
    private val retrofitRunner: RetrofitRunner
) : SearchDataSource {
    override suspend fun search(query: String): Result<List<TiviShow>> {
        return retrofitRunner.executeForResponse(mapper.toListMapper()) {
            searchService.get().textQueryShow(query,
                    /* years */ null, /* genres */ null, /* langs */ null, /* country */ null, /* runtime */ null,
                    /* ratings */ null, /* certs */ null, /* networks */ null, /* status */ null,
                    Extended.NOSEASONS, 1, 25)
                    .executeWithRetry()
        }
    }
}