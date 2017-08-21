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
 *
 */

package me.banes.chris.tivi.home.discover

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_discover.*
import kotlinx.android.synthetic.main.header_item.view.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.data.TiviShow
import me.banes.chris.tivi.home.HomeFragment
import me.banes.chris.tivi.home.discover.DiscoverViewModel.Section.*
import me.banes.chris.tivi.ui.SpacingItemDecorator
import me.banes.chris.tivi.ui.groupieitems.ShowPosterItem
import me.banes.chris.tivi.ui.groupieitems.ShowPosterUpdatingSection

internal class DiscoverFragment : HomeFragment<DiscoverViewModel>() {

    private lateinit var gridLayoutManager: GridLayoutManager
    private val groupAdapter = GroupAdapter<ViewHolder>()

    private val groups = ArrayMap<DiscoverViewModel.Section, ShowPosterUpdatingSection>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(DiscoverViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.data.observe(this, Observer {
            if (it != null) {
                updateAdapter(it)
            }
        })
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gridLayoutManager = discover_rv.layoutManager as GridLayoutManager
        gridLayoutManager.spanSizeLookup = groupAdapter.spanSizeLookup

        groupAdapter.apply {
            setOnItemClickListener { item, _ ->
                when (item) {
                    is HeaderItem -> viewModel.onSectionHeaderClicked(item.section)
                    is ShowPosterItem -> viewModel.onItemPostedClicked(item.show)
                }
            }
            spanCount = gridLayoutManager.spanCount
        }

        discover_rv.apply {
            adapter = groupAdapter
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }

        discover_toolbar?.apply {
            title = getString(R.string.home_nav_discover)
            inflateMenu(R.menu.home_toolbar)
            setOnMenuItemClickListener {
                onMenuItemClicked(it)
            }
        }
    }

    override fun findUserAvatarMenuItem(): MenuItem? {
        return discover_toolbar.menu.findItem(R.id.home_menu_user_avatar)
    }

    override fun findUserLoginMenuItem(): MenuItem? {
        return discover_toolbar.menu.findItem(R.id.home_menu_user_login)
    }

    private fun updateAdapter(data: Map<DiscoverViewModel.Section, List<TiviShow>>) {
        if (groups.size != data.size) {
            groups.clear()
            for ((category) in data) {
                val group = ShowPosterUpdatingSection()
                groups[category] = group
                group.setHeader(HeaderItem(category))
                groupAdapter.add(group)
            }
        }
        val spanCount = gridLayoutManager.spanCount
        for ((category, items) in data) {
            groups[category]?.update(items.take(spanCount * 2))
        }
    }

    private fun titleFromSection(section: DiscoverViewModel.Section) = when (section) {
        POPULAR -> getString(R.string.discover_popular)
        TRENDING -> getString(R.string.discover_trending)
    }

    internal inner class HeaderItem(val section: DiscoverViewModel.Section) : Item<ViewHolder>() {
        override fun getLayout() = R.layout.header_item

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.header_title.text = titleFromSection(section)
        }
    }

}
