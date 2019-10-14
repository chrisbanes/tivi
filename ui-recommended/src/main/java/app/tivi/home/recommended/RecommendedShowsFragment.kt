/*
 * Copyright 2017 Google LLC
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

package app.tivi.home.recommended

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import app.tivi.SharedElementHelper
import app.tivi.common.layouts.PosterGridItemBindingModel_
import app.tivi.data.entities.findHighestRatedPoster
import app.tivi.data.resultentities.RecommendedEntryWithShow
import app.tivi.extensions.resolveThemeDrawable
import app.tivi.extensions.toActivityNavigatorExtras
import app.tivi.util.EntryGridEpoxyController
import app.tivi.util.EntryGridFragment
import com.airbnb.epoxy.EpoxyModel
import javax.inject.Inject

class RecommendedShowsFragment : EntryGridFragment<RecommendedEntryWithShow, RecommendedShowsViewModel>() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    override val viewModel: RecommendedShowsViewModel by viewModels(factoryProducer = { viewModelFactory })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gridToolbar.apply {
            navigationIcon = context.resolveThemeDrawable(android.R.attr.homeAsUpIndicator)
            setTitle(R.string.discover_recommended_title)
        }
    }

    override fun createController(): EntryGridEpoxyController<RecommendedEntryWithShow> {
        return object : EntryGridEpoxyController<RecommendedEntryWithShow>() {
            override fun buildItemModel(item: RecommendedEntryWithShow): EpoxyModel<*> {
                return PosterGridItemBindingModel_()
                        .id(item.generateStableId())
                        .posterImage(item.images.findHighestRatedPoster())
                        .tiviShow(item.show)
                        .transitionName(item.show.homepage)
                        .selected(item.show.id in state.selectedShowIds)
                        .clickListener(View.OnClickListener {
                            if (viewModel.onItemClick(item.show)) {
                                return@OnClickListener
                            }
                            onItemClicked(item)
                        })
                        .longClickListener(View.OnLongClickListener {
                            viewModel.onItemLongClick(item.show)
                        })
            }
        }
    }

    internal fun onItemClicked(item: RecommendedEntryWithShow) {
        val sharedElements = SharedElementHelper()
        binding.gridRecyclerview.findViewHolderForItemId(item.generateStableId())?.let {
            sharedElements.addSharedElement(it.itemView, "poster")
        }

        findNavController().navigate(
                R.id.activity_show_details,
                bundleOf("show_id" to item.show.id),
                null,
                sharedElements.toActivityNavigatorExtras(requireActivity())
        )
    }

    override fun startSelectionActionMode(): ActionMode? {
        return requireActivity().startActionMode(object : ActionMode.Callback {
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.menu_follow -> viewModel.followSelectedShows()
                }
                return true
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                mode.menuInflater.inflate(R.menu.action_mode_entry, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = true

            override fun onDestroyActionMode(mode: ActionMode) {
                viewModel.clearSelection()
            }
        })
    }
}
