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

package app.tivi.common.compose

import android.view.View
import androidx.compose.Composable
import androidx.compose.Providers
import androidx.compose.ambientOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.ui.livedata.observeAsState
import androidx.ui.unit.IntPx
import androidx.ui.unit.ipx

data class InsetsHolder(
    val left: IntPx = IntPx.Zero,
    val top: IntPx = IntPx.Zero,
    val right: IntPx = IntPx.Zero,
    val bottom: IntPx = IntPx.Zero
) {
    constructor(insets: WindowInsetsCompat) : this(
        insets.systemWindowInsetLeft.ipx,
        insets.systemWindowInsetTop.ipx,
        insets.systemWindowInsetRight.ipx,
        insets.systemWindowInsetBottom.ipx
    )

    val horizontal get() = right + left
    val vertical get() = top + bottom
}

val InsetsAmbient = ambientOf { InsetsHolder() }

@Composable
fun ProvideInsets(
    liveData: LiveData<WindowInsetsCompat>,
    children: @Composable () -> Unit
) {
    val currentInsets = Transformations.map(liveData) {
        it?.let(::InsetsHolder) ?: InsetsHolder()
    }.observeAsState(InsetsHolder())

    Providers(InsetsAmbient provides currentInsets.value) {
        children()
    }
}

fun View.observeWindowInsets(): LiveData<WindowInsetsCompat> {
    val data = MutableLiveData<WindowInsetsCompat>()
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        data.value = insets
        insets
    }
    doOnAttach(View::requestApplyInsets)
    doOnDetach { data.value = null }
    return data
}
