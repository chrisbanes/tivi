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

package app.tivi

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import app.tivi.home.HomeActivity
import app.tivi.utils.bottomNavItemWithTitle
import app.tivi.utils.rotateLandscape
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class HomeActivityNavigationTests {

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(HomeActivity::class.java)

    @Test
    fun testBottomNavigationLibraryClick() {
        onView(bottomNavItemWithTitle(R.id.home_bottom_nav, R.string.home_nav_library))
                .perform(click())

        onView(withId(R.id.library_rv))
                .check(matches(isDisplayed()))
    }

    @Test
    fun testBottomNavigationLibraryClickAfterRotation() {
        onView(isRoot()).perform(rotateLandscape())

        onView(bottomNavItemWithTitle(R.id.home_bottom_nav, R.string.home_nav_library))
                .perform(click())

        onView(withId(R.id.library_rv))
                .check(matches(isDisplayed()))
    }
}