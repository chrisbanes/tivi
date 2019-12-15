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

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.RecyclerView
import app.tivi.extensions.findBaseContext

class HideImeOnScrollListener : RecyclerView.OnScrollListener() {
    private lateinit var imm: InputMethodManager

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING && recyclerView.childCount > 0) {
            if (!::imm.isInitialized) {
                imm = recyclerView.context.getSystemService()!!
            }
            if (imm.isAcceptingText) {
                val activity: Activity? = recyclerView.context.findBaseContext()
                val currentFocus = activity?.currentFocus
                if (currentFocus != null) {
                    imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
                }
            }
        }
    }
}
