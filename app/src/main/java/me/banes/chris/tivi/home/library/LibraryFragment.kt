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

package me.banes.chris.tivi.home.library

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
import me.banes.chris.tivi.SharedElementHelper
import me.banes.chris.tivi.extensions.doOnPreDraw
import me.banes.chris.tivi.extensions.observeK
import me.banes.chris.tivi.home.HomeFragment
import me.banes.chris.tivi.home.HomeNavigator
import me.banes.chris.tivi.home.HomeNavigatorViewModel
import me.banes.chris.tivi.home.discover.SectionedHelper
import me.banes.chris.tivi.ui.SpacingItemDecorator
import me.banes.chris.tivi.ui.groupieitems.HeaderItem
import me.banes.chris.tivi.ui.groupieitems.ShowPosterItem
import me.banes.chris.tivi.ui.groupieitems.ShowPosterSection
import me.banes.chris.tivi.util.GridToGridTransitioner

class LibraryFragment : HomeFragment<LibraryViewModel>() {

    private lateinit var homeNavigator: HomeNavigator

    private lateinit var gridLayoutManager: GridLayoutManager
    private val groupAdapter = GroupAdapter<ViewHolder>()

    private lateinit var sectionHelper: SectionedHelper<LibraryViewModel.Section>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LibraryViewModel::class.java)
        homeNavigator = ViewModelProviders.of(activity!!, viewModelFactory).get(HomeNavigatorViewModel::class.java)

        GridToGridTransitioner.setupFirstFragment(this,
                intArrayOf(R.id.summary_appbarlayout, R.id.summary_status_scrim))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.data.observeK(this) {
            if (it != null && !it.isEmpty() && groupAdapter.itemCount == 0) {
                scheduleStartPostponedTransitions()
            }
            it?.run {
                sectionHelper.update(it.map { it.section to it.items }.toMap())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val container = view.parent as ViewGroup
        postponeEnterTransition()
        container.doOnPreDraw {
            startPostponedEnterTransition()
        }

        gridLayoutManager = summary_rv.layoutManager as GridLayoutManager
        gridLayoutManager.spanSizeLookup = groupAdapter.spanSizeLookup

        sectionHelper = SectionedHelper(
                summary_rv,
                groupAdapter,
                gridLayoutManager.spanCount,
                { _, list -> ShowPosterSection(list.mapNotNull { it.show }) },
                this::titleFromSection)

        groupAdapter.apply {
            setOnItemClickListener { item, _ ->
                when (item) {
                    is HeaderItem -> {
                        val section = item.tag as LibraryViewModel.Section

                        val sharedElements = SharedElementHelper()
                        sectionHelper.addSharedElementsForSection(section, sharedElements)

                        viewModel.onSectionHeaderClicked(homeNavigator, section, sharedElements)
                    }
                    is ShowPosterItem -> viewModel.onItemPostedClicked(homeNavigator, item.show)
                }
            }
            spanCount = gridLayoutManager.spanCount
        }

        summary_rv.apply {
            adapter = groupAdapter
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }

        summary_toolbar.apply {
            title = getString(R.string.library_title)
            inflateMenu(R.menu.home_toolbar)
            setOnMenuItemClickListener {
                onMenuItemClicked(it)
            }
        }
    }

    override fun canStartTransition(): Boolean {
        return groupAdapter.itemCount > 0
    }

    override fun getMenu(): Menu? = summary_toolbar.menu

    private fun titleFromSection(section: LibraryViewModel.Section) = when (section) {
        LibraryViewModel.Section.WATCHED -> getString(R.string.library_watched)
        else -> "FIXME"
    }

    internal fun scrollToTop() {
        summary_rv.apply {
            stopScroll()
            smoothScrollToPosition(0)
        }
        summary_appbarlayout.setExpanded(true)
    }
}
