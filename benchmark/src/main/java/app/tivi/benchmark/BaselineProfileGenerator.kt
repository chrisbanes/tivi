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

import android.os.SystemClock
import androidx.benchmark.macro.ExperimentalStableBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @OptIn(ExperimentalStableBaselineProfilesApi::class)
    @Test
    fun generateBaselineProfile() = rule.collectStableBaselineProfile(
        packageName = "app.tivi",
        maxIterations = 15, // What @tikurahul said
    ) {
        startActivityAndWait()
        device.waitForIdle()

        // -------------
        // Discover
        // -------------
        device.testDiscover() || return@collectStableBaselineProfile
        device.navigateFromDiscoverToShowDetails()

        // -------------
        // Show Details
        // -------------
        device.testShowDetails() || return@collectStableBaselineProfile
        device.navigateFromShowDetailsToSeasons()

        // -------------
        // Seasons
        // -------------
        device.testSeasons() || return@collectStableBaselineProfile
        device.navigateFromSeasonsToEpisodeDetails()

        // -------------
        // Episode details
        // -------------
        device.testEpisodeDetails() || return@collectStableBaselineProfile
    }

    private fun UiDevice.testDiscover(): Boolean {
        // Scroll one of the Discover Carousels
        waitForObject(By.res("discover_carousel"))
            .scroll(Direction.RIGHT, 1f)
        waitForObject(By.res("discover_carousel"))
            .scroll(Direction.LEFT, 1f)

        return wait(Until.hasObject(By.res("discover_carousel_item")), 5_000)
    }

    private fun UiDevice.navigateFromDiscoverToShowDetails() {
        // Open a show from one of the carousels
        waitForObject(By.res("discover_carousel_item")).click()
        waitForIdle()
    }

    private fun UiDevice.testShowDetails(): Boolean {
        // Follow the show
        waitForObject(By.res("show_details_follow_button"))
            .click()

        // Wait 10 seconds for a season item to show
        for (i in 1..10) {
            if (hasObject(By.res("show_details_season_item"))) {
                return true
            }

            SystemClock.sleep(1000)

            // Scroll to the end to show the seasons
            waitForObject(By.res("show_details_lazycolumn"))
                .scroll(Direction.DOWN, 1f)
        }

        return false
    }

    private fun UiDevice.navigateFromShowDetailsToSeasons() {
        waitForObject(By.res("show_details_season_item")).click()
        waitForIdle()
    }

    private fun UiDevice.testSeasons(): Boolean {
        // Not much to test here at the moment
        return wait(Until.hasObject(By.res("show_seasons_episode_item")), 5_000)
    }

    private fun UiDevice.navigateFromSeasonsToEpisodeDetails() {
        waitForObject(By.res("show_seasons_episode_item")).click()
        waitForIdle()
    }

    private fun UiDevice.testEpisodeDetails(): Boolean {
        with(waitForObject(By.res("episode_details"))) {
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

private fun UiDevice.waitForObject(selector: BySelector, timeout: Long = 5_000): UiObject2 {
    if (wait(Until.hasObject(selector), timeout)) {
        return findObject(selector)
    }

    error("Object with selector [$selector] not found")
}
