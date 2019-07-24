/*
 * Copyright 2019 Google LLC
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

package app.tivi

import app.tivi.base.BuildConfig
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Success
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

/**
 * Simple ViewModel which exposes a [CompositeDisposable] which is automatically cleared/stopped when
 * the ViewModel is cleared.
 */
open class TiviMvRxViewModel<S : MvRxState>(
    initialState: S
) : BaseMvRxViewModel<S>(initialState, debugMode = BuildConfig.DEBUG) {

    suspend fun <T> Flow<T>.execute(stateReducer: S.(Async<T>) -> S) {
        execute({ it }, stateReducer)
    }

    suspend fun <T, V> Flow<T>.execute(
        mapper: (T) -> V,
        stateReducer: S.(Async<V>) -> S
    ) {
        setState { stateReducer(Loading()) }

        @Suppress("USELESS_CAST")
        return map { value -> Success(mapper(value)) as Async<V> }
                .catch { emit(Fail(it)) }
                .collect { async -> setState { stateReducer(async) } }
    }
}
