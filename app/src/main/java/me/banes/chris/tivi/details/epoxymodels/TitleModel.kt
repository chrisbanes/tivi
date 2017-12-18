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

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import kotlinx.android.synthetic.main.details_title_item.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.ui.epoxymodels.TiviEpoxyHolder

@EpoxyModelClass(layout = R.layout.details_title_item)
abstract class TitleModel : EpoxyModelWithHolder<TiviEpoxyHolder>() {
    @EpoxyAttribute var title: String? = null
    @EpoxyAttribute var subtitle: String? = null
    @EpoxyAttribute var genres: String? = null

    override fun bind(viewHolder: TiviEpoxyHolder) {
        viewHolder.details_title.text = title

        viewHolder.details_subtitle.apply {
            if (subtitle != null) {
                text = subtitle
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        viewHolder.details_categories.apply {
            text = genres
        }
    }

    override fun getSpanSize(totalSpanCount: Int, position: Int, itemCount: Int) = totalSpanCount
}