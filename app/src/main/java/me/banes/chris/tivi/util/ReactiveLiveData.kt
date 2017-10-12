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
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.lang.ref.WeakReference

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
class ReactiveLiveData<T>(private val publisher: Publisher<T>) : LiveData<T>() {
    private var subRef: WeakReference<Subscription>? = null

    override fun onActive() {
        super.onActive()

        publisher.subscribe(object : Subscriber<T> {
            override fun onSubscribe(s: Subscription) {
                // Don't worry about backpressure. If the stream is too noisy then
                // backpressure can be handled upstream.
                s.request(java.lang.Long.MAX_VALUE)
                subRef = WeakReference(s)
            }

            override fun onNext(t: T) {
                postValue(t)
            }

            override fun onError(t: Throwable) {
                // Errors should be handled upstream, so propagate as a crash.
                throw RuntimeException(t)
            }

            override fun onComplete() {
                subRef = null
            }
        })

    }

    override fun onInactive() {
        super.onInactive()
        subRef?.get()?.cancel()
        subRef = null
    }
}
