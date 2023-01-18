/*
 * Copyright 2022 Google LLC
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

package app.tivi.benchmark

import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalBaselineProfilesApi::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = rule.collectBaselineProfile("app.tivi") {
        startActivityAndWait()
        device.waitForIdle()

        // -------------
        // Discover
        // -------------
        device.testDiscover() || return@collectBaselineProfile
        device.navigateFromDiscoverToShowDetails()

        // -------------
        // Show Details
        // -------------
        device.testShowDetails() || return@collectBaselineProfile
        device.navigateFromShowDetailsToSeasons()

        // -------------
        // Seasons
        // -------------
        device.testSeasons() || return@collectBaselineProfile
        device.navigateFromSeasonsToEpisodeDetails()

        // -------------
        // Episode details
        // -------------
        device.testEpisodeDetails() || return@collectBaselineProfile
    }

    private fun UiDevice.testDiscover(): Boolean {
        // Scroll one of the Discover Carousels
        findObject(By.res("discover_carousel")).apply {
            scroll(Direction.RIGHT, 1f)
            scroll(Direction.LEFT, 1f)
        }

        return findObject(By.res("discover_carousel_item")) != null
    }

    private fun UiDevice.navigateFromDiscoverToShowDetails() {
        // Open a show from one of the carousels
        findObject(By.res("discover_carousel_item")).click()
        waitForIdle()
    }

    private fun UiDevice.testShowDetails(): Boolean {
        // Scroll the main show details list down, then back up
        findObject(By.scrollable(true))
            .scroll(Direction.DOWN, 1f)

        // Scroll the main show details list back up
        findObject(By.scrollable(true))
            .scroll(Direction.UP, 1f)

        // Now follow the show
        findObject(By.res("show_details_follow_button"))
            .click()

        fun seasonItemExists(): Boolean {
            return findObject(By.res("show_details_season_item")) != null
        }

        // Wait 10 seconds for a season item to show
        for (i in 1..10) {
            if (seasonItemExists()) break

            Thread.sleep(1000)
            findObject(By.scrollable(true)).scroll(Direction.DOWN, 1f)
        }

        return seasonItemExists()
    }

    private fun UiDevice.navigateFromShowDetailsToSeasons() {
        findObject(By.res("show_details_season_item")).click()
        waitForIdle()
    }

    private fun UiDevice.testSeasons(): Boolean {
        // Not much to test here at the moment
        return findObject(By.res("show_seasons_episode_item")) != null
    }

    private fun UiDevice.navigateFromSeasonsToEpisodeDetails() {
        findObject(By.res("show_seasons_episode_item")).click()
        waitForIdle()
    }

    private fun UiDevice.testEpisodeDetails(): Boolean {
        wait(Until.hasObject(By.res("episode_details")), 3_000)

        with(findObject(By.res("episode_details"))) {
            // Need to 'inset' the gesture so that we don't swipe
            // the notification tray down
            setGestureMargin(displayWidth / 10)

            // Swipe the bottom sheet 'up', then 'down'
            scroll(Direction.DOWN, 0.8f)
            scroll(Direction.UP, 0.8f)
        }

        return true
    }
}
