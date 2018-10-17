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

package app.tivi.utils

import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

fun toolbarWithTitle(@StringRes titleRes: Int) = object : BaseMatcher<Toolbar>() {
    override fun matches(item: Any?): Boolean {
        return when (item) {
            is Toolbar -> item.title == item.context.getString(titleRes)
            else -> false
        }
    }

    override fun describeTo(description: Description?) {
    }
}

fun bottomNavItemWithTitle(@StringRes titleRes: Int): Matcher<View> {
    return allOf(
            withContentDescription(titleRes),
            isDescendantOfA(isAssignableFrom(BottomNavigationView::class.java))
    )
}
