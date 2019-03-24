/*
 * Copyright 2018 Google LLC
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

package app.tivi.data

import app.tivi.data.entities.ErrorResult
import app.tivi.data.entities.Result
import app.tivi.data.entities.Success
import app.tivi.data.mappers.Mapper
import app.tivi.extensions.bodyOrThrow
import app.tivi.extensions.toException
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitRunner @Inject constructor() {
    suspend fun <T, E> executeForResponse(mapper: Mapper<T, E>, request: suspend () -> Response<T>): Result<E> {
        return try {
            val response = request()
            if (response.isSuccessful) {
                val okHttpNetworkResponse = response.raw().networkResponse()
                val notModified = okHttpNetworkResponse == null || okHttpNetworkResponse.code() == 304
                Success(data = mapper.map(response.bodyOrThrow()), responseModified = !notModified)
            } else {
                ErrorResult(response.toException())
            }
        } catch (e: Exception) {
            ErrorResult(e)
        }
    }

    suspend fun <T> executeForResponse(request: suspend () -> Response<T>): Result<Unit> {
        val unitMapper = object : Mapper<T, Unit> {
            override suspend fun map(from: T) = Unit
        }
        return executeForResponse(unitMapper, request)
    }
}