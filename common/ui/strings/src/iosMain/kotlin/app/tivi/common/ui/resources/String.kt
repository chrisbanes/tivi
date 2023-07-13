// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.ui.resources

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat

/**
 * https://stackoverflow.com/a/71442518
 */
actual fun String.fmt(vararg args: Any?): String = when (args.size) {
    0 -> NSString.stringWithFormat(this)
    1 -> NSString.stringWithFormat(this, args[0])
    2 -> NSString.stringWithFormat(this, args[0], args[1])
    3 -> NSString.stringWithFormat(this, args[0], args[1], args[2])
    4 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3])
    5 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3], args[4])
    6 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3], args[4], args[5])
    7 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3], args[4], args[5], args[6])
    8 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7])
    9 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8])
    10 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9])
    else -> throw IllegalStateException("String.format() can only accept up to 10 arguments")
}
