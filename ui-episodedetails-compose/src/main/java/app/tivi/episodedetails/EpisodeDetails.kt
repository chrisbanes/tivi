/*
 * Copyright 2019 Google LLC
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

package app.tivi.episodedetails

import android.view.ViewGroup
import androidx.compose.Composable
import androidx.compose.effectOf
import androidx.compose.memo
import androidx.compose.onCommit
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.ui.core.Text
import androidx.ui.core.setContent
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.Column
import androidx.ui.material.MaterialTheme
import androidx.ui.material.TopAppBar

/**
 * This is a bit of hack. I can't make `ui-episodedetails` depend on any of the compose libraries,
 * so I wrap `setContext` as my own function, which `ui-episodedetails` can use.
 */
fun composeEpisodeDetails(viewGroup: ViewGroup, state: LiveData<EpisodeDetailsViewState>) {
    viewGroup.setContent {
        EpisodeDetails(state)
    }
}

@Composable
private fun EpisodeDetails(vm: LiveData<EpisodeDetailsViewState>) {
    MaterialTheme(typography = themeTypography) {
        val viewState = +observe(vm)
        Column {
            TopAppBar(
                    title = { Text(text = viewState?.episode?.title ?: "No episode") }
            )
            VerticalScroller(modifier = Flexible(1f)) {
                Column {
                    Text("blah")
                }
            }
        }
    }
}

// general purpose observe effect. this will likely be provided by LiveData. effect API for
// compose will also simplify soon.
fun <T> observe(data: LiveData<T>) = effectOf<T?> {
    val result = +state { data.value }
    val observer = +memo { Observer<T> { result.value = it } }

    +onCommit(data) {
        data.observeForever(observer)
        onDispose { data.removeObserver(observer) }
    }

    result.value
}
