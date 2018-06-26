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

package app.tivi.util

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class RxLoadingCounter {
    private var loaders = 0
    private val loadingState = BehaviorSubject.createDefault(loaders)

    val observable: Observable<Boolean>
        get() = loadingState.map { it > 0 }

    fun addLoader() {
        loadingState.onNext(++loaders)
    }

    fun removeLoader() {
        loadingState.onNext(--loaders)
    }
}