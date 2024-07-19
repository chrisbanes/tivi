// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.gradle

import app.cash.licensee.LicenseeExtension
import app.cash.licensee.UnusedAction
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

fun Project.configureLicensee() {
  with(pluginManager) {
    apply("app.cash.licensee")
  }

  configure<LicenseeExtension> {
    allow("Apache-2.0")
    allow("MIT")
    allow("BSD-2-Clause")
    allow("BSD-3-Clause")
    allowUrl("https://developer.android.com/studio/terms.html")
    allowUrl("https://opensource.org/licenses/MIT") // MIT
    allowUrl("https://github.com/icerockdev/moko-permissions/blob/master/LICENSE.md") // Apache-2.0
    allowUrl("https://github.com/jordond/materialkolor/blob/master/LICENSE") // MIT
    allowUrl("https://developer.amazon.com/support/legal/pml")

    unusedAction(UnusedAction.IGNORE)
  }
}
