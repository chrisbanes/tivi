// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("UNCHECKED_CAST")

package app.tivi.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

fun <T1, T2, T3, T4, T5, T6, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  transform: suspend (T1, T2, T3, T4, T5, T6) -> R,
): Flow<R> = combine(flow, flow2, flow3, flow4, flow5, flow6) { args: Array<*> ->
  transform(
    args[0] as T1,
    args[1] as T2,
    args[2] as T3,
    args[3] as T4,
    args[4] as T5,
    args[5] as T6,
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R,
): Flow<R> = combine(flow, flow2, flow3, flow4, flow5, flow6, flow7) { args: Array<*> ->
  transform(
    args[0] as T1,
    args[1] as T2,
    args[2] as T3,
    args[3] as T4,
    args[4] as T5,
    args[5] as T6,
    args[6] as T7,
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  flow8: Flow<T8>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8) -> R,
): Flow<R> = combine(flow, flow2, flow3, flow4, flow5, flow6, flow7, flow8) { args: Array<*> ->
  transform(
    args[0] as T1,
    args[1] as T2,
    args[2] as T3,
    args[3] as T4,
    args[4] as T5,
    args[5] as T6,
    args[6] as T7,
    args[7] as T8,
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  flow8: Flow<T8>,
  flow9: Flow<T9>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R,
): Flow<R> = combine(flow, flow2, flow3, flow4, flow5, flow6, flow7, flow8, flow9) { args: Array<*> ->
  transform(
    args[0] as T1,
    args[1] as T2,
    args[2] as T3,
    args[3] as T4,
    args[4] as T5,
    args[5] as T6,
    args[6] as T7,
    args[7] as T8,
    args[8] as T9,
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> combine(
  flow: Flow<T1>,
  flow2: Flow<T2>,
  flow3: Flow<T3>,
  flow4: Flow<T4>,
  flow5: Flow<T5>,
  flow6: Flow<T6>,
  flow7: Flow<T7>,
  flow8: Flow<T8>,
  flow9: Flow<T9>,
  flow10: Flow<T10>,
  transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> R,
): Flow<R> = combine(flow, flow2, flow3, flow4, flow5, flow6, flow7, flow8, flow9, flow10) { args: Array<*> ->
  transform(
    args[0] as T1,
    args[1] as T2,
    args[2] as T3,
    args[3] as T4,
    args[4] as T5,
    args[5] as T6,
    args[6] as T7,
    args[7] as T8,
    args[8] as T9,
    args[9] as T10,
  )
}
