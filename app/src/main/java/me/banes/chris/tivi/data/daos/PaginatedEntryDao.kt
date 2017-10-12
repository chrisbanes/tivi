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

package me.banes.chris.tivi.data.daos

import io.reactivex.Flowable
import io.reactivex.Single
import me.banes.chris.tivi.data.PaginatedEntry

abstract class PaginatedEntryDao<EC : PaginatedEntry>(showDao: TiviShowDao) : EntryDao<EC>(showDao) {

    protected abstract fun entriesPageImpl(page: Int): Flowable<List<EC>>

    fun entriesPage(page: Int): Flowable<List<EC>> {
        return mapEntryShow(entriesPageImpl(page))
    }

    abstract fun deletePage(page: Int)

    abstract fun getLastPage(): Single<Int>
}

