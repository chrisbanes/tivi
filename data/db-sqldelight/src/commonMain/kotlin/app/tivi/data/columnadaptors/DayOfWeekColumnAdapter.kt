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
import app.tivi.extensions.unsafeLazy
import kotlinx.datetime.DayOfWeek

internal object DayOfWeekColumnAdapter: ColumnAdapter<DayOfWeek, Long> {
    private val dayOfWeekValues by unsafeLazy { DayOfWeek.values() }

    override fun decode(databaseValue: Long): DayOfWeek {
        return dayOfWeekValues.first { it.value == databaseValue.toInt() }
    }

    override fun encode(value: DayOfWeek): Long = value.value.toLong()
}
