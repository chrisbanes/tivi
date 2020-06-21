/*
 * Copyright 2020 Google LLC
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

package app.tivi.ui

import app.tivi.api.UiError
import app.tivi.extensions.delayFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.threeten.bp.Duration
import javax.inject.Inject

class SnackbarManager @Inject constructor() {
    // We want a maximum of 3 errors queued
    private val pendingErrors = Channel<UiError>(3)
    private val removeErrorSignal = Channel<Unit>(1)

    fun launchInScope(
        scope: CoroutineScope,
        onErrorVisibilityChanged: (UiError, Boolean) -> Unit
    ) {
        scope.launch {
            pendingErrors.consumeAsFlow().collect { error ->
                // Set the error
                onErrorVisibilityChanged(error, true)

                merge(
                    delayFlow(Duration.ofSeconds(6).toMillis(), Unit),
                    removeErrorSignal.receiveAsFlow()
                ).firstOrNull()

                // Now remove the error
                onErrorVisibilityChanged(error, false)
                // Delay to allow the current error to disappear
                delay(200)
            }
        }
    }

    fun sendError(error: UiError) {
        if (!pendingErrors.isClosedForSend) {
            pendingErrors.offer(error)
        }
    }

    fun removeCurrentError() {
        if (!removeErrorSignal.isClosedForSend) {
            removeErrorSignal.offer(Unit)
        }
    }
}
