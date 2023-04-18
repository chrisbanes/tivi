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

import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.tivi.data.models.TiviEntity
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

internal suspend inline fun <T : Any> Query<T>.awaitAsNull(context: CoroutineContext): T? {
    return asFlow().mapToOneOrNull(context).firstOrNull()
}

internal suspend inline fun <T : Any> Query<T>.await(context: CoroutineContext): T {
    return asFlow().mapToOne(context).first()
}

internal fun <TX : Transacter, ET : TiviEntity> TX.upsert(
    entity: ET,
    insert: TX.(ET) -> Unit,
    update: TX.(ET) -> Unit,
    lastInsertRowId: TX.() -> Long,
): Long = transactionWithResult {
    try {
        insert(entity)
        lastInsertRowId()
    } catch (e: Exception) {
        // TODO: make this exception more granular (just on SQL Constraints errors?)
        update(entity)
        entity.id
    }
}
