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

import android.arch.paging.PagedListAdapter
import android.support.v7.recyclerview.extensions.DiffCallback
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import me.banes.chris.tivi.R
import me.banes.chris.tivi.data.Entry
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.extensions.inflateView
import me.banes.chris.tivi.ui.holders.LoadingViewHolder
import me.banes.chris.tivi.ui.holders.PosterGridHolder

open class ShowPosterGridAdapter<LI : ListItem<out Entry>>(
        private val columnCount: Int,
        private val showBinder: ((LI, PosterGridHolder) -> Unit)? = null
) : PagedListAdapter<LI, RecyclerView.ViewHolder>(TiviShowDiffCallback<LI>()) {

    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_LOADING_MORE = -1
    }

    private val loadingMoreItemPosition: Int
        get() = if (isLoading) itemCount - 1 else RecyclerView.NO_POSITION

    var isLoading = false
        set(value) {
            if (value != field) {
                val position = loadingMoreItemPosition
                if (position >= 0) {
                    when {
                        value -> notifyItemInserted(position)
                        else -> notifyItemRemoved(position)
                    }
                }
                field = value
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ITEM -> {
                PosterGridHolder(inflateView(R.layout.grid_item, parent, false))
            }
            TYPE_LOADING_MORE -> LoadingViewHolder(parent)
            else -> {
                throw IllegalArgumentException("Invalid item type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PosterGridHolder -> {
                val item = getItem(position)
                val show = item?.show
                when (show) {
                    null -> bindPlaceholder(item, holder)
                    else -> bindEntry(item, holder)
                }
            }
            is LoadingViewHolder -> holder.bind()
        }
    }

    private fun bindEntry(entry: LI, holder: PosterGridHolder) {
        showBinder?.invoke(entry, holder) ?: holder.bindShow(entry?.show?.tmdbPosterPath, entry?.show?.title)
    }

    private fun bindPlaceholder(entry: LI?, holder: PosterGridHolder) {
        holder.bindPlaceholder()
    }

    override fun getItemId(position: Int): Long = when (getItemViewType(position)) {
        TYPE_LOADING_MORE -> RecyclerView.NO_ID
        else -> super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        val itemCount = super.getItemCount()
        return when {
            position < itemCount && itemCount > 0 -> TYPE_ITEM
            else -> TYPE_LOADING_MORE
        }
    }

    fun getItemColumnSpan(position: Int) = when (getItemViewType(position)) {
        TYPE_LOADING_MORE -> columnCount
        else -> 1
    }

    override fun getItemCount(): Int = super.getItemCount() + if (isLoading) 1 else 0

    private class TiviShowDiffCallback<LI : ListItem<out Entry>> : DiffCallback<LI>() {
        override fun areItemsTheSame(oldItem: LI, newItem: LI): Boolean {
            return (oldItem.show?.id != null && oldItem.show?.id == newItem.show?.id)
                    || (oldItem.show?.traktId != null && oldItem.show?.traktId == newItem.show?.traktId)
                    || (oldItem.show?.tmdbId != null && oldItem.show?.tmdbId == newItem.show?.tmdbId)
        }

        override fun areContentsTheSame(oldItem: LI, newItem: LI): Boolean {
            return oldItem == newItem
        }
    }
}

