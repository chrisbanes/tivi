// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.perf

interface Tracer {
    fun trace(
        name: String,
        block: () -> Unit,
    )
}
