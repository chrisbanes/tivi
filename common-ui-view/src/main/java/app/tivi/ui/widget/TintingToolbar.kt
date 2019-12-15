/*
 * Copyright 2018 Google LLC
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

package app.tivi.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.appcompat.widget.Toolbar
import app.tivi.common.ui.R
import app.tivi.extensions.resolveThemeColor

class TintingToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.toolbarStyle
) : Toolbar(context, attrs, defStyleAttr) {
    @get:Keep
    @set:Keep
    var iconTint: Int = context.resolveThemeColor(android.R.attr.colorControlNormal)
        set(value) {
            if (value != field) {
                navigationIcon = navigationIcon?.let {
                    it.setTint(value)
                    it.mutate()
                }
                overflowIcon = overflowIcon?.let {
                    it.setTint(value)
                    it.mutate()
                }
            }
            field = value
        }
}
