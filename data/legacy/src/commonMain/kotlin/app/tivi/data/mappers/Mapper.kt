// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

fun interface Mapper<F, T> {
    fun map(from: F): T
}

fun interface IndexedMapper<F, T> {
    fun map(index: Int, from: F): T
}
