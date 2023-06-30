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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap

/** A [BackStackRecordLocalProvider] that provides a [SaveableStateRegistry] for each record. */
public object SaveableStateRegistryBackStackRecordLocalProvider :
  BackStackRecordLocalProvider<BackStack.Record> {
  @Composable
  override fun providedValuesFor(record: BackStack.Record): ProvidedValues {
    val childRegistry =
      rememberSaveable(
        record,
        saver = BackStackRecordLocalSaveableStateRegistry.Saver,
        key = record.key
      ) {
        BackStackRecordLocalSaveableStateRegistry(mutableStateMapOf())
      }
    // This write depends on childRegistry.parentRegistry being snapshot state backed
    childRegistry.parentRegistry = LocalSaveableStateRegistry.current
    return remember(childRegistry) {
      object : ProvidedValues {
        val list = listOf(LocalSaveableStateRegistry provides childRegistry)

        @Composable
        override fun provideValues(): List<ProvidedValue<*>> {
          remember {
            object : RememberObserver {
              override fun onForgotten() {
                childRegistry.saveForContentLeavingComposition()
              }

              override fun onRemembered() {}

              override fun onAbandoned() {}
            }
          }
          return list
        }
      }
    }
  }
}

private class BackStackRecordLocalSaveableStateRegistry(
  // Note: restored is snapshot-backed because consumeRestored runs in composition
  // and must be rolled back if composition does not commit
  private val restored: SnapshotStateMap<String, List<Any?>>
) : SaveableStateRegistry {
  var parentRegistry: SaveableStateRegistry? by mutableStateOf(null)

  private val valueProviders = mutableMapOf<String, MutableList<() -> Any?>>()

  override fun canBeSaved(value: Any): Boolean = parentRegistry?.canBeSaved(value) != false

  override fun consumeRestored(key: String): Any? =
    restored.remove(key)?.let { list ->
      list.first().also {
        if (list.size > 1) {
          restored[key] = list.drop(1)
        }
      }
    }

  override fun performSave(): Map<String, List<Any?>> {
    val map = restored.toMutableMap()
    saveInto(map)
    return map
  }

  override fun registerProvider(
    key: String,
    valueProvider: () -> Any?
  ): SaveableStateRegistry.Entry {
    require(key.isNotBlank()) { "Registered key is empty or blank" }
    synchronized(valueProviders) {
      valueProviders.getOrPut(key) { mutableListOf() }.add(valueProvider)
    }
    return object : SaveableStateRegistry.Entry {
      override fun unregister() {
        synchronized(valueProviders) {
          val list = valueProviders.remove(key)
          list?.remove(valueProvider)
          if (list != null && list.isNotEmpty()) {
            // if there are other providers for this key return list
            // back to the map
            valueProviders[key] = list
          }
        }
      }
    }
  }

  fun saveForContentLeavingComposition() {
    saveInto(restored)
  }

  private fun saveInto(map: MutableMap<String, List<Any?>>) {
    synchronized(valueProviders) {
      valueProviders.forEach { (key, list) ->
        if (list.size == 1) {
          val value = list[0].invoke()
          if (value != null) {
            map[key] = arrayListOf<Any?>(value)
          }
        } else {
          // nulls hold empty spaces
          map[key] = list.map { it() }
        }
      }
    }
  }

  companion object {
    val Saver =
      Saver<BackStackRecordLocalSaveableStateRegistry, Map<String, List<Any?>>>(
        save = { value -> value.performSave() },
        restore = { value ->
          BackStackRecordLocalSaveableStateRegistry(
            mutableStateMapOf<String, List<Any?>>().apply { putAll(value) }
          )
        }
      )
  }
}
