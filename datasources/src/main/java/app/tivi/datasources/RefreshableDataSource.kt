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

package app.tivi.datasources

import android.arch.paging.DataSource
import io.reactivex.Flowable

interface RefreshableDataSource<in Param, DatabaseOutput> {
    fun data(param: Param): Flowable<DatabaseOutput>
    suspend fun refresh(param: Param)
}

interface ListRefreshableDataSource<in Param, DatabaseOutput> : RefreshableDataSource<Param, List<DatabaseOutput>> {
    fun dataSourceFactory(): DataSource.Factory<Int, DatabaseOutput>
    val pageSize: Int
}