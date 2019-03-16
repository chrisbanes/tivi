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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import app.tivi.R
import app.tivi.TiviFragment
import app.tivi.api.Status
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.extensions.observeNotNull
import app.tivi.ui.ProgressTimeLatch
import app.tivi.ui.SpacingItemDecorator
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_rv_grid.*
import javax.inject.Inject

@SuppressLint("ValidFragment")
abstract class EntryGridFragment<LI : EntryWithShow<out Entry>, VM : EntryViewModel<LI>>(
    private val vmClass: Class<VM>
) : TiviFragment() {
    protected lateinit var viewModel: VM

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var swipeRefreshLatch: ProgressTimeLatch
    private var originalRvTopPadding = 0

    private lateinit var controller: EntryGridEpoxyController<LI>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(vmClass)

        controller = createController()
        controller.callbacks = object : EntryGridEpoxyController.Callbacks<LI> {
            override fun onItemClicked(item: LI) {
                this@EntryGridFragment.onItemClicked(item)
            }
        }

        GridToGridTransitioner.setupSecondFragment(this) {
            grid_recyclerview?.itemAnimator = DefaultItemAnimator()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rv_grid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()

        swipeRefreshLatch = ProgressTimeLatch(minShowTime = 1350) {
            grid_swipe_refresh?.isRefreshing = it
        }

        grid_recyclerview.apply {
            // We set the item animator to null since it can interfere with the enter/shared element
            // transitions
            itemAnimator = null

            setController(controller)
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }
        originalRvTopPadding = grid_recyclerview.paddingTop

        grid_swipe_refresh.setOnRefreshListener(viewModel::refresh)

        viewModel.viewState.observeNotNull(this) {
            controller.tmdbImageUrlProvider = it.tmdbImageUrlProvider
            controller.submitList(it.liveList)

            when (it.uiResource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLatch.refreshing = false
                    controller.isLoading = false
                }
                Status.ERROR -> {
                    swipeRefreshLatch.refreshing = false
                    controller.isLoading = false
                    Snackbar.make(view, it.uiResource.message ?: "EMPTY", Snackbar.LENGTH_SHORT).show()
                }
                Status.REFRESHING -> swipeRefreshLatch.refreshing = true
                Status.LOADING_MORE -> controller.isLoading = true
            }

            if (it.isLoaded) {
                // First time we've had state, start any postponed transitions
                scheduleStartPostponedTransitions()
            }
        }
    }

    abstract fun onItemClicked(item: LI)

    open fun createController(): EntryGridEpoxyController<LI> {
        return EntryGridEpoxyController()
    }
}
