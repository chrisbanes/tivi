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

package app.tivi.util

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import app.tivi.R
import app.tivi.TiviFragment
import app.tivi.api.Status
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.extensions.observeK
import app.tivi.ui.ProgressTimeLatch
import app.tivi.ui.SpacingItemDecorator
import kotlinx.android.synthetic.main.fragment_rv_grid.*

@SuppressLint("ValidFragment")
abstract class EntryGridFragment<LI : EntryWithShow<out Entry>, VM : EntryViewModel<LI>>(
    private val vmClass: Class<VM>
) : TiviFragment() {
    protected lateinit var viewModel: VM

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

        GridToGridTransitioner.setupSecondFragment(this,
                intArrayOf(R.id.grid_toolbar, R.id.grid_status_scrim)) {
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

        val layoutManager = grid_recyclerview.layoutManager as GridLayoutManager

        grid_recyclerview.apply {
            // We set the item animator to null since it can interfere with the enter/shared element
            // transitions
            itemAnimator = null

            setController(controller)
            addItemDecoration(SpacingItemDecorator(paddingLeft))
        }
        originalRvTopPadding = grid_recyclerview.paddingTop

        grid_swipe_refresh.setOnRefreshListener(viewModel::refresh)

        grid_root.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        grid_root.setOnApplyWindowInsetsListener { _, insets ->
            val topInset = insets.systemWindowInsetTop

            grid_toolbar.doOnLayout {
                grid_recyclerview.updatePadding(top = topInset + originalRvTopPadding + grid_toolbar.height)
            }

            val tlp = (grid_toolbar.layoutParams as ConstraintLayout.LayoutParams)
            tlp.topMargin = topInset
            grid_toolbar.layoutParams = tlp

            val scrimLp = (grid_status_scrim.layoutParams as ConstraintLayout.LayoutParams)
            scrimLp.height = topInset
            grid_status_scrim.layoutParams = scrimLp
            grid_status_scrim.visibility = View.VISIBLE

            insets.consumeSystemWindowInsets()
        }
        grid_root.requestApplyInsets()

        grid_recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    val scrollAmountPx = recyclerView.paddingTop
                    val scrollAmount = recyclerView.getChildAt(0).top / scrollAmountPx.toFloat()
                    grid_toolbar.apply {
                        visibility = View.VISIBLE
                        translationY = -scrollAmountPx * (1f - scrollAmount) * 0.5f
                        alpha = scrollAmount
                    }
                } else {
                    grid_toolbar.visibility = View.GONE
                }
            }
        })

        viewModel.liveList.observeK(this) {
            controller.setList(it)
            scheduleStartPostponedTransitions()
        }

        viewModel.viewState.observeK(this) {
            controller.tmdbImageUrlProvider = it?.tmdbImageUrlProvider

            it?.uiResource?.let {
                when (it.status) {
                    Status.SUCCESS -> {
                        swipeRefreshLatch.refreshing = false
                        controller.isLoading = false
                    }
                    Status.ERROR -> {
                        swipeRefreshLatch.refreshing = false
                        controller.isLoading = false
                        Snackbar.make(grid_recyclerview, it.message ?: "EMPTY", Snackbar.LENGTH_SHORT).show()
                    }
                    Status.REFRESHING -> swipeRefreshLatch.refreshing = true
                    Status.LOADING_MORE -> controller.isLoading = true
                }
            }
        }
    }

    abstract fun onItemClicked(item: LI)

    open fun createController(): EntryGridEpoxyController<LI> {
        return EntryGridEpoxyController()
    }
}
