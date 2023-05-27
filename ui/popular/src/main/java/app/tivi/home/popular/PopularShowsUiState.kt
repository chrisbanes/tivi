/*
 * Copyright 2023 Google LLC
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

package app.tivi.home.popular

import androidx.compose.runtime.Stable
import androidx.paging.compose.LazyPagingItems
import app.tivi.data.compoundmodels.PopularEntryWithShow
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Stable // 'only' stable due to LazyPagingItems
data class PopularShowsUiState(
    val items: LazyPagingItems<PopularEntryWithShow>,
    val eventSink: (PopularShowsUiEvent) -> Unit,
) : CircuitUiState

sealed interface PopularShowsUiEvent : CircuitUiEvent {
    data class OpenShowDetails(val showId: Long) : PopularShowsUiEvent
    object NavigateUp : PopularShowsUiEvent
}
