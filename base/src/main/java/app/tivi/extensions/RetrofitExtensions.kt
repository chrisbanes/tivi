/*
 * Copyright 2017 Google LLC
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

@file:Suppress("NOTHING_TO_INLINE")

package app.tivi.extensions

import app.tivi.data.entities.ErrorResult
import app.tivi.data.entities.Result
import app.tivi.data.entities.Success
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.IOException

inline fun <T> Response<T>.bodyOrThrow(): T {
    if (!isSuccessful) throw HttpException(this)
    return body()!!
}

suspend fun <T> withRetry(
    defaultDelay: Long = 100,
    maxAttempts: Int = 3,
    shouldRetry: (Throwable) -> Boolean = ::defaultShouldRetry,
    block: suspend () -> Result<T>
): Result<T> {
    repeat(maxAttempts) { attempt ->
        when (val response = block()) {
            is Success -> return response
            is ErrorResult -> {
                // The response failed, so lets see if we should retry again
                if (attempt == maxAttempts - 1 || !shouldRetry(response.throwable)) {
                    throw response.throwable
                }

                var nextDelay = attempt * attempt * defaultDelay

                if (response.throwable is HttpException) {
                    // If we have a HttpException, check whether we have a Retry-After
                    // header to decide how long to delay
                    response.throwable.retryAfter?.let {
                        nextDelay = it.coerceAtLeast(defaultDelay)
                    }
                }

                delay(nextDelay)
            }
        }
    }

    // We should never hit here
    throw IllegalStateException("Unknown exception from executeWithRetry")
}

private val HttpException.retryAfter: Long?
    get() {
        val retryAfterHeader = response()?.headers()?.get("Retry-After")
        if (retryAfterHeader != null && retryAfterHeader.isNotEmpty()) {
            // Got a Retry-After value, try and parse it to an long
            try {
                return retryAfterHeader.toLong() + 10
            } catch (nfe: NumberFormatException) {
                // Probably won't happen, ignore the value and use the generated default above
            }
        }
        return null
    }

private fun defaultShouldRetry(throwable: Throwable) = when (throwable) {
    is HttpException -> throwable.code() == 429
    is IOException -> true
    else -> false
}

private val Response<*>.isFromNetwork: Boolean
    get() = raw().cacheResponse == null

suspend fun <T> Call<T>.awaitUnit(): Result<Unit> = try {
    Success(data = Unit, responseModified = awaitResponse().isFromNetwork)
} catch (t: Throwable) {
    ErrorResult(t)
}

suspend fun <T, E> Call<T>.awaitResult(
    mapper: suspend (T) -> E,
): Result<E> = try {
    awaitResponse().let {
        if (it.isSuccessful) {
            Success(
                data = mapper(it.bodyOrThrow()),
                responseModified = it.isFromNetwork
            )
        } else {
            ErrorResult(HttpException(it))
        }
    }
} catch (t: Throwable) {
    ErrorResult(t)
}
