/*
 * Copyright 2017 Google LLC
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

package app.tivi.ui

import android.os.Handler
import android.os.Looper
import android.os.SystemClock

/**
 * A class which acts as a time latch for show progress bars UIs. It waits a minimum time to be
 * dismissed before showing. Once visible, the progress bar will be visible for
 * a minimum amount of time to avoid "flashes" in the UI when an event could take
 * a largely variable time to complete (from none, to a user perceivable amount).
 *
 * Works with an view through the lambda API.
 */
class ProgressTimeLatch(
    private val delayMs: Long = 750,
    private val minShowTime: Long = 500,
    private val viewRefreshingToggle: ((Boolean) -> Unit)
) {
    private val handler = Handler(Looper.getMainLooper())
    private var showTime = 0L

    private val delayedShow = Runnable(this::show)
    private val delayedHide = Runnable(this::hideAndReset)

    var refreshing = false
        set(value) {
            if (field != value) {
                field = value
                handler.removeCallbacks(delayedShow)
                handler.removeCallbacks(delayedHide)

                if (value) {
                    handler.postDelayed(delayedShow, delayMs)
                } else if (showTime >= 0) {
                    // We're already showing, lets check if we need to delay the hide
                    val showTime = SystemClock.uptimeMillis() - showTime
                    if (showTime < minShowTime) {
                        handler.postDelayed(delayedHide, minShowTime - showTime)
                    } else {
                        // We've been showing longer than the min, so hide and clean up
                        hideAndReset()
                    }
                } else {
                    // We're not currently show so just hide and clean up
                    hideAndReset()
                }
            }
        }

    private fun show() {
        viewRefreshingToggle(true)
        showTime = SystemClock.uptimeMillis()
    }

    private fun hideAndReset() {
        viewRefreshingToggle(false)
        showTime = 0
    }
}
