/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.util

import android.view.View
import com.airbnb.epoxy.paging.PagingEpoxyController
import me.banes.chris.tivi.data.Entry
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.tmdb.TmdbImageUrlProvider
import me.banes.chris.tivi.ui.epoxymodels.ShowPosterModel_
import me.banes.chris.tivi.ui.epoxymodels.emptyPlaceholder
import me.banes.chris.tivi.ui.epoxymodels.loadingView

open class EntryGridEpoxyController<LI : ListItem<out Entry>> : PagingEpoxyController<LI>() {

    internal var callbacks: Callbacks<LI>? = null

    var isLoading = false
        set(value) {
            if (value != field) {
                field = value
                requestModelBuild()
            }
        }

    var tmdbImageUrlProvider: TmdbImageUrlProvider? = null
        set(value) {
            if (value != field) {
                field = value
                requestModelBuild()
            }
        }

    interface Callbacks<in LI> {
        fun onItemClicked(item: LI)
    }

    override fun buildModels(items: List<LI?>?) {
        if (items != null && !items.isEmpty()) {
            items.forEachIndexed { index, item ->
                when {
                    item != null -> buildItemModel(item)
                    else -> buildItemPlaceholder(index)
                }.addTo(this)
            }
        } else {
            emptyPlaceholder {
                id("item_placeholder")
            }
        }

        if (isLoading) {
            loadingView {
                id("loading_view")
            }
        }
    }

    protected open fun buildItemModel(item: LI): ShowPosterModel_ {
        return ShowPosterModel_()
                .id(item.generateStableId())
                .tmdbImageUrlProvider(tmdbImageUrlProvider)
                .title(item.show?.title)
                .posterPath(item.show?.tmdbPosterPath)
                .transName(item.show?.homepage)
                .clickListener(View.OnClickListener {
                    callbacks?.onItemClicked(item)
                })
    }

    protected open fun buildItemPlaceholder(index: Int): ShowPosterModel_ = ShowPosterModel_().apply {
        id("placeholder_$index")
    }
}