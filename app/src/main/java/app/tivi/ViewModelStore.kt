/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("NOTHING_TO_INLINE")

package app.tivi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * The amount of time before cancelling a ViewModel, after all UI has stop using a ViewModel
 */
private const val ViewModelCancelTimeout: Long = 5_000 // 5 seconds

/**
 * TODO
 */
@Stable
internal class ViewModelStore(
    coroutineScope: CoroutineScope,
) {
    private val viewModelMap = HashMap<Any, ViewModelStoreEntry>()
    private val mapLock = ReentrantLock()

    // Create a copy of the provided CoroutineScope, but replacing the Job with
    // a child SupervisorJob. This allows a ViewModel to be cancelled without affecting any
    // other ViewModels.
    private val coroutineScope = CoroutineScope(
        coroutineScope.coroutineContext +
            SupervisorJob(coroutineScope.coroutineContext[Job])
    )

    /**
     * Retrieve or create a ViewModel.
     */
    fun <T : Any> viewModelFlow(
        key: Any,
        create: (scope: CoroutineScope) -> T
    ): StateFlow<T> = mapLock.withLock {
        // First check if we have a 'cached' ViewModel which is still active
        val cached = viewModelMap[key]?.takeIf { it.isActive }
        if (cached != null) {
            @Suppress("UNCHECKED_CAST")
            return cached.flow as StateFlow<T>
        }

        // If not we'll create a new ViewModel
        val viewModelScope = createChildCoroutineScope()

        val flow = flow<T> {
            // This is a 'fake' flow which doesn't emit anything. The actual ViewModel is
            // created in the stateIn() below. We suspend the flow until its cancelled, which
            // happens when either the stateIn `WhileSubscribed`, or the parent scope is cancelled
            suspendCancellableCoroutine<Unit> { cont ->
                cont.invokeOnCancellation {
                    // The flow has been cancelled, so we need to also cancel the
                    // ViewModel's coroutine scope
                    viewModelScope.cancel()
                    // Remove the entry from the map
                    mapLock.withLock { viewModelMap.remove(key) }
                }
            }
        }.stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(ViewModelCancelTimeout),
            initialValue = create(viewModelScope),
        )

        // Store the child scope & flow
        viewModelMap[key] = ViewModelStoreEntry(
            coroutineScope = viewModelScope,
            flow = flow,
        )

        return flow
    }

    /**
     * Create a [CoroutineScope] for each ViewModel. This is modelled after `viewModelScope`
     */
    private fun createChildCoroutineScope(): CoroutineScope = CoroutineScope(
        Dispatchers.Main.immediate +
            SupervisorJob(coroutineScope.coroutineContext[Job])
    )
}

@Composable
internal inline fun <T : Any> ViewModelStore.viewModel(
    key: Any,
    noinline create: (scope: CoroutineScope) -> T,
): T = viewModelFlow(key, create).collectAsState().value

private data class ViewModelStoreEntry(
    val coroutineScope: CoroutineScope,
    val flow: StateFlow<Any>,
) {
    val isActive: Boolean get() = coroutineScope.isActive
}
