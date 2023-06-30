package com.slack.circuit.backstack

import androidx.compose.runtime.compositionLocalOf

@Suppress("RemoveExplicitTypeArguments")
internal actual val LocalBackStackRecordLocalProviders =
  compositionLocalOf<List<BackStackRecordLocalProvider<BackStack.Record>>> {
    listOf(SaveableStateRegistryBackStackRecordLocalProvider, ViewModelBackStackRecordLocalProvider)
  }
