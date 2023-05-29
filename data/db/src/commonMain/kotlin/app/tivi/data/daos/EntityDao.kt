// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.models.TiviEntity

interface EntityDao<in E : TiviEntity> {
    fun insert(entity: E): Long
    fun insert(entities: List<E>)

    fun update(entity: E)
    fun upsert(entity: E): Long = upsert(entity, ::insert, ::update)

    fun upsert(entities: List<E>)

    fun deleteEntity(entity: E)
}

fun <E : TiviEntity> EntityDao<E>.insert(vararg entities: E) = insert(entities.toList())
fun <E : TiviEntity> EntityDao<E>.upsert(vararg entities: E) = upsert(entities.toList())

fun <ET : TiviEntity> upsert(
    entity: ET,
    insert: (ET) -> Long,
    update: (ET) -> Unit,
    onConflict: ((ET, Throwable) -> Long)? = null,
): Long {
    return try {
        if (entity.id != 0L) {
            update(entity)
            entity.id
        } else {
            insert(entity)
        }
    } catch (t: Throwable) {
        when {
            onConflict != null -> onConflict(entity, t)
            else -> throw t
        }
    }
}
