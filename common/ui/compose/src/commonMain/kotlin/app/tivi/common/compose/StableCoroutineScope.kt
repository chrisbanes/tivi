// Copyright (C) 2023 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.slack.circuit.retained.rememberRetained
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

/**
 * Returns a [StableCoroutineScope] around a [androidx.compose.runtime.rememberCoroutineScope].
 * This is useful for event callback lambdas that capture a local scope variable to launch new
 * coroutines, as it allows them to be stable.
 */
@Composable
fun rememberCoroutineScope(): StableCoroutineScope {
  val scope = androidx.compose.runtime.rememberCoroutineScope()
  return remember { StableCoroutineScope(scope) }
}

/** @see rememberCoroutineScope */
@Stable
class StableCoroutineScope(scope: CoroutineScope) : CoroutineScope by scope

@Composable
fun rememberRetainedCoroutineScope(): StableCoroutineScope {
  return rememberRetained("coroutine_scope") {
    object : RememberObserver {
      val scope = StableCoroutineScope(CoroutineScope(Dispatchers.Main + Job()))

      override fun onAbandoned() = onForgotten()

      override fun onForgotten() {
        scope.cancel()
      }

      override fun onRemembered() = Unit
    }
  }.scope
}
