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

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import kotlinx.android.synthetic.main.view_holder_details_badge.*
import me.banes.chris.tivi.R
import me.banes.chris.tivi.ui.epoxymodels.TiviEpoxyHolder

@EpoxyModelClass(layout = R.layout.view_holder_details_badge)
abstract class BadgeModel : EpoxyModelWithHolder<TiviEpoxyHolder>() {
    @EpoxyAttribute var label: String? = null
    @EpoxyAttribute @StringRes var labelRes = 0
    @EpoxyAttribute @DrawableRes var iconRes = 0

    override fun bind(holder: TiviEpoxyHolder) {
        holder.details_badge_icon.setImageResource(iconRes)
        holder.details_badge_label.apply {
            when {
                label != null -> text = label
                labelRes > 0 -> setText(labelRes)
                else -> text = "-"
            }
        }
    }
}