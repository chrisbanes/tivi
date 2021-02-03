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

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KProperty1

abstract class ReduxViewModel<S> constructor(initialState: S) : ViewModel() {
    private val state = MutableStateFlow(initialState)
    private val stateMutex = Mutex()

    /**
     * Returns a snapshot of the current state.
     */
    fun currentState(): S = state.value

    val liveData: LiveData<S>
        get() = state.asLiveData()

    protected suspend fun <T> Flow<T>.collectAndSetState(reducer: S.(T) -> S) {
        return collect { item -> setState { reducer(item) } }
    }

    fun <A> selectObserve(prop1: KProperty1<S, A>): LiveData<A> {
        return selectSubscribe(prop1).asLiveData()
    }

    protected fun subscribe(block: (S) -> Unit) {
        viewModelScope.launch {
            state.collect { block(it) }
        }
    }

    protected fun <A> selectSubscribe(prop1: KProperty1<S, A>, block: (A) -> Unit) {
        viewModelScope.launch {
            selectSubscribe(prop1).collect { block(it) }
        }
    }

    private fun <A> selectSubscribe(prop1: KProperty1<S, A>): Flow<A> {
        return state.map { prop1.get(it) }.distinctUntilChanged()
    }

    protected suspend fun setState(reducer: S.() -> S) {
        stateMutex.withLock {
            state.value = reducer(state.value)
        }
    }

    protected fun CoroutineScope.launchSetState(reducer: S.() -> S) {
        launch { this@ReduxViewModel.setState(reducer) }
    }

    protected suspend fun withState(block: (S) -> Unit) {
        stateMutex.withLock {
            block(state.value)
        }
    }

    protected fun CoroutineScope.withState(block: (S) -> Unit) {
        launch { this@ReduxViewModel.withState(block) }
    }
}
