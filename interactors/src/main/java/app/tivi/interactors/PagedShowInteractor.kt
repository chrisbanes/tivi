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

import kotlinx.coroutines.experimental.CoroutineDispatcher

interface PagedShowInteractor : Interactor<Int> {
    companion object {
        const val NEXT_PAGE = -1
        const val REFRESH = 0
    }

    val pageSize: Int

    fun asLoadMoreInteractor(): Interactor<Unit> = object : Interactor<Unit> {
        override val dispatcher: CoroutineDispatcher
            get() = this@PagedShowInteractor.dispatcher

        override suspend fun invoke(param: Unit) = invoke(NEXT_PAGE)
    }

    fun asRefreshInteractor(): Interactor<Unit> = object : Interactor<Unit> {
        override val dispatcher: CoroutineDispatcher
            get() = this@PagedShowInteractor.dispatcher

        override suspend fun invoke(param: Unit) = invoke(REFRESH)
    }
}