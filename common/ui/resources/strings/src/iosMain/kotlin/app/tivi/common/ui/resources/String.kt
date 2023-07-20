// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.ui.resources

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat

/**
 * https://github.com/icerockdev/moko-resources/blob/c7e3ec78838308aaf9079496a080a12a1adff938/resources/src/appleMain/kotlin/dev/icerock/moko/resources/desc/Utils.kt#L137
 */
actual fun String.fmt(vararg args: Any?): String {
    // NSString format works with NSObjects via %@, we should change standard format to %@
    val fixed = replace(Regex("%((?:\\.|\\d|\\$)*)[abcdefs]"), "%$1@")

    return when (args.size) {
        0 -> NSString.stringWithFormat(fixed)
        1 -> NSString.stringWithFormat(fixed, args[0])
        2 -> NSString.stringWithFormat(fixed, args[0], args[1])
        3 -> NSString.stringWithFormat(fixed, args[0], args[1], args[2])
        4 -> NSString.stringWithFormat(fixed, args[0], args[1], args[2], args[3])
        5 -> NSString.stringWithFormat(fixed, args[0], args[1], args[2], args[3], args[4])
        6 -> NSString.stringWithFormat(fixed, args[0], args[1], args[2], args[3], args[4], args[5])
        7 -> NSString.stringWithFormat(fixed, args[0], args[1], args[2], args[3], args[4], args[5], args[6])
        8 -> NSString.stringWithFormat(fixed, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7])
        9 -> NSString.stringWithFormat(fixed, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8])
        10 -> NSString.stringWithFormat(fixed, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9])
        else -> error("String.fmt() can only accept up to 10 arguments")
    }
}
