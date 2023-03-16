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

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import app.tivi.api.UiMessage

fun CombinedLoadStates.appendErrorOrNull(): UiMessage? {
    return (append.takeIf { it is LoadState.Error } as? LoadState.Error)
        ?.let { UiMessage(it.error) }
}

fun CombinedLoadStates.prependErrorOrNull(): UiMessage? {
    return (prepend.takeIf { it is LoadState.Error } as? LoadState.Error)
        ?.let { UiMessage(it.error) }
}

fun CombinedLoadStates.refreshErrorOrNull(): UiMessage? {
    return (refresh.takeIf { it is LoadState.Error } as? LoadState.Error)
        ?.let { UiMessage(it.error) }
}

/*
 * The following are workarounds for https://issuetracker.google.com/issues/177245496.
 *
 * Due to the way which paging loads data, it will always result in an initial empty state,
 * and then the loaded items. That conflicts with the way which rememberLazyListState() and
 * friends store their state, as they rely on the items being present at the time of
 * (Android) state restoration. To workaround this, we use a different LazyListState while the
 * lazy paging items are empty, and then switcheroo to the standard state once we know we have the
 * items available.
 */

@Composable
fun rememberLazyListState(empty: Boolean = false): LazyListState {
    return if (empty) {
        // Return a different state instance
        remember { LazyListState(0, 0) }
    } else {
        // Return restored state
        androidx.compose.foundation.lazy.rememberLazyListState()
    }
}

@Composable
fun rememberLazyGridState(empty: Boolean = false): LazyGridState {
    return if (empty) {
        // Return a different state instance
        remember { LazyGridState(0, 0) }
    } else {
        // Return restored state
        androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    }
}
