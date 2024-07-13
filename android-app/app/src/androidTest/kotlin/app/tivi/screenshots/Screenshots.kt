// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.screenshots

import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import app.tivi.app.test.navigateFromDiscoverToShowDetails
import app.tivi.app.test.navigateToLibrary
import app.tivi.app.test.navigateToSearch
import app.tivi.app.test.navigateToUpNext
import app.tivi.test.smoke.startAppAndWait
import org.junit.Rule
import org.junit.Test
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

class Screenshots {
  @Rule
  @JvmField
  val localeTestRule = LocaleTestRule()

  @Test
  fun screenshots() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    startAppAndWait(device)

    SystemClock.sleep(3_000)
    Screengrab.screenshot("0_home")

    device.navigateFromDiscoverToShowDetails()
    SystemClock.sleep(1_000)
    Screengrab.screenshot("1_show_details")

    device.navigateToUpNext()
    SystemClock.sleep(1_000)
    Screengrab.screenshot("2_upnext")

    device.navigateToLibrary()
    SystemClock.sleep(1_000)
    Screengrab.screenshot("3_library")

    device.navigateToSearch()
    SystemClock.sleep(1_000)
    Screengrab.screenshot("4_search")
  }
}
