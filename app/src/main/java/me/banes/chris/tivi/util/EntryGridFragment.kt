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

package me.banes.chris.tivi.util

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.transition.TransitionInflater
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_rv_grid.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.TiviFragment
import me.banes.chris.tivi.api.Status
import me.banes.chris.tivi.data.Entry
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.extensions.doWhenLaidOut
import me.banes.chris.tivi.extensions.observeK
import me.banes.chris.tivi.extensions.updatePadding
import me.banes.chris.tivi.ui.EndlessRecyclerViewScrollListener
import me.banes.chris.tivi.ui.ProgressTimeLatch
import me.banes.chris.tivi.ui.ShowPosterGridAdapter
import me.banes.chris.tivi.ui.SpacingItemDecorator
import javax.inject.Inject

@SuppressLint("ValidFragment")
abstract class EntryGridFragment<LI : ListItem<out Entry>, VM : EntryViewModel<LI>>(
        private val vmClass: Class<VM>
) : TiviFragment() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    protected lateinit var viewModel: VM

    private lateinit var adapter: ShowPosterGridAdapter<LI>
    private lateinit var swipeRefreshLatch: ProgressTimeLatch

    private var originalRvTopPadding = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(vmClass)

        TransitionInflater.from(context).run {
            sharedElementEnterTransition = inflateTransition(R.transition.fragment_shared_enter)
            sharedElementReturnTransition = inflateTransition(R.transition.fragment_shared_return)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rv_grid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()

        swipeRefreshLatch = ProgressTimeLatch(minShowTime = 1350) {
            grid_swipe_refresh.isRefreshing = it
        }

        val layoutManager = grid_recyclerview.layoutManager as GridLayoutManager
        adapter = createAdapter(layoutManager.spanCount)

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = adapter.getItemColumnSpan(position)
        }

        grid_recyclerview.apply {
            adapter = this@EntryGridFragment.adapter
            addItemDecoration(SpacingItemDecorator(paddingLeft))
            addOnScrollListener(EndlessRecyclerViewScrollListener(layoutManager, { _: Int, _: RecyclerView ->
                if (userVisibleHint) {
                    viewModel.onListScrolledToEnd()
                }
            }))
        }
        originalRvTopPadding = grid_recyclerview.paddingTop

        grid_swipe_refresh.setOnRefreshListener(viewModel::fullRefresh)

        grid_root.setOnApplyWindowInsetsListener { _, insets ->
            val topInset = insets.systemWindowInsetTop

            grid_toolbar.doWhenLaidOut {
                grid_recyclerview.updatePadding(paddingTop = topInset + originalRvTopPadding + grid_toolbar.height)
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
            adapter.setList(it)
            startPostponedEnterTransition()
        }

        viewModel.messages.observeK(this) {
            when (it?.status) {
                Status.SUCCESS -> {
                    swipeRefreshLatch.refreshing = false
                    adapter.isLoading = false
                }
                Status.ERROR -> {
                    swipeRefreshLatch.refreshing = false
                    adapter.isLoading = false
                    Snackbar.make(grid_recyclerview, it.message ?: "EMPTY", Snackbar.LENGTH_SHORT).show()
                }
                Status.REFRESHING -> swipeRefreshLatch.refreshing = true
                Status.LOADING_MORE -> adapter.isLoading = true
            }
        }
    }

    open fun createAdapter(spanCount: Int): ShowPosterGridAdapter<LI> = ShowPosterGridAdapter(spanCount)

}
