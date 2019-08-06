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

package app.tivi.util

import androidx.annotation.StringRes
import app.tivi.common.epoxy.EpoxyModelProperty
import app.tivi.common.epoxy.TotalSpanOverride
import app.tivi.common.layouts.HeaderBindingModel_
import app.tivi.common.layouts.PosterGridItemBindingModel_
import app.tivi.common.layouts.emptyState
import app.tivi.common.layouts.header
import app.tivi.common.layouts.infiniteLoading
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.tmdb.TmdbImageUrlProvider
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.paging.PagedListEpoxyController

abstract class EntryGridEpoxyController<LI : EntryWithShow<out Entry>>(
    @StringRes private val titleRes: Int
) : PagedListEpoxyController<LI>() {
    var isLoading by EpoxyModelProperty { false }
    var tmdbImageUrlProvider by EpoxyModelProperty<TmdbImageUrlProvider?> { null }

    @Suppress("UselessCallOnCollection")
    override fun addModels(models: List<EpoxyModel<*>>) {
        // Need to do this due to https://github.com/airbnb/epoxy/issues/567
        val modelsFiltered = models.filterNotNull()

        if (modelsFiltered.isNotEmpty()) {
            header {
                id("header")
                title(titleRes)
                spanSizeOverride(TotalSpanOverride)
            }

            super.addModels(modelsFiltered)
        } else {
            emptyState {
                id("item_placeholder")
                spanSizeOverride(TotalSpanOverride)
            }
        }
        if (isLoading) {
            infiniteLoading {
                id("loading_view")
                spanSizeOverride(TotalSpanOverride)
            }
        }
    }

    override fun buildItemModel(currentPosition: Int, item: LI?): EpoxyModel<*> {
        return if (item != null) buildItemModel(item) else buildItemPlaceholder(currentPosition)
    }

    protected abstract fun buildItemModel(item: LI): EpoxyModel<*>

    protected open fun buildItemPlaceholder(index: Int): PosterGridItemBindingModel_ {
        return PosterGridItemBindingModel_()
                .id("placeholder_$index")
    }

    open fun isHeader(model: EpoxyModel<*>): Boolean {
        return model is HeaderBindingModel_
    }
}