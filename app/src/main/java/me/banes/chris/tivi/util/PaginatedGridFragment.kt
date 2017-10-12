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
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_rv_grid.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.TiviFragment
import me.banes.chris.tivi.api.Status
import me.banes.chris.tivi.data.PaginatedEntry
import me.banes.chris.tivi.ui.EndlessRecyclerViewScrollListener
import me.banes.chris.tivi.ui.SpacingItemDecorator
import me.banes.chris.tivi.ui.TiviShowGridAdapter
import javax.inject.Inject

@SuppressLint("ValidFragment")
abstract class PaginatedGridFragment<EC : PaginatedEntry, VM : PaginatedTraktViewModel<EC>>(
        private val vmClass: Class<VM>?) : TiviFragment() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    protected lateinit var viewModel: VM

    private val adapter = TiviShowGridAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rv_grid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        grid_recyclerview.apply {
            adapter = this@PaginatedGridFragment.adapter
            addItemDecoration(SpacingItemDecorator(paddingLeft))
            addOnScrollListener(EndlessRecyclerViewScrollListener(
                    grid_recyclerview.layoutManager, { _: Int, _: RecyclerView ->
                if (userVisibleHint) viewModel.onListScrolledToEnd()
            }))
        }

        grid_swipe_refresh.setOnRefreshListener { viewModel.fullRefresh() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(vmClass)
    }

    override fun onStart() {
        super.onStart()

        viewModel.data.observe(this, Observer {
            it.let { adapter.updateItems(it!!.map { it.show!! }) }
        })

        viewModel.messages.observe(this, Observer {
            when (it?.status) {
                Status.SUCCESS -> {
                    grid_swipe_refresh.isRefreshing = false
                    progress_loadmore.visibility = View.GONE
                }
                Status.ERROR -> {
                    grid_swipe_refresh.isRefreshing = false
                    progress_loadmore.visibility = View.GONE
                    Snackbar.make(grid_recyclerview, it.message ?: "EMPTY", Snackbar.LENGTH_SHORT).show()
                }
                Status.REFRESHING -> {
                    grid_swipe_refresh.isRefreshing = true
                }
                Status.LOADING_MORE -> {
                    progress_loadmore.visibility = View.VISIBLE
                }
            }
        })
    }

}
