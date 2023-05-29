// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

data class TraktOAuthInfo(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
)
