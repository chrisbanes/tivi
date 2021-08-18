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

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
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
        navBackStackEntry: NavBackStackEntry,
        create: (scope: CoroutineScope) -> T,
    ): StateFlow<T> = viewModelFlow(
        key = key,
        cancellationSignal = {
            // With a NavBackStackEntry, we use it's lifecycle as the signal to
            // cancel the scope
            suspendCancellableCoroutine<Unit> { cont ->
                val observer = LifecycleEventObserver { _, event ->
                    // Once the lifecycle is destroyed, cancel the coroutine
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        cont.cancel()
                    }
                }
                navBackStackEntry.lifecycle.addObserver(observer)
                cont.invokeOnCancellation {
                    navBackStackEntry.lifecycle.removeObserver(observer)
                }
            }
        },
        create = create,
    )

    /**
     * Retrieve or create a ViewModel.
     */
    @SuppressLint("LogNotTimber")
    fun <T : Any> viewModelFlow(
        key: Any,
        cancellationSignal: (suspend () -> Unit)? = null,
        create: (scope: CoroutineScope) -> T,
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
            Log.d("ViewModelStore", "Creating CoroutineScope with key: $key")

            // This is a 'fake' flow which doesn't emit anything. The actual ViewModel is
            // created in the stateIn() below

            if (cancellationSignal != null) {
                // If we've been given a cancellation signal, invoke it
                cancellationSignal()
            } else {
                // Otherwise we this is a no-op suspend call, and we let the WhileSubscribed
                // behavior control the cancellation
                awaitCancellation()
            }
        }.onCompletion {
            // The flow has been cancelled, so we need to also cancel the
            // ViewModel's coroutine scope
            Log.d("ViewModelStore", "Cancelling CoroutineScope with key: $key")
            viewModelScope.cancel()
            // Remove the entry from the map
            mapLock.withLock { viewModelMap.remove(key) }
        }.stateIn(
            scope = coroutineScope,
            started = when {
                // If we've been given a cancellation signal, we rely solely on that to tell us
                // when to cancel, so use Eagerly
                cancellationSignal != null -> SharingStarted.Eagerly
                // Otherwise we use the implicit behavior of cancelling the scope with a
                // 5 seconds timeout
                else -> SharingStarted.WhileSubscribed(ViewModelCancelTimeout)
            },
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
): T = viewModelFlow(key, null, create).collectAsState().value

@Composable
internal inline fun <T : Any> ViewModelStore.viewModel(
    key: Any,
    navBackStackEntry: NavBackStackEntry,
    noinline create: (scope: CoroutineScope) -> T,
): T = viewModelFlow(key, navBackStackEntry, create).collectAsState().value

private data class ViewModelStoreEntry(
    val coroutineScope: CoroutineScope,
    val flow: StateFlow<Any>,
) {
    val isActive: Boolean get() = coroutineScope.isActive
}
