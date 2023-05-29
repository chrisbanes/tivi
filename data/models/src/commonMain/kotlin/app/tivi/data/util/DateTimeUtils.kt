// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.util

import kotlin.time.Duration
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

inline val Duration.inPast: Instant
    get() = Clock.System.now() - this
