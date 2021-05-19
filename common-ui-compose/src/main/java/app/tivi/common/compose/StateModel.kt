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

package app.tivi.common.compose

import androidx.compose.runtime.RememberObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

/**
 * A ViewModel-like class which can be used from composables.
 *
 * It implements [RememberObserver] which allows it to hold a [CoroutineScope], and cancel the
 * scope's job once the model has been 'cleared'.
 */
abstract class StateModel : RememberObserver {
    /**
     * A [CoroutineScope] which can be used similar to `viewModelScope`
     */
    protected val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onRemembered() {
    }

    override fun onForgotten() {
        coroutineScope.cancel()
    }

    override fun onAbandoned() {
        coroutineScope.cancel()
    }
}
