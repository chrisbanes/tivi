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

package app.tivi.home.discover

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.setContent
import androidx.lifecycle.LiveData
import app.tivi.common.compose.LogCompositions
import app.tivi.common.compose.ProvideDisplayInsets
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.util.TiviDateFormatter
import com.google.android.material.composethemeadapter.MdcTheme

internal fun ViewGroup.composeDiscover(
    state: LiveData<DiscoverViewState>,
    actioner: (DiscoverAction) -> Unit,
    tiviDateFormatter: TiviDateFormatter,
    textCreator: DiscoverTextCreator
): Any = setContent(Recomposer.current()) {
    Providers(
        TiviDateFormatterAmbient provides tiviDateFormatter,
        DiscoverTextCreatorAmbient provides textCreator
    ) {
        MdcTheme {
            LogCompositions("MdcTheme")

            ProvideDisplayInsets {
                LogCompositions("ProvideInsets")
                val viewState by state.observeAsState()
                if (viewState != null) {
                    LogCompositions("ViewState observeAsState")
                    Discover(viewState!!, actioner)
                }
            }
        }
    }
}

@Composable
fun Discover(
    state: DiscoverViewState,
    actioner: (DiscoverAction) -> Unit
) {
}
