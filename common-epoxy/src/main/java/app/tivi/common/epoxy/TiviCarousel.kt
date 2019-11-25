/*
 * Copyright 2019 Google LLC
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

package app.tivi.common.epoxy

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Dimension
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView

@ModelView(saveViewState = true, autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class TiviCarousel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : Carousel(context, attrs, defStyle) {

    @Dimension(unit = Dimension.PX)
    @set:ModelProp
    var itemWidth: Int = 0

    override fun onChildAttachedToWindow(child: View) {
        check(!(itemWidth > 0 && numViewsToShowOnScreen > 0)) {
            "Can't use itemWidth and numViewsToShowOnScreen together"
        }
        if (itemWidth > 0) {
            val childLayoutParams = child.layoutParams
            childLayoutParams.width = itemWidth
        }
        super.onChildAttachedToWindow(child)
    }
}
