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
 *
 */

package me.banes.chris.tivi.ui

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import me.banes.chris.tivi.R
import me.banes.chris.tivi.data.TiviShow

internal class TiviShowGridAdapter
    : RecyclerView.Adapter<TiviShowGridViewHolder>() {

    private val items: MutableList<TiviShow> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TiviShowGridViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.grid_item, parent, false)
        return TiviShowGridViewHolder(view)
    }

    override fun onBindViewHolder(holder: TiviShowGridViewHolder, position: Int) {
        holder.bindShow(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(shows: List<TiviShow>) {
        val oldItems = ArrayList(items)
        items.clear()
        items.addAll(shows)

        val diffResult = DiffUtil.calculateDiff(DiffCb(oldItems, shows))
        diffResult.dispatchUpdatesTo(this)
    }

    private class DiffCb(val oldItems: List<TiviShow>,
            val newItems: List<TiviShow>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldItems.size
        }

        override fun getNewListSize(): Int {
            return newItems.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldItems[oldItemPosition]
            val newItem = newItems[newItemPosition]
            return (oldItem.id != null && oldItem.id == newItem.id)
                    || (oldItem.traktId != null && oldItem.traktId == newItem.traktId)
                    || (oldItem.tmdbId != null && oldItem.tmdbId == newItem.tmdbId)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }
    }

}

