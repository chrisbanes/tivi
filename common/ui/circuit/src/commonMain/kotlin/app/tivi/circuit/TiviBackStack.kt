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

package app.tivi.circuit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.benasher44.uuid.uuid4
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.isEmpty
import com.slack.circuit.runtime.Screen

@Composable
fun rememberTiviBackStack(init: TiviBackStack.() -> Unit): TiviBackStack =
    rememberSaveable(saver = TiviBackStack.Saver) {
        TiviBackStack().apply(init).also {
            check(!it.isEmpty) { "Backstack must be non-empty after init." }
        }
    }

fun TiviBackStack.push(route: String, args: Map<String, Any?> = emptyMap()) {
    push(createRecord(route = route, args = args))
}

private fun TiviBackStack.createRecord(
    route: String,
    args: Map<String, Any?> = emptyMap(),
    key: String = uuid4().toString(),
): TiviBackStack.Record {
    val recordInstanceKeeper = instanceKeeper.child(key = key)

    return TiviBackStack.Record(
        route = route,
        args = args,
        key = key,
        instanceKeeper = recordInstanceKeeper,
    ).apply {
        // Destroy the record's InstanceKeeper once the record has been destroyed
        lifecycleRegistry.doOnDestroy {
            recordInstanceKeeper.destroy()
        }
    }
}

inline fun TiviBackStack.popUntil(predicate: (TiviBackStack.Record) -> Boolean) {
    while (topRecord?.let(predicate) == false) pop()
}

/**
 * A [BackStack] that supports saving its state via [rememberSaveable]. See
 * [rememberTiviBackStack].
 */
class TiviBackStack : BackStack<TiviBackStack.Record>, InstanceKeeperOwner {

    private val instanceKeeperDispatcher = InstanceKeeperDispatcher()
    override val instanceKeeper: InstanceKeeper get() = instanceKeeperDispatcher

    private val entryList = mutableStateListOf<Record>()

    override val size: Int
        get() = entryList.size

    override fun iterator(): Iterator<Record> = entryList.iterator()

    val topRecord: Record?
        get() = entryList.firstOrNull()

    fun push(record: Record) {
        record.lifecycleRegistry.create()
        entryList.add(0, record)
    }

    override fun pop(): Record? {
        return entryList.removeFirstOrNull()?.also { record ->
            record.lifecycleRegistry.destroy()
        }
    }

    data class Record(
        override val route: String,
        override val key: String,
        val args: Map<String, Any?> = emptyMap(),
        override val instanceKeeper: InstanceKeeper,
    ) : BackStack.Record, LifecycleOwner, InstanceKeeperOwner {

        internal val lifecycleRegistry = LifecycleRegistry()
        override val lifecycle: Lifecycle get() = lifecycleRegistry
    }

    internal companion object {
        val Saver = Saver<TiviBackStack, List<Any>>(
            save = { value ->
                value.entryList.map { record ->
                    buildList {
                        add(record.route)
                        add(record.args)
                        add(record.key)
                    }
                }
            },
            restore = { list ->
                @Suppress("UNCHECKED_CAST")
                TiviBackStack().also { backStack ->
                    list.mapTo(backStack.entryList) {
                        val saved = it as List<Any>
                        backStack.createRecord(
                            route = saved[0] as String,
                            args = saved[1] as Map<String, Any?>,
                            key = saved[2] as String,
                        )
                    }
                }
            },
        )
    }
}

fun TiviBackStack.push(screen: Screen) {
    push(
        createRecord(
            route = checkNotNull(screen::class.simpleName),
            args = mapOf("screen" to screen),
            key = screen.hashCode().toString()
        )
    )
}

val TiviBackStack.Record.screen: Screen
    get() = args.getValue("screen") as Screen
