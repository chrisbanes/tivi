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

package me.banes.chris.tivi.details.items

import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.details_summary_item.view.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.ui.groupieitems.TiviItem
import me.banes.chris.tivi.ui.holders.TiviViewHolder

class SummaryItem(private val show: TiviShow) : TiviItem<TiviViewHolder>() {

    override fun getLayout() = R.layout.details_summary_item

    override fun bind(viewHolder: TiviViewHolder, position: Int) {
        val resources = viewHolder.itemView.resources

        viewHolder.itemView.setOnClickListener(MaxLinesToggleClickListener(
                resources.getInteger(R.integer.details_summary_collapsed_lines)))

        viewHolder.itemView.details_summary.text = show.summary
    }

    override fun isClickable() = false

    private class MaxLinesToggleClickListener(private val collapsedLines: Int) : View.OnClickListener {
        private val transition = ChangeBounds().apply {
            duration = 200
            interpolator = FastOutSlowInInterpolator()
        }

        override fun onClick(view: View) {
            TransitionManager.beginDelayedTransition(view as ViewGroup, transition)
            view.details_summary.maxLines =
                    if (view.details_summary.maxLines > collapsedLines) collapsedLines else Int.MAX_VALUE
        }
    }
}