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

package app.tivi.extensions

import kotlinx.coroutines.experimental.delay
import retrofit2.Call
import retrofit2.HttpException
import java.io.IOException

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