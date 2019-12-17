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

package app.tivi.common.epoxy

import com.airbnb.epoxy.EpoxyModel

object TotalSpanOverride : EpoxyModel.SpanSizeOverrideCallback {
    override fun getSpanSize(totalSpanCount: Int, position: Int, itemCount: Int) = totalSpanCount
}

object HalfSpanOverride : EpoxyModel.SpanSizeOverrideCallback {
    override fun getSpanSize(
        totalSpanCount: Int,
        position: Int,
        itemCount: Int
    ) = (totalSpanCount / 2).coerceAtLeast(1)
}
