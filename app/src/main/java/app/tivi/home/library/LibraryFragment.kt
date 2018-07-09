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

package app.tivi.home.library

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import app.tivi.R
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.extensions.observeNotNull
import app.tivi.home.HomeFragment
import app.tivi.home.HomeNavigator
import app.tivi.home.HomeNavigatorViewModel
import app.tivi.ui.ListItemSharedElementHelper
import app.tivi.ui.SpacingItemDecorator
import app.tivi.ui.epoxy.EmptyEpoxyController
import app.tivi.util.GridToGridTransitioner
import com.airbnb.epoxy.EpoxyController
import kotlinx.android.synthetic.main.fragment_library.*

class LibraryFragment : HomeFragment<LibraryViewModel>() {
    private lateinit var homeNavigator: HomeNavigator
    private lateinit var gridLayoutManager: GridLayoutManager

    private val listItemSharedElementHelper by lazy(LazyThreadSafetyMode.NONE) {
        ListItemSharedElementHelper(library_rv)
    }

    private var controller: EpoxyController = EmptyEpoxyController
        set(value) {
            if (field != value) {
                field = value
                library_rv.adapter = value.adapter
                value.spanCount = gridLayoutManager.spanCount
                gridLayoutManager.spanSizeLookup = controller.spanSizeLookup
            }
        }

    private val filterController = LibraryFiltersEpoxyController(object : LibraryFiltersEpoxyController.Callbacks {
        override fun onFilterSelected(filter: LibraryFilter) {
            closeFilterPanel()
            viewModel.onFilterSelected(filter)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LibraryViewModel::class.java)
        homeNavigator = ViewModelProviders.of(activity!!, viewModelFactory).get(HomeNavigatorViewModel::class.java)

        GridToGridTransitioner.setupFirstFragment(this,
                intArrayOf(R.id.summary_appbarlayout, R.id.summary_status_scrim))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.data.observeNotNull(this) { viewState ->
            update(viewState)
            scheduleStartPostponedTransitions()
        }
    }

    private fun update(viewState: LibraryViewState) {
        filterController.setData(viewState)

        library_toolbar.setTitle(viewState.filter.labelResource)

        when (viewState) {
            is LibraryWatchedViewState -> {
                val c = (controller as? LibraryWatchedEpoxyController ?: createWatchedController())
                c.tmdbImageUrlProvider = viewState.tmdbImageUrlProvider
                c.setList(viewState.watchedShows)
                c.isEmpty = viewState.isEmpty
                controller = c
            }
            is LibraryFollowedViewState -> {
                val c = controller as? LibraryFollowedEpoxyController ?: createFollowedController()
                c.tmdbImageUrlProvider = viewState.tmdbImageUrlProvider
                c.setList(viewState.followedShows)
                c.isEmpty = viewState.isEmpty
                controller = c
            }
        }

        // Close the filter pane if needed
        closeFilterPanel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()

        // Setup span and columns
        gridLayoutManager = library_rv.layoutManager as GridLayoutManager

        library_rv.apply {
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }

        library_filters_rv.adapter = filterController.adapter

        library_toolbar.apply {
            title = getString(R.string.library_title)
            inflateMenu(R.menu.home_toolbar)
            setOnMenuItemClickListener(this@LibraryFragment::onMenuItemClicked)
        }
    }

    override fun getMenu(): Menu? = library_toolbar.menu

    internal fun scrollToTop() {
        library_rv.stopScroll()
        library_rv.smoothScrollToPosition(0)
    }

    private fun closeFilterPanel() {
        library_motion.transitionToStart()
    }

    private fun createWatchedController() = LibraryWatchedEpoxyController(
            object : LibraryWatchedEpoxyController.Callbacks {
                override fun onItemClicked(item: WatchedShowEntryWithShow) {
                    viewModel.onItemPostedClicked(homeNavigator, item.show,
                            listItemSharedElementHelper.createForItem(item, "poster")
                    )
                }
            })

    private fun createFollowedController() = LibraryFollowedEpoxyController(
            object : LibraryFollowedEpoxyController.Callbacks {
                override fun onItemClicked(item: FollowedShowEntryWithShow) {
                    viewModel.onItemPostedClicked(homeNavigator, item.show,
                            listItemSharedElementHelper.createForItem(item, "poster")
                    )
                }
            })
}
