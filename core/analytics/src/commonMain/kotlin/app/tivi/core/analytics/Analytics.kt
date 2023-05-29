// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.analytics

interface Analytics {
    fun trackScreenView(
        label: String,
        route: String?,
        arguments: Any? = null,
    )
}
