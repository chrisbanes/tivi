// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

interface Logger {

    fun setup(debugMode: Boolean)

    fun setUserId(id: String)

    /** Log a verbose exception and a message with optional format args.  */
    fun v(throwable: Throwable? = null, message: () -> String = { "" })

    /** Log a verbose exception and a message with optional format args.  */
    fun d(throwable: Throwable? = null, message: () -> String = { "" })

    /** Log a verbose exception and a message with optional format args.  */
    fun i(throwable: Throwable? = null, message: () -> String = { "" })

    /** Log a verbose exception and a message with optional format args.  */
    fun e(throwable: Throwable? = null, message: () -> String = { "" })
}
