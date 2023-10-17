// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses

import kotlinx.serialization.Serializable

@Serializable
data class LicenseItem(
  val groupId: String,
  val artifactId: String,
  val version: String,
  val spdxLicenses: List<SpdxLicense>?,
  val name: String?,
  val scm: Scm?,
)

@Serializable
data class SpdxLicense(
  val identifier: String,
  val name: String,
  val url: String,
)

@Serializable
data class Scm(
  val url: String,
)
