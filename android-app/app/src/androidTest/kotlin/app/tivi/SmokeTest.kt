// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import app.tivi.app.test.AppScenarios
import org.junit.Assert.assertNotNull
import org.junit.Test

class SmokeTest {

  @Test
  fun openApp() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    startAppAndWait(device)

    // Run through the main navigation items
    AppScenarios.mainNavigationItems(device)
  }
}

private fun startAppAndWait(device: UiDevice) {
  device.pressHome()

  // Wait for launcher
  val launcherPackage = device.launcherPackageName
  assertNotNull(launcherPackage)
  device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), 5_000)

  // Launch the app
  val context = ApplicationProvider.getApplicationContext<TiviApplication>()
  val packageName = context.packageName
  val intent = context.packageManager.getLaunchIntentForPackage(packageName)!!.apply {
    // Clear out any previous instances
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
  }
  context.startActivity(intent)

  // Wait for the app to appear
  device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 5_000)
}
