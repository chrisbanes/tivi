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

import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import kotlinx.android.synthetic.main.view_holder_details_summary.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.ui.MaxLinesToggleClickListener
import me.banes.chris.tivi.ui.epoxymodels.TiviEpoxyHolder

@EpoxyModelClass(layout = R.layout.view_holder_details_summary)
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
}