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

@file:Suppress("NOTHING_TO_INLINE")

package app.tivi.data.mappers

import com.uwetrottmann.tmdb2.entities.BaseTvShow
import com.uwetrottmann.tmdb2.entities.TvShowResultsPage

internal inline fun <F, T> Mapper<F, T>.forLists(): suspend (List<F>) -> List<T> {
    return { list -> list.map { item -> map(item) } }
}

internal inline fun <F, T> IndexedMapper<F, T>.forLists(): suspend (List<F>) -> List<T> {
    return { list -> list.mapIndexed { index, item -> map(index, item) } }
}

internal inline fun <F, T1, T2> pairMapperOf(
    firstMapper: Mapper<F, T1>,
    secondMapper: Mapper<F, T2>
): suspend (List<F>) -> List<Pair<T1, T2>> = { from ->
    from.map { value ->
        firstMapper.map(value) to secondMapper.map(value)
    }
}

internal inline fun <F, T1, T2> pairMapperOf(
    firstMapper: Mapper<F, T1>,
    secondMapper: IndexedMapper<F, T2>
): suspend (List<F>) -> List<Pair<T1, T2>> = { from ->
    from.mapIndexed { index, value ->
        firstMapper.map(value) to secondMapper.map(index, value)
    }
}

internal inline fun <T> unwrapTmdbShowResults(
    crossinline f: suspend (List<BaseTvShow>) -> List<T>
): suspend (TvShowResultsPage) -> List<T> = {
    f(it.results ?: emptyList())
}
