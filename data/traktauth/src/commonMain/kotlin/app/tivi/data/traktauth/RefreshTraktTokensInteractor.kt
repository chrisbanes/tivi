// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

fun interface RefreshTraktTokensInteractor {
    suspend operator fun invoke(): AuthState?
}
