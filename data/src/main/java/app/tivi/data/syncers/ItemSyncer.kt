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

package app.tivi.data.syncers

import app.tivi.data.daos.EntityDao
import app.tivi.data.entities.TiviEntity
import app.tivi.util.Logger

/**
 * @param NT Network type
 * @param ET local entity type
 * @param NID Network ID type
 */
class ItemSyncer<ET : TiviEntity, NT, NID>(
    private val entryInsertFunc: suspend (ET) -> Long,
    private val entryUpdateFunc: suspend (ET) -> Unit,
    private val entryDeleteFunc: suspend (ET) -> Int,
    private val localEntityToIdFunc: suspend (ET) -> NID?,
    private val networkEntityToIdFunc: suspend (NT) -> NID,
    private val networkEntityToLocalEntityMapperFunc: suspend (NT, Long?) -> ET,
    private val logger: Logger
) {
    suspend fun sync(
        currentValues: Collection<ET>,
        networkValues: Collection<NT>,
        removeNotMatched: Boolean = true
    ): ItemSyncerResult<ET> {
        val currentDbEntities = ArrayList(currentValues)

        val removed = ArrayList<ET>()
        val added = ArrayList<ET>()
        val updated = ArrayList<ET>()

        for (networkEntity in networkValues) {
            logger.v("Syncing item from network: %s", networkEntity)

            val remoteId = networkEntityToIdFunc(networkEntity)
            logger.v("Mapped to remote ID: %s", remoteId)
            if (remoteId == null) {
                break
            }

            val dbEntityForId = currentDbEntities.find { localEntityToIdFunc(it) == remoteId }
            logger.v("Matched database entity for remote ID %s : %s", remoteId, dbEntityForId)

            if (dbEntityForId != null) {
                val entity = networkEntityToLocalEntityMapperFunc(networkEntity, dbEntityForId.id)
                logger.v("Mapped network entity to local entity: %s", entity)
                if (dbEntityForId != entity) {
                    // This is currently in the DB, so lets merge it with the saved version and update it
                    entryUpdateFunc(entity)
                    logger.v("Updated entry with remote id: %s", remoteId)
                }
                // Remove it from the list so that it is not deleted
                currentDbEntities.remove(dbEntityForId)
                updated += entity
            } else {
                // Not currently in the DB, so lets insert
                added += networkEntityToLocalEntityMapperFunc(networkEntity, null)
            }
        }

        if (removeNotMatched) {
            // Anything left in the set needs to be deleted from the database
            currentDbEntities.forEach {
                entryDeleteFunc(it)
                logger.v("Deleted entry: ", it)
                removed += it
            }
        }

        // Finally we can insert all of the new entities
        added.forEach {
            entryInsertFunc(it)
        }

        return ItemSyncerResult(added, removed, updated)
    }

    data class ItemSyncerResult<ET : TiviEntity>(
        val added: List<ET>,
        val deleted: List<ET>,
        val updated: List<ET>
    )
}

fun <ET : TiviEntity, NT, NID> syncerForEntity(
    entityDao: EntityDao<ET>,
    localEntityToIdFunc: suspend (ET) -> NID?,
    networkEntityToIdFunc: suspend (NT) -> NID,
    networkEntityToLocalEntityMapperFunc: suspend (NT, Long?) -> ET,
    logger: Logger
) = ItemSyncer(
    entityDao::insert,
    entityDao::update,
    entityDao::delete,
    localEntityToIdFunc,
    networkEntityToIdFunc,
    networkEntityToLocalEntityMapperFunc,
    logger
)

fun <ET : TiviEntity, NID> syncerForEntity(
    entityDao: EntityDao<ET>,
    entityToIdFunc: suspend (ET) -> NID?,
    mapper: suspend (ET, Long?) -> ET,
    logger: Logger
) = ItemSyncer(
    entityDao::insert,
    entityDao::update,
    entityDao::delete,
    entityToIdFunc,
    entityToIdFunc,
    mapper,
    logger
)
