// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data

internal val Boolean.sqlValue: Long get() = if (this) 1 else 0
