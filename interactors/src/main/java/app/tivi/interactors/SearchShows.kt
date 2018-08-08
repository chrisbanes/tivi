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

package app.tivi.interactors

import app.tivi.data.entities.TiviShow
import app.tivi.data.repositories.search.SearchRepository
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.AppRxSchedulers
import kotlinx.coroutines.experimental.CoroutineDispatcher
import javax.inject.Inject

class SearchShows @Inject constructor(
    private val searchRepository: SearchRepository,
    dispatchers: AppCoroutineDispatchers,
    private val schedulers: AppRxSchedulers
) : RxInteractor<SearchShows.Params, List<TiviShow>>() {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend fun execute(param: Params): List<TiviShow> {
        return searchRepository.search(param.query)
    }

    data class Params(val query: String)
}