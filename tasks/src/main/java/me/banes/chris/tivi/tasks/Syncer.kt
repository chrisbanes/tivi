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

package me.banes.chris.tivi.tasks

import android.support.v4.util.ArraySet
import timber.log.Timber

class Syncer<NT, ET, ID>(
    private val currentIdsFunc: () -> Collection<ID>,
    private val entryFetchFunc: (ID) -> ET?,
    private val entryDeleteFunc: (ID) -> Unit,
    private val entryInsertFunc: (ET) -> Long,
    private val entryUpdateFunc: (ET) -> Unit,
    private val entityIdMapperFunc: (NT) -> ID,
    private val entityMapperFunc: (NT) -> ET,
    private val entityMergerFunc: (ET, ET) -> ET
) {
    fun sync(networkValues: Collection<NT>) {
        val currentIds = ArraySet<ID>(currentIdsFunc())
        Timber.d("Got current IDs as: $currentIds")

        networkValues.forEach { value ->
            val id = entityIdMapperFunc(value)
            val newEntry = entityMapperFunc(value)

            if (currentIds.contains(id)) {
                // This is currently in the DB, so lets merge it with the saved version and update it
                val dbEntry = entryFetchFunc(id)!!
                val merged = entityMergerFunc(dbEntry, newEntry)
                entryUpdateFunc(merged)
                Timber.d("Updated entry with id: $id")
            } else {
                // Not currently in the DB, so lets insert
                entryInsertFunc(newEntry)
                Timber.d("Insert entry with id: $id")
            }
            // Finally remove the id from the set
            currentIds.remove(id)
        }

        // Anything left in the set needs to be deleted from the database
        currentIds.forEach { id ->
            Timber.d("Remove entry with id: $id")
            entryDeleteFunc(id)
        }
    }
}