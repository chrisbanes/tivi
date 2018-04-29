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

package me.banes.chris.tivi.extensions

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.experimental.delay
import retrofit2.Call
import retrofit2.HttpException
import java.io.IOException

fun <T> Call<T>.toRxObservable(): Observable<T> = me.banes.chris.tivi.extensions.BodyObservable(me.banes.chris.tivi.extensions.RetrofitCallObservable(this))
fun <T> Call<T>.toRxSingle(): Single<T> = toRxObservable().singleOrError()

fun <T> Call<T>.fetchBody(): T {
    return execute().let {
        if (!it.isSuccessful) throw HttpException(it)
        it.body()!!
    }
}

suspend inline fun <T> Call<T>.fetchBodyWithRetry(
    firstDelay: Int = 100,
    maxAttempts: Int = 3,
    shouldRetry: (Exception) -> Boolean = ::defaultShouldRetry
): T {
    var nextDelay = firstDelay
    repeat(maxAttempts - 1) { attempt ->
        // Clone a new ready call if needed
        val call = if (!isExecuted) {
            this
        } else {
            clone()
        }

        // Execute the call
        try {
            return call.fetchBody()
        } catch (e: Exception) {
            // The response failed, so lets see if we should retry again
            if (attempt == (maxAttempts - 1) || !shouldRetry(e)) {
                throw e
            }
        }
        // Delay to implement exp. backoff
        delay(nextDelay)
        // Increase the next delay
        nextDelay *= 2
    }

    // We should never hit here
    throw IllegalStateException("Unknown exception from fetchBodyWithRetry")
}

fun defaultShouldRetry(exception: Exception) = when (exception) {
    is HttpException -> exception.code() == 429
    is IOException -> true
    else -> false
}

private class RetrofitCallObservable<T>(private val originalCall: Call<T>) : Observable<retrofit2.Response<T>>() {

    override fun subscribeActual(observer: Observer<in retrofit2.Response<T>>) {
        // Since Call is a one-shot type, clone it for each new observer.
        val call = originalCall.clone()
        observer.onSubscribe(CallDisposable(call))

        var terminated = false
        try {
            val response = call.execute()
            if (!call.isCanceled) {
                observer.onNext(response)
            }
            if (!call.isCanceled) {
                terminated = true
                observer.onComplete()
            }
        } catch (t: Throwable) {
            Exceptions.throwIfFatal(t)
            if (terminated) {
                RxJavaPlugins.onError(t)
            } else if (!call.isCanceled) {
                try {
                    observer.onError(t)
                } catch (inner: Throwable) {
                    Exceptions.throwIfFatal(inner)
                    RxJavaPlugins.onError(CompositeException(t, inner))
                }
            }
        }
    }
}

private class CallDisposable(private val call: Call<*>) : Disposable {
    override fun dispose() {
        call.cancel()
    }

    override fun isDisposed(): Boolean {
        return call.isCanceled
    }
}

private class BodyObservable<T>(private val upstream: Observable<retrofit2.Response<T>>) : Observable<T>() {
    override fun subscribeActual(observer: Observer<in T>) {
        upstream.subscribe(BodyObserver(observer))
    }
}

private class BodyObserver<R>(private val observer: Observer<in R>) : Observer<retrofit2.Response<R>> {
    private var terminated: Boolean = false

    override fun onSubscribe(disposable: Disposable) {
        observer.onSubscribe(disposable)
    }

    override fun onNext(response: retrofit2.Response<R>) {
        if (response.isSuccessful) {
            observer.onNext(response.body()!!)
        } else {
            terminated = true
            val t = retrofit2.HttpException(response)
            try {
                observer.onError(t)
            } catch (inner: Throwable) {
                Exceptions.throwIfFatal(inner)
                RxJavaPlugins.onError(CompositeException(t, inner))
            }
        }
    }

    override fun onComplete() {
        if (!terminated) {
            observer.onComplete()
        }
    }

    override fun onError(throwable: Throwable) {
        if (!terminated) {
            observer.onError(throwable)
        } else {
            // This should never happen! onNext handles and forwards errors automatically.
            val broken = AssertionError(
                    "This should never happen! Report as a bug with the full stacktrace.")

            broken.initCause(throwable)
            RxJavaPlugins.onError(broken)
        }
    }
}