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

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.tivi.common.ui.BuildConfig
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KProperty1

abstract class ReduxViewModel<S> : ViewModel() {
    private val stateMutex = Mutex()
    internal var state: S = createInitialState()
    private val stateChannel = ConflatedBroadcastChannel(state)

    private val _liveData = MutableLiveData<S>()

    val liveData: LiveData<S>
        get() = _liveData

    protected suspend fun <T> Flow<T>.execute(
        reducer: S.(Async<T>) -> S
    ) = execute({ it }, reducer)

    protected suspend fun <T, V> Flow<T>.execute(
        mapper: (T) -> V,
        reducer: S.(Async<V>) -> S
    ) {
        setState { reducer(Loading()) }

        @Suppress("USELESS_CAST")
        return map { Success(mapper(it)) as Async<V> }
            .catch { e ->
                if (BuildConfig.DEBUG) {
                    Log.e(
                        this@ReduxViewModel::class.java.simpleName,
                        "Exception during observe",
                        e
                    )
                }
                emit(Fail(e))
            }
            .collect { setState { reducer(it) } }
    }

    fun observeAsLiveData(): LiveData<S> = liveData

    fun <A> selectObserve(prop1: KProperty1<S, A>): LiveData<A> {
        return selectSubscribe(prop1).asLiveData()
    }

    protected fun subscribe(): Flow<S> {
        return stateChannel.asFlow().distinctUntilChanged()
    }

    protected fun subscribe(block: (S) -> Unit) {
        viewModelScope.launch {
            subscribe().collect { block(it) }
        }
    }

    protected fun <A> selectSubscribe(prop1: KProperty1<S, A>): Flow<A> {
        return stateChannel.asFlow()
            .map { prop1.get(it) }
            .distinctUntilChanged()
    }

    protected suspend fun setStateSuspend(reducer: S.() -> S) {
        stateMutex.withLock {
            state = reducer(state)
            stateChannel.offer(state)
        }
    }

    protected fun setState(reducer: S.() -> S) {
        viewModelScope.launch { setStateSuspend(reducer) }
    }

    protected suspend fun withStateSuspend(block: (S) -> Unit) {
        stateMutex.withLock { block(state) }
    }

    protected fun withState(block: (S) -> Unit) {
        viewModelScope.launch { withStateSuspend(block) }
    }

    protected abstract fun createInitialState(): S
}

fun <VM : ReduxViewModel<S>, S> withState(viewModel: VM, block: (S) -> Unit) = block(viewModel.state)
