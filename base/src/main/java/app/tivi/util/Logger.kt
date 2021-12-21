/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.util

interface Logger {

    fun setup(debugMode: Boolean)

    fun setUserId(id: String)

    /** Log a verbose message with optional format args.  */
    fun v(message: String, vararg args: Any?)

    /** Log a verbose exception and a message with optional format args.  */
    fun v(t: Throwable, message: String, vararg args: Any?)

    /** Log a verbose exception.  */
    fun v(t: Throwable)

    /** Log a debug message with optional format args.  */
    fun d(message: String, vararg args: Any?)

    /** Log a debug exception and a message with optional format args.  */
    fun d(t: Throwable, message: String, vararg args: Any?)

    /** Log a debug exception.  */
    fun d(t: Throwable)

    /** Log an info message with optional format args.  */
    fun i(message: String, vararg args: Any?)

    /** Log an info exception and a message with optional format args.  */
    fun i(t: Throwable, message: String, vararg args: Any?)

    /** Log an info exception.  */
    fun i(t: Throwable)

    /** Log a warning message with optional format args.  */
    fun w(message: String, vararg args: Any?)

    /** Log a warning exception and a message with optional format args.  */
    fun w(t: Throwable, message: String, vararg args: Any?)

    /** Log a warning exception.  */
    fun w(t: Throwable)

    /** Log an error message with optional format args.  */
    fun e(message: String, vararg args: Any?)

    /** Log an error exception and a message with optional format args.  */
    fun e(t: Throwable, message: String, vararg args: Any?)

    /** Log an error exception.  */
    fun e(t: Throwable)

    /** Log an assert message with optional format args.  */
    fun wtf(message: String, vararg args: Any?)

    /** Log an assert exception and a message with optional format args.  */
    fun wtf(t: Throwable, message: String, vararg args: Any?)

    /** Log an assert exception.  */
    fun wtf(t: Throwable)
}
