/*
 * Copyright (C) 2022 Adam Powell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.slack.circuit.backstack

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

private fun Context.findActivity(): Activity? {
  var context = this
  while (context is ContextWrapper) {
    if (context is Activity) return context
    context = context.baseContext
  }
  return null
}

/** A [BackStackRecordLocalProvider] that provides [LocalViewModelStoreOwner] */
internal object ViewModelBackStackRecordLocalProvider :
  BackStackRecordLocalProvider<BackStack.Record> {
  @Composable
  override fun providedValuesFor(record: BackStack.Record): ProvidedValues {
    // Implementation note: providedValuesFor stays in the composition as long as the
    // back stack entry is present, which makes it safe for us to use composition
    // forget/abandon to clear the associated ViewModelStore if the host activity
    // isn't in the process of changing configurations.
    val containerViewModel =
      viewModel<BackStackRecordLocalProviderViewModel>(
        factory = BackStackRecordLocalProviderViewModel.Factory
      )
    val viewModelStore = containerViewModel.viewModelStoreForKey(record.key)
    val activity = LocalContext.current.findActivity()
    remember(record, viewModelStore) {
      object : RememberObserver {
        override fun onAbandoned() {
          disposeIfNotChangingConfiguration()
        }

        override fun onForgotten() {
          disposeIfNotChangingConfiguration()
        }

        override fun onRemembered() {}

        fun disposeIfNotChangingConfiguration() {
          if (activity?.isChangingConfigurations != true) {
            containerViewModel.removeViewModelStoreOwnerForKey(record.key)?.clear()
          }
        }
      }
    }
    return remember(viewModelStore) {
      val list =
        listOf<ProvidedValue<*>>(
          LocalViewModelStoreOwner provides
            object : ViewModelStoreOwner {
              override val viewModelStore: ViewModelStore = viewModelStore
            }
        )
      @Suppress("ObjectLiteralToLambda")
      object : ProvidedValues {
        @Composable override fun provideValues() = list
      }
    }
  }
}

internal class BackStackRecordLocalProviderViewModel : ViewModel() {
  private val owners = mutableMapOf<String, ViewModelStore>()

  internal fun viewModelStoreForKey(key: String): ViewModelStore =
    owners.getOrPut(key) { ViewModelStore() }

  internal fun removeViewModelStoreOwnerForKey(key: String): ViewModelStore? = owners.remove(key)

  override fun onCleared() {
    owners.forEach { (_, store) -> store.clear() }
  }

  object Factory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      @Suppress("UNCHECKED_CAST") return BackStackRecordLocalProviderViewModel() as T
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
      return create(modelClass)
    }
  }
}
