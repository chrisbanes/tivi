// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses

import kotlinx.serialization.Serializable

data class LicensesState(
    val licenseItemList: List<LicenseItem>,
)

/**
 * {
 *         "groupId": "com.google.firebase",
 *         "artifactId": "firebase-crashlytics",
 *         "version": "18.4.1",
 *         "spdxLicenses": [
 *             {
 *                 "identifier": "Apache-2.0",
 *                 "name": "Apache License 2.0",
 *                 "url": "https://www.apache.org/licenses/LICENSE-2.0"
 *             }
 *         ],
 *         "scm": {
 *             "url": "https://github.com/firebase/firebase-android-sdk"
 *         }
 *     },
 */

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
