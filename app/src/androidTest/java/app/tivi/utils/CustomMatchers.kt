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

package app.tivi.utils

import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.v7.widget.Toolbar
import android.view.View
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

fun toolbarWithTitle(@StringRes titleRes: Int): Matcher<View> {
    return object : BaseMatcher<View>() {
        override fun matches(item: Any?): Boolean {
            return when (item) {
                is Toolbar -> item.title == item.context.getString(titleRes)
                else -> false
            }
        }

        override fun describeTo(description: Description?) {
        }
    }
}

fun bottomNavItemWithTitle(@IdRes bottomNavId: Int, @StringRes titleRes: Int): Matcher<View> {
    return allOf(withText(titleRes), isDescendantOfA(ViewMatchers.withId(bottomNavId)), isDisplayed())
}
