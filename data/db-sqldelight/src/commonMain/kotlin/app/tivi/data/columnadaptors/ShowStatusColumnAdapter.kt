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

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import app.tivi.data.models.ShowStatus
import app.tivi.extensions.unsafeLazy

internal object ShowStatusColumnAdapter : ColumnAdapter<ShowStatus, String> {
    private val showStatusValues by unsafeLazy { ShowStatus.values() }

    override fun decode(databaseValue: String): ShowStatus {
        return showStatusValues.first { it.storageKey == databaseValue }
    }

    override fun encode(value: ShowStatus): String = value.storageKey
}
