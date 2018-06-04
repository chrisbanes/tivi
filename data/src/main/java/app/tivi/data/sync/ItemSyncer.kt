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

package app.tivi.data.sync

import app.tivi.util.Logger

/**
 * @param NT Network type
 * @param ET local entity type
 * @param NID Network ID type
 */
class ItemSyncer<NT, ET, NID>(
    private val currentIdsFunc: () -> Collection<NID>,
    private val entryFetchFunc: (NID) -> ET?,
    private val entryDeleteFunc: (NID) -> Unit,
    private val entryInsertFunc: (ET) -> Long,
    private val entryUpdateFunc: (ET) -> Unit,
    private val entityIdMapperFunc: (NT) -> NID,
    private val entityMapperFunc: (NT) -> ET,
    private val entityMergerFunc: (ET, ET) -> ET,
    private val logger: Logger? = null
) {
    fun sync(networkValues: Collection<NT>) {
        val currentIds = HashSet<NID>(currentIdsFunc())
        logger?.d("Got current remote IDs as: $currentIds")

        networkValues.forEach { value ->
            val id = entityIdMapperFunc(value)
            val newEntry = entityMapperFunc(value)

            if (currentIds.contains(id)) {
                // This is currently in the DB, so lets merge it with the saved version and update it
                val dbEntry = entryFetchFunc(id)!!
                val merged = entityMergerFunc(dbEntry, newEntry)
                entryUpdateFunc(merged)
                logger?.d("Updated entry with remote id: $id")
            } else {
                // Not currently in the DB, so lets insert
                entryInsertFunc(newEntry)
                logger?.d("Insert entry with remote id: $id")
            }
            // Finally remove the id from the set
            currentIds.remove(id)
        }

        // Anything left in the set needs to be deleted from the database
        currentIds.forEach { id ->
            logger?.d("Remove entry with remote id: $id")
            entryDeleteFunc(id)
        }
    }
}