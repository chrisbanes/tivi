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
 * @param NetworkType Network type
 * @param LocalType local entity type
 * @param Key Network ID type
 */
class ItemSyncer<LocalType : TiviEntity, NetworkType, Key>(
    private val insertEntity: suspend (LocalType) -> Long,
    private val updateEntity: suspend (LocalType) -> Unit,
    private val deleteEntity: suspend (LocalType) -> Int,
    private val localEntityToKey: suspend (LocalType) -> Key?,
    private val networkEntityToKey: suspend (NetworkType) -> Key,
    private val networkEntityToLocalEntity: suspend (NetworkType, Long?) -> LocalType,
    private val logger: Logger
) {
    suspend fun sync(
        currentValues: Collection<LocalType>,
        networkValues: Collection<NetworkType>,
        removeNotMatched: Boolean = true
    ): ItemSyncerResult<LocalType> {
        val currentDbEntities = ArrayList(currentValues)

        val removed = ArrayList<LocalType>()
        val added = ArrayList<LocalType>()
        val updated = ArrayList<LocalType>()

        for (networkEntity in networkValues) {
            logger.v("Syncing item from network: %s", networkEntity)

            val remoteId = networkEntityToKey(networkEntity)
            logger.v("Mapped to remote ID: %s", remoteId)
            if (remoteId == null) {
                break
            }

            val dbEntityForId = currentDbEntities.find {
                localEntityToKey(it) == remoteId
            }
            logger.v("Matched database entity for remote ID %s : %s", remoteId, dbEntityForId)

            if (dbEntityForId != null) {
                val entity = networkEntityToLocalEntity(networkEntity, dbEntityForId.id)
                logger.v("Mapped network entity to local entity: %s", entity)
                if (dbEntityForId != entity) {
                    // This is currently in the DB, so lets merge it with the saved version
                    // and update it
                    updateEntity(entity)
                    logger.v("Updated entry with remote id: %s", remoteId)
                }
                // Remove it from the list so that it is not deleted
                currentDbEntities.remove(dbEntityForId)
                updated += entity
            } else {
                // Not currently in the DB, so lets insert
                added += networkEntityToLocalEntity(networkEntity, null)
            }
        }

        if (removeNotMatched) {
            // Anything left in the set needs to be deleted from the database
            currentDbEntities.forEach {
                deleteEntity(it)
                logger.v("Deleted entry: ", it)
                removed += it
            }
        }

        // Finally we can insert all of the new entities
        added.forEach {
            insertEntity(it)
        }

        return ItemSyncerResult(added, removed, updated)
    }
}

data class ItemSyncerResult<ET : TiviEntity>(
    val added: List<ET> = emptyList(),
    val deleted: List<ET> = emptyList(),
    val updated: List<ET> = emptyList()
)

fun <LocalType : TiviEntity, NetworkType, Key> syncerForEntity(
    entityDao: EntityDao<LocalType>,
    localEntityToKey: suspend (LocalType) -> Key?,
    networkEntityToKey: suspend (NetworkType) -> Key,
    networkEntityToLocalEntity: suspend (NetworkType, Long?) -> LocalType,
    logger: Logger
) = ItemSyncer(
    entityDao::insert,
    entityDao::update,
    entityDao::deleteEntity,
    localEntityToKey,
    networkEntityToKey,
    networkEntityToLocalEntity,
    logger
)

fun <Type : TiviEntity, Key> syncerForEntity(
    entityDao: EntityDao<Type>,
    entityToKey: suspend (Type) -> Key?,
    mapper: suspend (Type, Long?) -> Type,
    logger: Logger
) = ItemSyncer(
    entityDao::insert,
    entityDao::update,
    entityDao::deleteEntity,
    entityToKey,
    entityToKey,
    mapper,
    logger
)
