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
import java.io.IOException

inline fun <T> Response<T>.bodyOrThrow(): T {
    if (!isSuccessful) throw HttpException(this)
    return body()!!
}

inline fun <T> Response<T>.toException() = HttpException(this)

suspend inline fun <T> Call<T>.executeWithRetry(
    defaultDelay: Long = 100,
    maxAttempts: Int = 3,
    shouldRetry: (Exception) -> Boolean = ::defaultShouldRetry
): Response<T> {
    repeat(maxAttempts) { attempt ->
        var nextDelay = attempt * attempt * defaultDelay

        try {
            // Clone a new ready call if needed
            val call = if (isExecuted) clone() else this
            return call.execute()
        } catch (e: Exception) {
            // The response failed, so lets see if we should retry again
            if (attempt == (maxAttempts - 1) || !shouldRetry(e)) {
                throw e
            }

            if (e is HttpException) {
                // If we have a HttpException, check whether we have a Retry-After
                // header to decide how long to delay
                val retryAfterHeader = e.response()?.headers()?.get("Retry-After")
                if (retryAfterHeader != null && retryAfterHeader.isNotEmpty()) {
                    // Got a Retry-After value, try and parse it to an long
                    try {
                        nextDelay = (retryAfterHeader.toLong() + 10).coerceAtLeast(defaultDelay)
                    } catch (nfe: NumberFormatException) {
                        // Probably won't happen, ignore the value and use the generated default above
                    }
                }
            }
        }

        delay(nextDelay)
    }

    // We should never hit here
    throw IllegalStateException("Unknown exception from executeWithRetry")
}

suspend inline fun <T> Call<T>.fetchBodyWithRetry(
    firstDelay: Long = 100,
    maxAttempts: Int = 3,
    shouldRetry: (Exception) -> Boolean = ::defaultShouldRetry
) = executeWithRetry(firstDelay, maxAttempts, shouldRetry).bodyOrThrow()

inline fun defaultShouldRetry(exception: Exception) = when (exception) {
    is HttpException -> exception.code() == 429
    is IOException -> true
    else -> false
}

inline fun <T> Response<T>.isFromNetwork(): Boolean {
    return raw().cacheResponse == null
}

inline fun <T> Response<T>.isFromCache(): Boolean {
    return raw().cacheResponse != null
}

inline fun <T> Response<T>.toResultUnit(): Result<Unit> = try {
    if (isSuccessful) {
        Success(data = Unit, responseModified = isFromNetwork())
    } else {
        ErrorResult(toException())
    }
} catch (e: Exception) {
    ErrorResult(e)
}

inline fun <T> Response<T>.toResult(): Result<T> = try {
    if (isSuccessful) {
        Success(data = bodyOrThrow(), responseModified = isFromNetwork())
    } else {
        ErrorResult(toException())
    }
} catch (e: Exception) {
    ErrorResult(e)
}

@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
suspend fun <T, E> Response<T>.toResult(mapper: suspend (T) -> E): Result<E> = try {
    if (isSuccessful) {
        Success(data = mapper(bodyOrThrow()), responseModified = isFromNetwork())
    } else {
        ErrorResult(toException())
    }
} catch (e: Exception) {
    ErrorResult(e)
}
