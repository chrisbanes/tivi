/*
 * Copyright 2019 Google LLC
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

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionLogger @Inject constructor(
    private val logger: Logger
) {
    operator fun <T> invoke(message: String, b: () -> T): T {
        try {
            return b()
        } catch (t: Throwable) {
            logger.logForCrash(message)
            throw t
        }
    }

    operator fun <T> invoke(message: String, vararg args: Any, b: () -> T): T {
        try {
            return b()
        } catch (t: Throwable) {
            logger.logForCrash(message, args)
            throw t
        }
    }
}