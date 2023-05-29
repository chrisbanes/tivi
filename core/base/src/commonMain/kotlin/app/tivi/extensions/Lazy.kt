// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.extensions

@Suppress("NOTHING_TO_INLINE")
inline fun <T> unsafeLazy(noinline initializer: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE, initializer)
