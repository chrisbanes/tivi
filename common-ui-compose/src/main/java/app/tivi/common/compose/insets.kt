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
import androidx.compose.Ambient
import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.ambient
import androidx.compose.onCommit
import androidx.compose.remember
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.ui.unit.IntPx
import androidx.ui.unit.ipx

@Model
data class InsetsHolder(
    var left: IntPx = IntPx.Zero,
    var top: IntPx = IntPx.Zero,
    var right: IntPx = IntPx.Zero,
    var bottom: IntPx = IntPx.Zero
) {
    val horizontal get() = right + left
    val vertical get() = top + bottom
}

fun InsetsHolder.setFrom(insets: WindowInsetsCompat) {
    val newLeft = insets.systemWindowInsetLeft.ipx
    if (left != newLeft) {
        left = newLeft
    }
    val newTop = insets.systemWindowInsetTop.ipx
    if (top != newTop) {
        top = newTop
    }
    val newRight = insets.systemWindowInsetRight.ipx
    if (right != newRight) {
        right = newRight
    }
    val newBottom = insets.systemWindowInsetBottom.ipx
    if (bottom != newBottom) {
        bottom = newBottom
    }
}

val InsetsAmbient = Ambient.of { InsetsHolder() }

@Composable
fun observeInsets(liveData: LiveData<WindowInsetsCompat>) {
    val insetsHolder = ambient(InsetsAmbient)
    val observer = remember {
        Observer<WindowInsetsCompat> { insets -> insets?.let(insetsHolder::setFrom) }
    }
    onCommit(liveData) {
        liveData.observeForever(observer)
        onDispose { liveData.removeObserver(observer) }
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
