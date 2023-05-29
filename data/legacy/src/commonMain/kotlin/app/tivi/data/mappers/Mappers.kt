// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("NOTHING_TO_INLINE")

package app.tivi.data.mappers

inline fun <F, T> Mapper<F, T>.map(collection: Collection<F>) = collection.map { map(it) }

inline fun <F, T1, T2> pairMapperOf(
    firstMapper: Mapper<F, T1>,
    secondMapper: Mapper<F, T2>,
): (List<F>) -> List<Pair<T1, T2>> = { from ->
    from.map { value ->
        firstMapper.map(value) to secondMapper.map(value)
    }
}

inline fun <F, T1, T2> pairMapperOf(
    firstMapper: Mapper<F, T1>,
    secondMapper: IndexedMapper<F, T2>,
): (List<F>) -> List<Pair<T1, T2>> = { from ->
    from.mapIndexed { index, value ->
        firstMapper.map(value) to secondMapper.map(index, value)
    }
}
