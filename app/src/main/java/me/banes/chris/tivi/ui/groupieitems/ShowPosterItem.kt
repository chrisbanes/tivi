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

package me.banes.chris.tivi.ui.groupieitems

import android.view.View
import com.xwray.groupie.Item
import me.banes.chris.tivi.R
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.ui.holders.PosterGridHolder

internal class ShowPosterItem(val show: TiviShow) : Item<PosterGridHolder>() {

    override fun getLayout() = R.layout.grid_item

    override fun createViewHolder(itemView: View): PosterGridHolder {
        return PosterGridHolder(itemView)
    }

    override fun bind(viewHolder: PosterGridHolder, position: Int) {
        viewHolder.bindShow(show.tmdbPosterPath, show.title, show.homepage)
    }

    override fun getSpanSize(spanCount: Int, position: Int) = 1

    override fun getId(): Long = show.id!!
}