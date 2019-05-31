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

package app.tivi.util

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.LiveData
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

import java.util.concurrent.atomic.AtomicReference

fun <T> Observable<T>.toLiveData(): LiveData<T> = ObservableLiveData(this)

private class ObservableLiveData<T>(val observable: Observable<T>) : LiveData<T>() {
    private val observer: AtomicReference<LiveObserver> = AtomicReference()

    override fun onActive() {
        super.onActive()
        LiveObserver().run {
            observer.set(this)
            observable.subscribe(this)
        }
    }

    override fun onInactive() {
        super.onInactive()

        val s = observer.getAndSet(null)
        s?.dispose()
    }

    private inner class LiveObserver : AtomicReference<Disposable>(), Observer<T> {
        override fun onSubscribe(d: Disposable) {
            if (!compareAndSet(null, d)) {
                d.dispose()
            }
        }

        override fun onNext(item: T) = postValue(item)

        override fun onError(ex: Throwable) {
            observer.compareAndSet(this, null)

            ArchTaskExecutor.getInstance().executeOnMainThread {
                // Errors should be handled upstream, so propagate as a crash.
                throw RuntimeException("LiveData does not handle errors. Errors from " +
                        "publishers should be handled upstream and propagated as " +
                        "state", ex)
            }
        }

        override fun onComplete() {
            observer.compareAndSet(this, null)
        }

        fun dispose() {
            get()?.dispose()
        }
    }
}
