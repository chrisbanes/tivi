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

package app.tivi.home.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.marginBottom
import androidx.core.view.updatePadding
import app.tivi.AppNavigator
import app.tivi.DaggerMvRxFragment
import app.tivi.data.entities.TiviShow
import app.tivi.extensions.doOnLayouts
import app.tivi.extensions.hideSoftInput
import app.tivi.home.search.databinding.FragmentSearchBinding
import app.tivi.ui.createSharedElementHelperForItemId
import app.tivi.ui.recyclerview.HideImeOnScrollListener
import app.tivi.ui.transitions.GridToGridTransitioner
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import javax.inject.Inject

internal class SearchFragment : DaggerMvRxFragment() {
    private lateinit var binding: FragmentSearchBinding

    private val viewModel: SearchViewModel by fragmentViewModel()

    @Inject lateinit var searchViewModelFactory: SearchViewModel.Factory
    @Inject lateinit var controller: SearchEpoxyController
    @Inject lateinit var appNavigator: AppNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GridToGridTransitioner.setupFirstFragment(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchRecyclerview.apply {
            setController(controller)
            addOnScrollListener(HideImeOnScrollListener())
        }

        binding.searchAppbar.doOnLayouts { appBar ->
            binding.searchRecyclerview.updatePadding(top = appBar.bottom + appBar.marginBottom)
            true
        }

        binding.searchSearchview.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.setSearchQuery(query)
                    hideSoftInput()
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    viewModel.setSearchQuery(newText)
                    return true
                }
            })
        }

        controller.callbacks = object : SearchEpoxyController.Callbacks {
            override fun onSearchItemClicked(show: TiviShow) {
                // We should really use AndroidX navigation here, but this fragment isn't in the tree
                val extras = binding.searchRecyclerview.createSharedElementHelperForItemId(show.id, "poster") {
                    it.findViewById(R.id.show_poster)
                }
                appNavigator.startShowDetails(show.id, extras)
            }
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.state = state
        controller.viewState = state
    }
}
