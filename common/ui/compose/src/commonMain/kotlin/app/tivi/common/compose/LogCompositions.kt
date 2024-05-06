// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

const val ENABLE_LOG_COMPOSITIONS = false

data class LogCompositionsRef(var count: Int)

// Note the inline function below which ensures that this function is essentially
// copied at the call site to ensure that its logging only recompositions from the
// original call site.
@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun LogCompositions(tag: String, msg: String) {
  if (ENABLE_LOG_COMPOSITIONS) {
    val ref = remember { LogCompositionsRef(0) }
    SideEffect { ref.count++ }
    println("[$tag] Compositions $msg: ${ref.count}")
  }
}
