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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.tivi.common.ui.BuildConfig
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.DeliveryMode
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Success
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlin.reflect.KProperty1
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

/**
 * Simple ViewModel which exposes a [CompositeDisposable] which is automatically cleared/stopped when
 * the ViewModel is cleared.
 */
abstract class TiviMvRxViewModel<S : MvRxState>(
    initialState: S
) : BaseMvRxViewModel<S>(initialState, debugMode = BuildConfig.DEBUG) {

    private val liveData by lazy(LazyThreadSafetyMode.NONE) {
        MvRxStateLiveData<S> {
            subscribe { value = it }
        }
    }

    protected suspend fun <T> Flow<T>.execute(
        stateReducer: S.(Async<T>) -> S
    ) = execute({ it }, stateReducer)

    protected suspend fun <T, V> Flow<T>.execute(
        mapper: (T) -> V,
        stateReducer: S.(Async<V>) -> S
    ) {
        setState { stateReducer(Loading()) }

        @Suppress("USELESS_CAST")
        return map { Success(mapper(it)) as Async<V> }
            .catch {
                if (BuildConfig.DEBUG) {
                    Log.e(this@TiviMvRxViewModel::class.java.simpleName,
                        "Exception during observe", it)
                }
                emit(Fail(it))
            }
            .collect { setState { stateReducer(it) } }
    }

    fun observeAsLiveData(): LiveData<S> = liveData

    fun <A> selectObserve(
        owner: LifecycleOwner,
        prop1: KProperty1<S, A>,
        deliveryMode: DeliveryMode
    ): LiveData<A> = MvRxStateLiveData {
        selectSubscribe(owner, prop1, deliveryMode) { value = it }
    }

    private class MvRxStateLiveData<T>(
        private val subscribe: MvRxStateLiveData<T>.() -> Disposable
    ) : MutableLiveData<T>() {
        private var disposable: Disposable? = null

        override fun onActive() {
            disposable = subscribe()
        }

        override fun onInactive() {
            val d = disposable
            disposable = null

            if (d != null && !d.isDisposed) {
                d.dispose()
            }
        }
    }
}
