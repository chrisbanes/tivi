// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.app.test

import android.os.SystemClock
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.SearchCondition
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object AppScenarios {
    fun mainNavigationItems(device: UiDevice) {
        device.waitForIdle()

        // -------------
        // Discover
        // -------------
        device.testDiscover() || return
        device.navigateFromDiscoverToShowDetails()

        // -------------
        // Show Details
        // -------------
        device.testShowDetails() || return
        device.navigateFromShowDetailsToSeasons()

        // -------------
        // Seasons
        // -------------
        device.testSeasons() || return
        device.navigateFromSeasonsToEpisodeDetails()

        // -------------
        // Episode details
        // -------------
        device.testEpisodeDetails() || return
    }
}

private fun UiDevice.testDiscover(): Boolean {
    // Scroll one of the Discover Carousels. Might need to wait a while for the app to load
    waitForObject(By.res("discover_carousel"), 30.seconds)
        .apply { setGestureMargin(visibleBounds.width() / 6) }
        .scroll(Direction.RIGHT, 1f)

    waitForObject(By.res("discover_carousel"))
        .apply { setGestureMargin(visibleBounds.width() / 6) }
        .scroll(Direction.LEFT, 1f)

    return true
}

private fun UiDevice.navigateFromDiscoverToShowDetails() {
    // Open a show from one of the carousels
    waitForObject(By.res("discover_carousel_item")).click()
    waitForIdle()
}

private fun UiDevice.testShowDetails(): Boolean {
    // Follow the show
    waitForObject(By.res("show_details_follow_button")).click()

    // Keep scrolling to the end of the LazyColumn, waiting for a season item
    repeat(20) {
        if (hasObject(By.res("show_details_season_item"))) {
            return true
        }

        SystemClock.sleep(1.seconds.inWholeMilliseconds)

        // Scroll to the end to show the seasons
        waitForObject(By.res("show_details_lazycolumn"))
            .scroll(Direction.DOWN, 0.8f)
    }

    return false
}

private fun UiDevice.navigateFromShowDetailsToSeasons() {
    waitForObject(By.res("show_details_season_item")).click()
    waitForIdle()
}

private fun UiDevice.testSeasons(): Boolean {
    // Not much to test here at the moment
    return wait(Until.hasObject(By.res("show_seasons_episode_item")), 5.seconds)
}

private fun UiDevice.navigateFromSeasonsToEpisodeDetails() {
    waitForObject(By.res("show_seasons_episode_item")).click()
    waitForIdle()
}

private fun UiDevice.testEpisodeDetails(): Boolean {
    waitForObject(By.res("episode_details")).run {
        // Need to 'inset' the gesture so that we don't swipe
        // the notification tray down
        setGestureMargin(displayWidth / 10)

        // Swipe the bottom sheet 'up', then 'down'
        scroll(Direction.DOWN, 0.8f)
        scroll(Direction.UP, 0.8f)
    }
    return true
}

fun UiDevice.waitForObject(selector: BySelector, timeout: Duration = 5.seconds): UiObject2 {
    if (wait(Until.hasObject(selector), timeout)) {
        return findObject(selector)
    }
    error("Object with selector [$selector] not found")
}

fun <R> UiDevice.wait(condition: SearchCondition<R>, timeout: Duration): R {
    return wait(condition, timeout.inWholeMilliseconds)
}
