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

package app.tivi.showdetails

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import app.tivi.R
import app.tivi.TiviActivity
import dev.chrisbanes.insetter.doOnApplyWindowInsets

class ShowDetailsActivity : TiviActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_details)

        findViewById<View>(R.id.details_root).apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

            doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(
                    left = insets.systemWindowInsetLeft + initialState.paddings.left,
                    right = insets.systemWindowInsetRight + initialState.paddings.right
                )
            }
        }

        postponeEnterTransition()
    }

    override fun handleIntent(intent: Intent) {
        supportFragmentManager.commit {
            replace(
                R.id.details_content,
                NavHostFragment.create(R.navigation.show_details_nav_graph, intent.extras)
            )
        }
    }
}
