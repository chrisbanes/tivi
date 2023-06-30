package com.slack.circuit.backstack

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

internal actual val LocalBackStackRecordLocalProviders:
  ProvidableCompositionLocal<List<BackStackRecordLocalProvider<BackStack.Record>>>
  get() = compositionLocalOf { emptyList() }
