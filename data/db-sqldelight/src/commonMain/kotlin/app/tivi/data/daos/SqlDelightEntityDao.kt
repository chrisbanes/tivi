// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.Database
import app.tivi.data.models.TiviEntity

interface SqlDelightEntityDao<in E : TiviEntity> : EntityDao<E> {
    val db: Database

    override fun insert(entities: List<E>) {
        db.transaction {
            for (entity in entities) {
                insert(entity)
            }
        }
    }

    override fun upsert(entities: List<E>) {
        db.transaction {
            for (entity in entities) {
                upsert(entity)
            }
        }
    }
}
