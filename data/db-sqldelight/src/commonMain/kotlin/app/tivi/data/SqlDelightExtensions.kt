/*
 * Copyright 2023 Google LLC
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

package app.tivi.data

import app.cash.sqldelight.Transacter
import app.tivi.data.models.TiviEntity
import kotlinx.coroutines.CancellationException

internal fun <TX : Transacter, ET : TiviEntity> TX.upsert(
    entity: ET,
    insert: TX.(ET) -> Unit,
    update: TX.(ET) -> Unit,
    lastInsertRowId: TX.() -> Long,
    onConflict: (TX.(ET, Throwable) -> Long)? = null,
): Long = transactionWithResult {
    try {
        if (entity.id != 0L) {
            update(entity)
            entity.id
        } else {
            insert(entity)
            lastInsertRowId()
        }
    } catch (t: Throwable) {
        when {
            onConflict != null -> onConflict(entity, t)
            else -> throw t
        }
    }
}
