// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.util

import app.tivi.data.daos.EntityDao
import app.tivi.data.models.TiviEntity
import app.tivi.util.Logger

/**
 * @param NetworkType Network type
 * @param LocalType local entity type
 * @param Key Network ID type
 */
class ItemSyncer<LocalType : TiviEntity, NetworkType, Key>(
    private val upsertEntity: (LocalType) -> Long,
    private val deleteEntity: (LocalType) -> Unit,
    private val localEntityToKey: (LocalType) -> Key?,
    private val networkEntityToKey: (NetworkType) -> Key,
    private val networkEntityToLocalEntity: (networkEntity: NetworkType, currentEntity: LocalType?) -> LocalType,
    private val logger: Logger,
) {
    fun sync(
        currentValues: Collection<LocalType>,
        networkValues: Collection<NetworkType>,
        removeNotMatched: Boolean = true,
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
                val entity = networkEntityToLocalEntity(networkEntity, dbEntityForId)
                logger.v("Mapped network entity to local entity: %s", entity)
                if (dbEntityForId != entity) {
                    // This is currently in the DB, so lets merge it with the saved version
                    // and update it
                    upsertEntity(entity)
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
                logger.v("Deleted entry: %s", it)
                removed += it
            }
        }

        // Finally we can insert all of the new entities
        added.forEach {
            upsertEntity(it)
        }

        return ItemSyncerResult(added, removed, updated)
    }
}

data class ItemSyncerResult<ET : TiviEntity>(
    val added: List<ET> = emptyList(),
    val deleted: List<ET> = emptyList(),
    val updated: List<ET> = emptyList(),
)

fun <LocalType : TiviEntity, NetworkType, Key> syncerForEntity(
    entityDao: EntityDao<LocalType>,
    localEntityToKey: (LocalType) -> Key?,
    networkEntityToKey: (NetworkType) -> Key,
    networkEntityToLocalEntity: (networkEntity: NetworkType, currentEntity: LocalType?) -> LocalType,
    logger: Logger,
) = ItemSyncer(
    entityDao::upsert,
    entityDao::deleteEntity,
    localEntityToKey,
    networkEntityToKey,
    networkEntityToLocalEntity,
    logger,
)

fun <Type : TiviEntity, Key> syncerForEntity(
    entityDao: EntityDao<Type>,
    entityToKey: (Type) -> Key?,
    mapper: (Type, Type?) -> Type,
    logger: Logger,
) = ItemSyncer(
    entityDao::upsert,
    entityDao::deleteEntity,
    entityToKey,
    entityToKey,
    mapper,
    logger,
)
