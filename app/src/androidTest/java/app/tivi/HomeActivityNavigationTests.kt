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

package app.tivi

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.hasSibling
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.isRoot
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import app.tivi.home.HomeActivity
import app.tivi.utils.bottomNavItemWithTitle
import app.tivi.utils.rotateLandscape
import app.tivi.utils.toolbarWithTitle
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.notNullValue
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

        onView(toolbarWithTitle(R.string.library_title))
                .check(matches(isDisplayed()))
    }

    @Test
    fun testBottomNavigationLibraryClickAfterRotation() {
        onView(isRoot()).perform(rotateLandscape())

        onView(bottomNavItemWithTitle(R.id.home_bottom_nav, R.string.home_nav_library))
                .perform(click())

        onView(toolbarWithTitle(R.string.library_title))
                .check(matches(isDisplayed()))
    }

    @Test
    fun testRotateRetainsMainFragment() {
        // First click the trending header
        onView(allOf(withText(R.string.header_more), hasSibling(withText(R.string.discover_trending))))
                .perform(click())

        // And assert that the trending fragment is shown
        onView(toolbarWithTitle(R.string.discover_trending)).check(matches(notNullValue()))

        // Now rotate the screen
        onView(isRoot()).perform(rotateLandscape())

        // And check that the trending show is still shown
        onView(toolbarWithTitle(R.string.discover_trending)).check(matches(notNullValue()))
    }
}