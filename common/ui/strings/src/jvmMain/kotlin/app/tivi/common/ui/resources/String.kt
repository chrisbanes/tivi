// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.ui.resources

internal actual fun String.fmt(vararg args: Any?): String = format(*args)
