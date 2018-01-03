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

package me.banes.chris.tivi.home.trending

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_rv_grid.*
import me.banes.chris.tivi.PosterGridItemBindingModel_
import me.banes.chris.tivi.R
import me.banes.chris.tivi.SharedElementHelper
import me.banes.chris.tivi.data.entities.TrendingListItem
import me.banes.chris.tivi.home.HomeNavigator
import me.banes.chris.tivi.home.HomeNavigatorViewModel
import me.banes.chris.tivi.util.EntryGridEpoxyController
import me.banes.chris.tivi.util.EntryGridFragment

class TrendingShowsFragment : EntryGridFragment<TrendingListItem, TrendingShowsViewModel>(TrendingShowsViewModel::class.java) {

    private lateinit var homeNavigator: HomeNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeNavigator = ViewModelProviders.of(activity!!, viewModelFactory).get(HomeNavigatorViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        grid_toolbar.apply {
            title = getString(R.string.discover_trending)
            setNavigationOnClickListener {
                viewModel.onUpClicked(homeNavigator)
            }
        }
    }

    override fun createController(): EntryGridEpoxyController<TrendingListItem> {
        return object : EntryGridEpoxyController<TrendingListItem>() {
            override fun buildItemModel(item: TrendingListItem): PosterGridItemBindingModel_ {
                return super.buildItemModel(item)
                        .annotationLabel(item.entry?.watchers.toString())
                        .annotationIcon(R.drawable.ic_eye_12dp)
            }
        }
    }

    override fun onItemClicked(item: TrendingListItem) {
        val sharedElements = SharedElementHelper()
        grid_recyclerview.findViewHolderForItemId(item.generateStableId())?.let {
            sharedElements.addSharedElement(it.itemView, "poster")
        }
        viewModel.onItemClicked(item, homeNavigator, sharedElements)
    }
}
