// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.screenshots

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import app.tivi.app.test.navigateFromDiscoverToShowDetails
import app.tivi.app.test.navigateToLibrary
import app.tivi.app.test.navigateToSearch
import app.tivi.app.test.navigateToUpNext
import app.tivi.test.smoke.startAppAndWait
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.locale.LocaleTestRule

class Screenshots {
  @Rule
  @JvmField
  val localeTestRule = LocaleTestRule()

  @Before
  fun before() {
    CleanStatusBar.enableWithDefaults()
  }

  @After
  fun after() {
    CleanStatusBar.disable()
  }

  @Test
  fun screenshots() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    startAppAndWait(device)

    Screengrab.screenshot("0_home")

    device.navigateFromDiscoverToShowDetails()
    Screengrab.screenshot("1_show_details")

    device.navigateToUpNext()
    Screengrab.screenshot("2_upnext")

    device.navigateToLibrary()
    Screengrab.screenshot("3_library")

    device.navigateToSearch()
    Screengrab.screenshot("4_search")
  }
}
