// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.app

data class ApplicationInfo(
  val packageName: String,
  val debugBuild: Boolean,
  val flavor: Flavor,
  val versionName: String,
  val versionCode: Int,
)

enum class Flavor {
  Qa,
  Standard,
}
