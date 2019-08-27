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
import android.content.Context
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import app.tivi.TiviFragment
import app.tivi.api.UiError
import app.tivi.api.UiLoading
import app.tivi.common.entrygrid.R
import app.tivi.common.entrygrid.databinding.FragmentEntryGridBinding
import app.tivi.common.epoxy.StickyHeaderScrollListener
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.ui.ProgressTimeLatch
import app.tivi.ui.SpacingItemDecorator
import app.tivi.ui.transitions.GridToGridTransitioner
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("ValidFragment")
abstract class EntryGridFragment<LI : EntryWithShow<out Entry>, VM : EntryViewModel<LI, *>> : TiviFragment() {
    protected abstract val viewModel: VM

    @Inject internal lateinit var _fakeInjection: Context

    private lateinit var swipeRefreshLatch: ProgressTimeLatch

    private lateinit var controller: EntryGridEpoxyController<LI>
    protected lateinit var binding: FragmentEntryGridBinding

    private var currentActionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        controller = createController()

        GridToGridTransitioner.setupSecondFragment(this) {
            binding.gridRecyclerview.itemAnimator = DefaultItemAnimator()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEntryGridBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()

        swipeRefreshLatch = ProgressTimeLatch(minShowTime = 1350) {
            binding.gridSwipeRefresh.isRefreshing = it
        }

        binding.gridRecyclerview.apply {
            // We set the item animator to null since it can interfere with the enter/shared element
            // transitions
            itemAnimator = null

            setController(controller)
            addItemDecoration(SpacingItemDecorator(paddingLeft))
            addOnScrollListener(StickyHeaderScrollListener(controller, controller::isHeader, binding.headerHolder))
        }

        binding.gridSwipeRefresh.setOnRefreshListener(viewModel::refresh)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.viewState.collect { invalidate(it) }
        }
    }

    private fun invalidate(state: EntryViewState<LI>) {
        controller.state = state
        controller.submitList(state.liveList)

        when (val status = state.status) {
            is UiError -> {
                swipeRefreshLatch.refreshing = false
                Snackbar.make(requireView(),
                        status.exception?.localizedMessage
                                ?: getString(R.string.error_generic),
                        Snackbar.LENGTH_SHORT
                ).show()
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
            currentActionMode?.title = getString(R.string.selection_title,
                    state.selectedShowIds.size)
        }

        if (state.isLoaded) {
            // First time we've had state, start any postponed transitions
            scheduleStartPostponedTransitions()
        }
    }

    abstract fun startSelectionActionMode(): ActionMode?

    abstract fun createController(): EntryGridEpoxyController<LI>
}
