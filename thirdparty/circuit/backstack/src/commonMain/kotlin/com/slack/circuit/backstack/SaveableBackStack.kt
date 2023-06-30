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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.benasher44.uuid.uuid4

@Composable
public fun rememberSaveableBackStack(init: SaveableBackStack.() -> Unit): SaveableBackStack =
  rememberSaveable(saver = SaveableBackStack.Saver) {
    SaveableBackStack().apply(init).also {
      check(!it.isEmpty) { "Backstack must be non-empty after init." }
    }
  }

public fun SaveableBackStack.push(route: String, args: Map<String, Any?> = emptyMap()) {
  push(SaveableBackStack.Record(route, args))
}

public inline fun SaveableBackStack.popUntil(predicate: (SaveableBackStack.Record) -> Boolean) {
  while (topRecord?.let(predicate) == false) pop()
}

/**
 * A [BackStack] that supports saving its state via [rememberSaveable]. See
 * [rememberSaveableBackStack].
 */
public class SaveableBackStack : BackStack<SaveableBackStack.Record> {

  private val entryList = mutableStateListOf<Record>()

  override val size: Int
    get() = entryList.size

  override fun iterator(): Iterator<Record> = entryList.iterator()

  public val topRecord: Record?
    get() = entryList.firstOrNull()

  public fun push(record: Record) {
    entryList.add(0, record)
  }

  override fun pop(): Record? = entryList.removeFirstOrNull()

  public data class Record(
    override val route: String,
    val args: Map<String, Any?> = emptyMap(),
    override val key: String = uuid4().toString(),
  ) : BackStack.Record {
    internal companion object {
      val Saver: Saver<Record, List<Any>> =
        Saver(
          save = { value ->
            buildList {
              add(value.route)
              add(value.args)
              add(value.key)
            }
          },
          restore = { list ->
            @Suppress("UNCHECKED_CAST")
            Record(
              route = list[0] as String,
              args = list[1] as Map<String, Any?>,
              key = list[2] as String
            )
          }
        )
    }
  }

  internal companion object {
    val Saver =
      Saver<SaveableBackStack, List<Any>>(
        save = { value -> value.entryList.map { with(Record.Saver) { save(it)!! } } },
        restore = { list ->
          SaveableBackStack().also { backStack ->
            list.mapTo(backStack.entryList) {
              @Suppress("UNCHECKED_CAST") Record.Saver.restore(it as List<Any>)!!
            }
          }
        }
      )
  }
}
