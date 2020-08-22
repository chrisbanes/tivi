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

package app.tivi.util

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import app.tivi.FragmentWithBinding
import app.tivi.api.UiError
import app.tivi.api.UiLoading
import app.tivi.common.entrygrid.R
import app.tivi.common.entrygrid.databinding.FragmentEntryGridBinding
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.extensions.doOnSizeChange
import app.tivi.extensions.postponeEnterTransitionWithTimeout
import app.tivi.extensions.scheduleStartPostponedTransitions
import app.tivi.ui.ProgressTimeLatch
import app.tivi.ui.SpacingItemDecorator
import app.tivi.ui.transitions.GridToGridTransitioner
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect

@SuppressLint("ValidFragment")
abstract class EntryGridFragment<LI, VM> : FragmentWithBinding<FragmentEntryGridBinding>()
    where LI : EntryWithShow<out Entry>, VM : EntryViewModel<LI, *> {
    protected abstract val viewModel: VM

    private lateinit var swipeRefreshLatch: ProgressTimeLatch
    private lateinit var controller: EntryGridEpoxyController<LI>

    private var currentActionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        controller = createController()

        GridToGridTransitioner.setupSecondFragment(this, R.id.grid_appbar) {
            requireBinding().gridRecyclerview.itemAnimator = DefaultItemAnimator()
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentEntryGridBinding {
        return FragmentEntryGridBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(binding: FragmentEntryGridBinding, savedInstanceState: Bundle?) {
        postponeEnterTransitionWithTimeout()

        swipeRefreshLatch = ProgressTimeLatch(minShowTime = 1350) {
            binding.gridSwipeRefresh.isRefreshing = it
        }

        binding.gridToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.gridAppbar.doOnSizeChange {
            binding.gridRecyclerview.updatePadding(top = it.height)
            binding.gridSwipeRefresh.setProgressViewOffset(
                true, 0,
                it.height + binding.gridSwipeRefresh.progressCircleDiameter / 2
            )
            true
        }

        binding.gridRecyclerview.apply {
            // We set the item animator to null since it can interfere with the enter/shared element
            // transitions
            itemAnimator = null

            setController(controller)
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }

        binding.gridSwipeRefresh.setOnRefreshListener(viewModel::refresh)

        lifecycleScope.launchWhenStarted {
            viewModel.pagedList.collect { controller.submitList(it) }
        }

        viewModel.liveData.observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: EntryViewState) {
        controller.state = state

        when (val status = state.status) {
            is UiError -> {
                swipeRefreshLatch.refreshing = false
                Snackbar.make(requireView(), status.message, Snackbar.LENGTH_SHORT).show()
            }
            is UiLoading -> swipeRefreshLatch.refreshing = status.fullRefresh
            else -> swipeRefreshLatch.refreshing = false
        }

        if (state.selectionOpen && currentActionMode == null) {
            currentActionMode = startSelectionActionMode()
        } else if (!state.selectionOpen) {
            currentActionMode?.finish()
            currentActionMode = null
        }

        if (currentActionMode != null) {
            currentActionMode?.title = getString(
                R.string.selection_title,
                state.selectedShowIds.size
            )
        }

        if (state.isLoaded) {
            // First time we've had state, start any postponed transitions
            scheduleStartPostponedTransitions()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        currentActionMode?.finish()
        currentActionMode = null
    }

    abstract fun startSelectionActionMode(): ActionMode?

    abstract fun createController(): EntryGridEpoxyController<LI>
}
