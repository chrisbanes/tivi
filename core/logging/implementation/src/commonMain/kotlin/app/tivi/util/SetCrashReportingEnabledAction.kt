// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

fun interface SetCrashReportingEnabledAction {
    operator fun invoke(enabled: Boolean)
}
