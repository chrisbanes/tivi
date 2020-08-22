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
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.net.toUri
import androidx.core.view.marginBottom
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import app.tivi.FragmentWithBinding
import app.tivi.data.entities.TiviShow
import app.tivi.extensions.doOnLayouts
import app.tivi.extensions.hideSoftInput
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.home.search.databinding.FragmentSearchBinding
import app.tivi.ui.createSharedElementHelperForItemId
import app.tivi.ui.recyclerview.HideImeOnScrollListener
import app.tivi.ui.transitions.GridToGridTransitioner
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class SearchFragment : FragmentWithBinding<FragmentSearchBinding>() {
    private val viewModel: SearchViewModel by viewModels()

    @Inject internal lateinit var controller: SearchEpoxyController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GridToGridTransitioner.setupFirstFragment(this)
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(binding: FragmentSearchBinding, savedInstanceState: Bundle?) {
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
                findNavController().navigate(
                    "app.tivi://show/${show.id}".toUri(),
                    null,
                    extras.toActivityNavigatorExtras(requireActivity())
                )
            }
        }

        viewModel.liveData.observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: SearchViewState) {
        val binding = requireBinding()
        binding.state = state
        controller.state = state
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller.clear()
    }
}
