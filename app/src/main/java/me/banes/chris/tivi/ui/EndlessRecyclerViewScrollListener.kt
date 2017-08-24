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

package me.banes.chris.tivi.ui

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager

class EndlessRecyclerViewScrollListener(
        private val layoutManager: RecyclerView.LayoutManager,
        private val loadMore: (totalItemsCount: Int, view: RecyclerView) -> Unit)
    : RecyclerView.OnScrollListener() {

    private var loadMoreThreshold = 2
    private var previousItemCount = 0
    private var loading = true

    init {
        // Convert the threshold so that is for rows, not items
        when (layoutManager) {
            is GridLayoutManager -> {
                loadMoreThreshold *= layoutManager.spanCount
            }
            is StaggeredGridLayoutManager -> {
                loadMoreThreshold *= layoutManager.spanCount
            }
        }
    }

    fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        var lastVisibleItemPosition = 0
        val itemCount = layoutManager.itemCount

        when (layoutManager) {
            is StaggeredGridLayoutManager -> {
                val lastVisibleItemPositions = layoutManager.findLastVisibleItemPositions(null)
                // get maximum element within the list
                lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
            }
            is GridLayoutManager -> lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            is LinearLayoutManager -> lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        }

        if (itemCount < previousItemCount) {
            previousItemCount = itemCount
            if (itemCount == 0) {
                loading = true
            }
        }

        if (loading && itemCount > previousItemCount) {
            loading = false
            previousItemCount = itemCount
        }

        if (!loading && lastVisibleItemPosition + loadMoreThreshold > itemCount) {
            loadMore(itemCount, view)
            loading = true
        }
    }
}