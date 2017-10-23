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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_summary.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.home.HomeFragment
import me.banes.chris.tivi.home.discover.DiscoverViewModel.Section.POPULAR
import me.banes.chris.tivi.home.discover.DiscoverViewModel.Section.TRENDING
import me.banes.chris.tivi.ui.SpacingItemDecorator
import me.banes.chris.tivi.ui.groupieitems.EmptyPlaceholderItem
import me.banes.chris.tivi.ui.groupieitems.HeaderItem
import me.banes.chris.tivi.ui.groupieitems.ShowPosterItem
import me.banes.chris.tivi.ui.groupieitems.ShowPosterUpdatingSection

internal class DiscoverFragment : HomeFragment<DiscoverViewModel>() {

    private lateinit var gridLayoutManager: GridLayoutManager
    private val groupAdapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DiscoverViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.data.observe(this, Observer {
            it?.run { updateAdapter(it) } ?: groupAdapter.clear()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gridLayoutManager = discover_rv.layoutManager as GridLayoutManager
        gridLayoutManager.spanSizeLookup = groupAdapter.spanSizeLookup

        groupAdapter.apply {
            setOnItemClickListener { item, _ ->
                when (item) {
                    is HeaderItem -> viewModel.onSectionHeaderClicked(item.tag as DiscoverViewModel.Section)
                    is ShowPosterItem -> viewModel.onItemPostedClicked(item.show)
                }
            }
            spanCount = gridLayoutManager.spanCount
        }

        discover_rv.apply {
            adapter = groupAdapter
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }

        discover_toolbar.apply {
            title = getString(R.string.discover_title)
            inflateMenu(R.menu.home_toolbar)
            setOnMenuItemClickListener {
                onMenuItemClicked(it)
            }
        }
    }

    override fun getMenu(): Menu? = discover_toolbar.menu

    private fun updateAdapter(data: List<DiscoverViewModel.SectionPage>) {
        val spanCount = gridLayoutManager.spanCount
        groupAdapter.clear()

        data.forEach { section ->
            val group = ShowPosterUpdatingSection().apply {
                setHeader(HeaderItem(titleFromSection(section.section), section.section))
                setPlaceholder(EmptyPlaceholderItem())
                update(section.items.mapNotNull { it.show }.take(spanCount * 2))
            }
            groupAdapter.add(group)
        }
    }

    private fun titleFromSection(section: DiscoverViewModel.Section) = when (section) {
        POPULAR -> getString(R.string.discover_popular)
        TRENDING -> getString(R.string.discover_trending)
    }

    internal fun scrollToTop() {
        discover_rv.apply {
            stopScroll()
            smoothScrollToPosition(0)
        }
    }
}
