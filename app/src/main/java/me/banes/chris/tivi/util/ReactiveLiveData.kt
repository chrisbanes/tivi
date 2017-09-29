/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.util

import android.arch.lifecycle.LiveData
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable

/**
 * Defines a [LiveData] object that wraps a [Flowable].
 * When the LiveData becomes active (the number of observers goes from 0 to 1), it subscribes to
 * the Flowable emissions. When the LiveData becomes inactive, the subscription is cleared.
 * LiveData holds the last value emitted by the Flowable, when the LiveData was active.
 * Therefore, when a new [Observer] is added, it will automatically notify with the last
 * value.
 *
 * @param <T> The type of data hold by this instance
 */
class ReactiveLiveData<T>(private val flowable: Flowable<T>) : LiveData<T>() {

    private val disposable: CompositeDisposable = CompositeDisposable()

    override fun onActive() {
        super.onActive()

        disposable.add(flowable.doOnSubscribe {
            // Don't worry about backpressure. If the stream is too noisy then backpressure can
            // be handled upstream.
            it.request(java.lang.Long.MAX_VALUE)
        }.subscribe(ReactiveLiveData@this::postValue, {
            // Errors should be handled upstream, so propagate as a crash.
            throw RuntimeException(it)
        }))
    }

    override fun onInactive() {
        super.onInactive()
        disposable.clear()
    }
}
