// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.extensions

inline fun <T> T.fluentIf(condition: Boolean, block: T.() -> T): T {
    return if (condition) block() else this
}
