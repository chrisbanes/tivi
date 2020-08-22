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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Icon
import androidx.compose.foundation.contentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.DeferredResource
import androidx.compose.ui.res.loadVectorResource

@Composable
fun IconResource(
    @DrawableRes resourceId: Int,
    modifier: Modifier = Modifier,
    tint: Color = contentColor()
) {
    val deferredResource = loadVectorResource(resourceId)
    deferredResource.onLoadRun { asset ->
        Icon(asset = asset, modifier = modifier, tint = tint)
    }
}

inline fun <T> DeferredResource<T>.onLoadRun(block: (T) -> Unit) {
    val res = resource.resource
    if (res != null) {
        block(res)
    }
}
