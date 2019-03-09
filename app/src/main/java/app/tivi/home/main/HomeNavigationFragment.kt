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

package app.tivi.home.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import app.tivi.R
import app.tivi.databinding.FragmentHomeBinding
import app.tivi.extensions.updateConstraintSets
import app.tivi.home.discover.DiscoverFragment
import app.tivi.home.library.followed.FollowedFragment
import app.tivi.home.library.watched.WatchedFragment
import app.tivi.util.TiviMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState

class HomeNavigationFragment : TiviMvRxFragment() {

    companion object {
        const val ROOT_FRAGMENT = "root"
    }

    private val viewModel: HomeNavigationViewModel by fragmentViewModel()

    private lateinit var binding: FragmentHomeBinding

    private val controller = HomeNavigationEpoxyController(object : HomeNavigationEpoxyController.Callbacks {
        override fun onNavigationItemSelected(item: HomeNavigationItem) {
            viewModel.onNavigationItemSelected(item)
        }
    })

    private lateinit var searchView: SearchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        view.setOnApplyWindowInsetsListener { _, insets ->
            binding.homeRoot.updateConstraintSets {
                it.constrainHeight(R.id.status_scrim, insets.systemWindowInsetTop)
            }
            // Just return insets
            insets
        }
        // Finally, request some insets
        view.requestApplyInsets()

        //        binding.summaryToolbar.apply {
//            inflateMenu(R.menu.discover_toolbar)
//            setOnMenuItemClickListener(this@DiscoverFragment::onMenuItemClicked)
//
//            val searchItem = menu.findItem(R.id.discover_search)
//            searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
//                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
//                    viewModel.onSearchOpened()
//                    return true
//                }
//
//                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
//                    viewModel.onSearchClosed()
//                    return true
//                }
//            })
//
//            searchView = menu.findItem(R.id.discover_search).actionView as SearchView
//        }
//
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String): Boolean {
//                viewModel.onSearchQueryChanged(query)
//                return true
//            }
//
//            override fun onQueryTextChange(query: String): Boolean {
//                viewModel.onSearchQueryChanged(query)
//                return true
//            }
//        })

        binding.homeNavRv.setController(controller)
    }

    override fun invalidate() {
        withState(viewModel) {
            controller.setData(it)

            showNavigationItem(it.currentNavigationItem)
        }
    }

    private fun showNavigationItem(item: HomeNavigationItem) {
        val newFragment: Fragment = when (item) {
            HomeNavigationItem.DISCOVER -> DiscoverFragment()
            HomeNavigationItem.FOLLOWED -> FollowedFragment()
            HomeNavigationItem.WATCHED -> WatchedFragment()
        }

        val currentFragment = childFragmentManager.findFragmentById(R.id.home_content)

        if (currentFragment == null || currentFragment::class != newFragment::class) {
            childFragmentManager.popBackStackImmediate(ROOT_FRAGMENT, 0)

            childFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.home_content, newFragment, ROOT_FRAGMENT)
                    .commit()
        }
    }
}
