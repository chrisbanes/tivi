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

package me.banes.chris.tivi.ui.epoxymodels

import android.support.annotation.StringRes
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import kotlinx.android.synthetic.main.header_item.*
import me.banes.chris.tivi.R

@EpoxyModelClass(layout = R.layout.header_item)
abstract class HeaderModel : EpoxyModelWithHolder<TiviEpoxyHolder>() {
    @EpoxyAttribute @StringRes var title = 0
    @EpoxyAttribute var clickListener: View.OnClickListener? = null

    override fun bind(holder: TiviEpoxyHolder) {
        holder.header_title.setText(title)
        holder.header_more.setOnClickListener(clickListener)
    }

    override fun getSpanSize(totalSpanCount: Int, position: Int, itemCount: Int) = totalSpanCount
}