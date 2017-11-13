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

package me.banes.chris.tivi.home.discover

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_summary.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.data.entities.TrendingEntry
import me.banes.chris.tivi.extensions.filterItemsViewHolders
import me.banes.chris.tivi.extensions.observeK
import me.banes.chris.tivi.home.HomeFragment
import me.banes.chris.tivi.home.HomeNavigator
import me.banes.chris.tivi.home.HomeNavigatorViewModel
import me.banes.chris.tivi.home.discover.DiscoverViewModel.Section.POPULAR
import me.banes.chris.tivi.home.discover.DiscoverViewModel.Section.TRENDING
import me.banes.chris.tivi.ui.SharedElementHelper
import me.banes.chris.tivi.ui.SpacingItemDecorator
import me.banes.chris.tivi.ui.groupieitems.EmptyPlaceholderItem
import me.banes.chris.tivi.ui.groupieitems.HeaderItem
import me.banes.chris.tivi.ui.groupieitems.ShowPosterItem
import me.banes.chris.tivi.ui.groupieitems.ShowPosterSection
import me.banes.chris.tivi.ui.groupieitems.TrendingPosterItem
import me.banes.chris.tivi.ui.groupieitems.TrendingShowPosterSection

internal class DiscoverFragment : HomeFragment<DiscoverViewModel>() {

    private lateinit var gridLayoutManager: GridLayoutManager
    private val groupAdapter = GroupAdapter<ViewHolder>()

    private val sectionMap = mutableMapOf<DiscoverViewModel.Section, Section>()

    private lateinit var homeNavigator: HomeNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DiscoverViewModel::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        homeNavigator = ViewModelProviders.of(activity!!, viewModelFactory).get(HomeNavigatorViewModel::class.java)

        viewModel.data.observeK(this) {
            it?.run { updateAdapter(it) } ?: groupAdapter.clear()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gridLayoutManager = summary_rv.layoutManager as GridLayoutManager
        gridLayoutManager.spanSizeLookup = groupAdapter.spanSizeLookup

        groupAdapter.apply {
            setOnItemClickListener { item, _ ->
                when (item) {
                    is HeaderItem -> {
                        val cSection = item.tag as DiscoverViewModel.Section
                        val sharedElements = SharedElementHelper()
                        sectionMap[cSection]!!
                                .filterItemsViewHolders(summary_rv) { it.layout == R.layout.grid_item }
                                .map(ViewHolder::itemView)
                                .forEach { sharedElements.addSharedElement(it) }

                        viewModel.onSectionHeaderClicked(homeNavigator, cSection, sharedElements)
                    }
                    is ShowPosterItem -> viewModel.onItemPostedClicked(homeNavigator, item.show)
                    is TrendingPosterItem -> viewModel.onItemPostedClicked(homeNavigator, item.show)
                }
            }
            spanCount = gridLayoutManager.spanCount
        }

        summary_rv.apply {
            adapter = groupAdapter
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }

        summary_toolbar.apply {
            title = getString(R.string.discover_title)
            inflateMenu(R.menu.home_toolbar)
            setOnMenuItemClickListener {
                onMenuItemClicked(it)
            }
        }
    }

    override fun getMenu(): Menu? = summary_toolbar.menu

    private fun updateAdapter(data: List<DiscoverViewModel.SectionPage>) {
        val spanCount = gridLayoutManager.spanCount
        groupAdapter.clear()
        sectionMap.clear()

        data.forEach { section ->
            val group: Section

            when (section.section) {
                TRENDING -> {
                    group = TrendingShowPosterSection().apply {
                        update(section.items
                                .filter { it.show != null }
                                .take(spanCount * 2) as List<ListItem<TrendingEntry>>)
                    }
                }
                POPULAR -> {
                    group = ShowPosterSection().apply {
                        update(section.items
                                .filter { it.show != null }
                                .take(spanCount * 2)
                                .map { it.show!! })
                    }
                }
            }

            group.run {
                setHeader(HeaderItem(titleFromSection(section.section), section.section))
                setPlaceholder(EmptyPlaceholderItem())
                groupAdapter.add(this)
            }
            sectionMap[section.section] = group
        }
    }

    private fun titleFromSection(section: DiscoverViewModel.Section) = when (section) {
        POPULAR -> getString(R.string.discover_popular)
        TRENDING -> getString(R.string.discover_trending)
    }

    internal fun scrollToTop() {
        summary_rv.apply {
            stopScroll()
            smoothScrollToPosition(0)
        }
        summary_appbarlayout.setExpanded(true)
    }
}
