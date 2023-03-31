/*
 * Copyright 2017 Google LLC
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

package app.tivi.data.daos

import app.tivi.data.compoundmodels.EntryWithShow
import app.tivi.data.models.PaginatedEntry

interface PaginatedEntryDao<EC : PaginatedEntry, LI : EntryWithShow<EC>> : EntryDao<EC, LI> {
    override suspend fun upsert(entity: EC): Long
    override suspend fun upsertAll(vararg entity: EC)
    override suspend fun upsertAll(entities: List<EC>)
    suspend fun deletePage(page: Int)
    suspend fun getLastPage(): Int?
}

suspend fun <EC : PaginatedEntry, LI : EntryWithShow<EC>> PaginatedEntryDao<EC, LI>.updatePage(
    page: Int,
    entities: List<EC>,
) {
    deletePage(page)
    upsertAll(entities)
}
