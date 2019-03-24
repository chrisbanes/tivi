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

package app.tivi.data.mappers

private class PairMapper<F, T1, T2>(
    private val firstMapper: Mapper<F, T1>,
    private val secondMapper: Mapper<F, T2>
) : Mapper<List<F>, List<Pair<T1, T2>>> {
    override suspend fun map(from: List<F>): List<Pair<T1, T2>> = from.map {
        firstMapper.map(it) to secondMapper.map(it)
    }
}

private class PairMapper2<F, T1, T2>(
    private val firstMapper: Mapper<F, T1>,
    private val secondMapper: IndexedMapper<F, T2>
) : Mapper<List<F>, List<Pair<T1, T2>>> {
    override suspend fun map(from: List<F>): List<Pair<T1, T2>> = from.mapIndexed { index, value ->
        firstMapper.map(value) to secondMapper.map(index, value)
    }
}

fun <F, T1, T2> pairMapperOf(
    firstMapper: Mapper<F, T1>,
    secondMapper: Mapper<F, T2>
): Mapper<List<F>, List<Pair<T1, T2>>> {
    return PairMapper(firstMapper, secondMapper)
}

fun <F, T1, T2> pairMapperOf(
    firstMapper: Mapper<F, T1>,
    secondMapper: IndexedMapper<F, T2>
): Mapper<List<F>, List<Pair<T1, T2>>> {
    return PairMapper2(firstMapper, secondMapper)
}