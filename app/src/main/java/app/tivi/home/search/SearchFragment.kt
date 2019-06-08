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
import app.tivi.AppNavigator
import app.tivi.R
import app.tivi.data.entities.TiviShow
import app.tivi.databinding.FragmentSearchBinding
import app.tivi.ui.ListItemSharedElementHelper
import app.tivi.ui.recyclerview.HideImeOnScrollListener
import app.tivi.util.GridToGridTransitioner
import app.tivi.util.TiviMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import javax.inject.Inject

internal class SearchFragment : TiviMvRxFragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var listItemSharedElementHelper: ListItemSharedElementHelper

    private val viewModel: SearchViewModel by fragmentViewModel()

    @Inject lateinit var searchViewModelFactory: SearchViewModel.Factory
    @Inject lateinit var controller: SearchEpoxyController
    @Inject lateinit var appNavigator: AppNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GridToGridTransitioner.setupFirstFragment(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listItemSharedElementHelper = ListItemSharedElementHelper(binding.recyclerview) {
            it.findViewById(R.id.show_poster)
        }

        binding.recyclerview.apply {
            setController(controller)
            addOnScrollListener(HideImeOnScrollListener())
        }

        controller.callbacks = object : SearchEpoxyController.Callbacks {
            override fun onSearchItemClicked(show: TiviShow) {
                // We should really use AndroidX navigation here, but this fragment isn't in the tree
                appNavigator.startShowDetails(show.id,
                        listItemSharedElementHelper.createForId(show.id, "poster"))
            }
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        controller.viewState = state
    }
}
