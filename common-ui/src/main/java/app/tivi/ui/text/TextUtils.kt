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

package app.tivi.ui.text

import android.content.Context
import android.text.style.TextAppearanceSpan
import android.util.TypedValue

private val typedValue = TypedValue()

fun textAppearanceSpanForAttribute(context: Context, attr: Int): TextAppearanceSpan {
    if (context.theme.resolveAttribute(attr, typedValue, true)) {
        return TextAppearanceSpan(context, typedValue.resourceId)
    } else {
        throw IllegalArgumentException("TextAppearance theme attribute can not be resolved")
    }
}
