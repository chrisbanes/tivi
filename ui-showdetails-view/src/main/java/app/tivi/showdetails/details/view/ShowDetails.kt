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

package app.tivi.showdetails.details.view

import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import app.tivi.common.compose.InsetsHolder
import app.tivi.common.compose.MaterialThemeFromAndroidTheme
import app.tivi.common.compose.WrapWithAmbients
import app.tivi.common.compose.observe
import app.tivi.common.compose.observeInsets
import app.tivi.common.compose.setContentWithLifecycle
import app.tivi.showdetails.details.ShowDetailsAction
import app.tivi.showdetails.details.ShowDetailsViewState
import app.tivi.util.TiviDateFormatter

fun ViewGroup.composeShowDetails(
    lifecycleOwner: LifecycleOwner,
    state: LiveData<ShowDetailsViewState>,
    insets: LiveData<WindowInsetsCompat>,
    actioner: (ShowDetailsAction) -> Unit,
    tiviDateFormatter: TiviDateFormatter
): Any = setContentWithLifecycle(lifecycleOwner) {
    WrapWithAmbients(tiviDateFormatter, InsetsHolder()) {
        observeInsets(insets)

        val viewState = observe(state)
        if (viewState != null) {
            MaterialThemeFromAndroidTheme(context) {
                // TODO
            }
        }
    }
}