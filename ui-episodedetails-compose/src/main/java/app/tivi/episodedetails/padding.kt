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

package app.tivi.episodedetails

import androidx.ui.unit.Dp
import androidx.ui.unit.dp

private val ZeroDp = 0.dp

fun LayoutPadding(
    horizontal: Dp = ZeroDp,
    vertical: Dp = ZeroDp,
    left: Dp = ZeroDp,
    top: Dp = ZeroDp,
    right: Dp = ZeroDp,
    bottom: Dp = ZeroDp
) = androidx.ui.layout.LayoutPadding(
    left = if (left > ZeroDp) left else horizontal,
    top = if (top > ZeroDp) top else vertical,
    right = if (right > ZeroDp) right else horizontal,
    bottom = if (bottom > ZeroDp) bottom else vertical
)
