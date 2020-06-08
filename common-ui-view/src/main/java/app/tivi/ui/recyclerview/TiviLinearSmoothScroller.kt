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

package app.tivi.ui.recyclerview

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller

class TiviLinearSmoothScroller(
    private val context: Context,
    private val snapPreference: Int = SNAP_TO_ANY,
    private val scrollMsPerInch: Float = 25f
) : LinearSmoothScroller(context) {

    var targetOffset: Int = 0

    override fun getVerticalSnapPreference(): Int = snapPreference

    override fun getHorizontalSnapPreference(): Int = snapPreference

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return scrollMsPerInch / displayMetrics.densityDpi
    }

    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int {
        return targetOffset + super.calculateDtToFit(
            viewStart, viewEnd,
            boxStart, boxEnd, snapPreference
        )
    }
}
