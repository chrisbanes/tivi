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

package app.tivi.util

import hu.akarnokd.kotlin.flow.BehaviorSubject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ObservableLoadingCounter @Inject constructor(
    val dispatchers: AppCoroutineDispatchers
) {
    private var loaders = 0
    private val loadingState = BehaviorSubject(loaders)

    val observable: Flow<Boolean>
        get() = loadingState.map { it > 0 }

    fun addLoader() {
        GlobalScope.launch(dispatchers.main) {
            loadingState.emit(++loaders)
        }
    }

    fun removeLoader() {
        GlobalScope.launch(dispatchers.main) {
            loadingState.emit(--loaders)
        }
    }
}