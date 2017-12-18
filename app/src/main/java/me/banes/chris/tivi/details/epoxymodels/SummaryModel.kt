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

package me.banes.chris.tivi.details.epoxymodels

import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import kotlinx.android.synthetic.main.details_summary_item.*
import kotlinx.android.synthetic.main.details_summary_item.view.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.ui.epoxymodels.TiviEpoxyHolder

@EpoxyModelClass(layout = R.layout.details_summary_item)
abstract class SummaryModel : EpoxyModelWithHolder<TiviEpoxyHolder>() {
    @EpoxyAttribute var summary: String? = null

    override fun bind(holder: TiviEpoxyHolder) {
        holder.details_summary.text = summary

        holder.containerView?.apply {
            setOnClickListener(
                    MaxLinesToggleClickListener(resources.getInteger(R.integer.details_summary_collapsed_lines)))
        }
    }

    override fun getSpanSize(totalSpanCount: Int, position: Int, itemCount: Int) = totalSpanCount

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