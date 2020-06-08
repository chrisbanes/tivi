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

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyControllerAdapter
import com.airbnb.epoxy.EpoxyModel

/** Add models to a CarouselModel_ by transforming a list of items into EpoxyModels.
 *
 * @param items The items to transform to models
 * @param modelBuilder A function that take an item and returns a new EpoxyModel for that item.
 */
inline fun <T> TiviCarouselModelBuilder.withModelsFrom(
    items: List<T>,
    modelBuilder: (T) -> EpoxyModel<*>
) {
    models(items.map { modelBuilder(it) })
}

fun RecyclerView.syncSpanSizes(controller: EpoxyController) {
    val layout = layoutManager
    if (layout is GridLayoutManager) {
        if (controller.spanCount != layout.spanCount ||
            layout.spanSizeLookup !== controller.spanSizeLookup
        ) {
            controller.spanCount = layout.spanCount
            layout.spanSizeLookup = controller.spanSizeLookup
        }
    }
}

fun EpoxyControllerAdapter.findPositionOfItemId(itemId: Long): Int {
    return (0 until itemCount).firstOrNull { getItemId(it) == itemId }
        ?: RecyclerView.NO_POSITION
}
