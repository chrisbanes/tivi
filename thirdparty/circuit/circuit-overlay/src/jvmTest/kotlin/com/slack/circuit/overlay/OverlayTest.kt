// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
package com.slack.circuit.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class OverlayTest {
  @Test
  fun noOverlayMeansNullData() = runTest {
    moleculeFlow(RecompositionClock.Immediate) {
        val overlayHost = rememberOverlayHost()
        overlayHost.currentOverlayData
      }
      .distinctUntilChanged()
      .test { assertNull(awaitItem()) }
  }

  @Test
  fun overlayHasData() = runTest {
    moleculeFlow(RecompositionClock.Immediate) {
        val overlayHost = rememberOverlayHost()
        LaunchedEffect(overlayHost) {
          overlayHost.show(
            object : Overlay<String> {
              @Composable override fun Content(navigator: OverlayNavigator<String>) {}
            }
          )
        }
        overlayHost.currentOverlayData
      }
      .distinctUntilChanged()
      .test {
        assertNull(awaitItem())
        assertNotNull(awaitItem())
      }
  }

  @Test
  fun overlayFinishedHasNullDataAgain() = runTest {
    val resultState = mutableStateOf<String?>(null)
    moleculeFlow(RecompositionClock.Immediate) {
        val overlayHost = rememberOverlayHost()
        val overlayHostData by rememberUpdatedState(overlayHost.currentOverlayData)
        key(overlayHostData) { overlayHostData?.let { data -> data.overlay.Content(data::finish) } }
        LaunchedEffect(overlayHost) {
          overlayHost.show(
            object : Overlay<String> {
              @Composable
              override fun Content(navigator: OverlayNavigator<String>) {
                val resultStateValue = resultState.value
                if (resultStateValue != null) {
                  navigator.finish(resultStateValue)
                }
              }
            }
          )
        }
        overlayHostData
      }
      .distinctUntilChanged()
      .test {
        assertNull(awaitItem())
        assertNotNull(awaitItem())
        resultState.value = "Done!"
        assertNull(awaitItem())
      }
  }
}
